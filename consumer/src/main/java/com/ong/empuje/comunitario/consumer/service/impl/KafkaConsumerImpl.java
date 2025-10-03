package com.ong.empuje.comunitario.consumer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.dto.in.EventDTO;
import com.ong.empuje.comunitario.consumer.dto.in.EventVoluntaryDTO;
import com.ong.empuje.comunitario.consumer.mapper.EventMapper;
import com.ong.empuje.comunitario.consumer.mapper.VoluntaryMapper;
import com.ong.empuje.comunitario.consumer.model.Event;
import com.ong.empuje.comunitario.consumer.model.Organization;
import com.ong.empuje.comunitario.consumer.model.Voluntary;
import com.ong.empuje.comunitario.consumer.model.VoluntaryEvents;
import com.ong.empuje.comunitario.consumer.repository.EventRepository;
import com.ong.empuje.comunitario.consumer.repository.OrganizationRepository;
import com.ong.empuje.comunitario.consumer.repository.VoluntaryEventsRepository;
import com.ong.empuje.comunitario.consumer.repository.VoluntaryRepository;
import com.ong.empuje.comunitario.consumer.service.IConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class KafkaConsumerImpl implements IConsumer {

    private final EventRepository eventRepository;
    private final OrganizationRepository organizationRepository;
    private final VoluntaryRepository voluntaryRepository;
    private final VoluntaryEventsRepository voluntaryEventsRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumerImpl(EventRepository eventRepository, OrganizationRepository organizationRepository, VoluntaryRepository voluntaryRepository, VoluntaryEventsRepository registrationEventsRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.organizationRepository = organizationRepository;
        this.voluntaryRepository = voluntaryRepository;
        this.voluntaryEventsRepository = registrationEventsRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @KafkaListener(topics = "eventos-solidarios", groupId = "consumidor1")
    public void listenCreateEvents(String message) {

        try {
            EventDTO event = objectMapper.readValue(message, EventDTO.class);
            eventRepository.save(build(event));
        }catch (Exception e){
            System.out.println("Exception : "+ e.getCause() + e.getMessage());
        }
    }

    @Override
    @KafkaListener(topics = "baja-evento-solidario", groupId = "consumidor2")
    public void listenDeleteEvents(String message) {

        try {
            EventDTO deleteEvent = objectMapper.readValue(message,EventDTO.class);

            if(Integer.parseInt(deleteEvent.organizationId()) != 1) { // Id de organizacion propia
                eventRepository.deleteByRemoteIdAndOrganizationId(deleteEvent.eventId(), Integer.valueOf(deleteEvent.organizationId()));
            }

            }catch (Exception e){
            System.out.println("Exception : "+ e.getCause() + e.getMessage());
        }
    }

    @Override
    @KafkaListener(topics = "adhesion-evento",groupId = "consumidor3")
    public void listenAddVoluntary(String message) {

        try {

            EventVoluntaryDTO eventVoluntary = objectMapper.readValue(message, EventVoluntaryDTO.class);
            Voluntary voluntary = VoluntaryMapper.INSTANCE.toEntity(eventVoluntary.voluntary());

            if(eventVoluntary.originOrganizationId() == 1) { // mi organización
                Optional<Event> event = eventRepository.findById(eventVoluntary.remoteId());

                Voluntary toSave;

                if (event.isPresent()) {
                    Optional<Voluntary> toUpdate = voluntaryRepository
                            .findByOrganizationIdAndVoluntaryId(voluntary.getOrganizationId(), voluntary.getVoluntaryId());

                    if (toUpdate.isPresent()) {
                        Voluntary existing = toUpdate.get();
                        existing.setName(voluntary.getName());
                        existing.setLastName(voluntary.getLastName());
                        existing.setPhone(voluntary.getPhone());
                        existing.setEmail(voluntary.getEmail());

                        toSave = voluntaryRepository.save(existing);
                    } else {
                        toSave = voluntaryRepository.save(voluntary);
                    }

                    VoluntaryEvents voluntaryEvents = new VoluntaryEvents();
                    voluntaryEvents.setEvent(event.get());
                    voluntaryEvents.setVoluntary(toSave);
                    voluntaryEvents.setRegistrationDate(new Date());
                    voluntaryEventsRepository.save(voluntaryEvents);
                }
            }
        }catch (Exception e){
            System.out.println("Exception : "+ e.getCause() + e.getMessage());
        }
    }


    private Event build(EventDTO message) throws Exception {
        Optional<Organization> organization = organizationRepository.findById(Integer.valueOf(message.organizationId()));

        if(organization.isPresent()) {
            Event event = EventMapper.INSTANCE.toEntity(message);
            event.setOrganization(organization.get());
            return event;
        }else{
            throw new Exception("no existe la organizacion");
        }
    }
}
