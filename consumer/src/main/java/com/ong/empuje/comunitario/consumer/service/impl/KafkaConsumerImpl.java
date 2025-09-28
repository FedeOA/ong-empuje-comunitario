package com.ong.empuje.comunitario.consumer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.dto.in.EventDTO;
import com.ong.empuje.comunitario.consumer.mapper.EventMapper;
import com.ong.empuje.comunitario.consumer.model.Event;
import com.ong.empuje.comunitario.consumer.model.Organization;
import com.ong.empuje.comunitario.consumer.repository.EventRepository;
import com.ong.empuje.comunitario.consumer.repository.OrganizationRepository;
import com.ong.empuje.comunitario.consumer.service.IConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class KafkaConsumerImpl implements IConsumer {

    private final EventRepository eventRepository;
    private final OrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumerImpl(EventRepository eventRepository, OrganizationRepository organizationRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.organizationRepository = organizationRepository;
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
