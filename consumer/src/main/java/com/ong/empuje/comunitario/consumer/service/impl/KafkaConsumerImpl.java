package com.ong.empuje.comunitario.consumer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.dto.in.EventDTO;
import com.ong.empuje.comunitario.consumer.dto.in.EventVoluntaryDTO;
import com.ong.empuje.comunitario.consumer.mapper.EventMapper;
import com.ong.empuje.comunitario.consumer.mapper.VoluntaryMapper;
import com.ong.empuje.comunitario.consumer.model.*;
import com.ong.empuje.comunitario.consumer.repository.*;
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
    private final UserRepository userRepository;
    private final UserEventsRepository userEventsRepository;

    public KafkaConsumerImpl(EventRepository eventRepository, OrganizationRepository organizationRepository, VoluntaryRepository voluntaryRepository, VoluntaryEventsRepository registrationEventsRepository, ObjectMapper objectMapper, UserRepository userRepository, UserEventsRepository userEventsRepository) {
        this.eventRepository = eventRepository;
        this.organizationRepository = organizationRepository;
        this.voluntaryRepository = voluntaryRepository;
        this.voluntaryEventsRepository = registrationEventsRepository;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.userEventsRepository = userEventsRepository;
    }

    @Override
    @KafkaListener(topics = "eventos-solidarios", groupId = "consumidor1")
    public void listenCreateEvents(String message) {

        try {
            EventDTO event = objectMapper.readValue(message, EventDTO.class);

            if(Integer.parseInt(event.organizationId()) != 1) { // si no es mi orgaizacion
                eventRepository.save(build(event));
            }
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

            Optional<Event> event = eventRepository.findByRemoteId(eventVoluntary.remoteId());

            if (event.isPresent()) {
                if (eventVoluntary.originOrganizationId() == 1) { // evento de mi organización

                    Voluntary toSave;

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
                } else if (voluntary.getOrganizationId() == 1){ // usuario de mi organizacion

                    Optional<User> user = userRepository.findById(voluntary.getVoluntaryId());

                    if(user.isPresent()){
                        UserEvents userEvents = new UserEvents();
                        userEvents.setEvent(event.get());
                        userEvents.setUser(user.get());
                        userEvents.setRegistrationDate(new Date());
                        userEventsRepository.save(userEvents);
                    }
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
