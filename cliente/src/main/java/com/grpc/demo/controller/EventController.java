package com.grpc.demo.controller;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpc.demo.dto.in.EventDTO;
import com.grpc.demo.dto.out.EventResponseDTO;
import com.grpc.demo.dto.out.ExternalEventResponseDTO;
import com.grpc.demo.dto.out.ResponseDTO;
import com.grpc.demo.dto.producer.EventDeleteDTO;
import com.grpc.demo.dto.producer.EventPublicationDTO;
import com.grpc.demo.dto.producer.EventVoluntaryDTO;
import com.grpc.demo.dto.producer.VoluntaryDTO;
import com.grpc.demo.enums.Organization;
import com.grpc.demo.enums.Topic;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.UserClient;
import com.grpc.demo.service.event.Event;
import com.grpc.demo.service.event.ExternalEvent;
import com.grpc.demo.service.event.Response;
import com.grpc.demo.service.producer.IProducer;
import com.grpc.demo.service.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grpc.demo.service.EventClient;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventClient eventClient;
    private final UserClient uSerClient;
    private final IProducer kafkaProducer;
    private final IMapper<Event, EventResponseDTO> mapper;
    private final IMapper<ExternalEvent, ExternalEventResponseDTO> externalEventMapper;
    private final ObjectMapper objectMapper;

    public EventController(EventClient eventClient, UserClient uSerClient, IProducer kafkaProducer, IMapper<Event, EventResponseDTO> mapper, IMapper<ExternalEvent, ExternalEventResponseDTO> externalEventMapper, ObjectMapper objectMapper) {
        this.eventClient = eventClient;
        this.uSerClient = uSerClient;
        this.kafkaProducer = kafkaProducer;
        this.mapper = mapper;
        this.externalEventMapper = externalEventMapper;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createEvent(@RequestBody EventDTO event) {
        try {
            Response serverResponse = eventClient.createEvent(event);
            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO> updateEvent(@PathVariable int id, @RequestBody EventDTO event) {
        try {

            Response serverResponse = eventClient.updateEvent(id,event);
            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteEvent(@PathVariable int id){
        try {
            Event event = Event.newBuilder().setId(id).build();
            Response serverResponse = eventClient.deleteEvent(event);

            EventDeleteDTO eventDelete = new EventDeleteDTO(id,Organization.ONG_EMPUJE_COMUNITARIO.getId());
            String jsonMessage = objectMapper.writeValueAsString(eventDelete);
            kafkaProducer.sendMessage(Topic.BAJA_EVENTO_SOLIDARIO.getName(),jsonMessage);

            ResponseDTO response = new ResponseDTO((serverResponse.getSuccess()),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> listEvents() {
        try {

            List<Event> serverEvents = eventClient.listEvents();
            List<EventResponseDTO> events = mapper.mapList(serverEvents);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/externals")
    public ResponseEntity<List<ExternalEventResponseDTO>> listExternalEvents() {
        try {

            List<ExternalEvent> serverEvents = eventClient.listExternalEvents();
            List<ExternalEventResponseDTO> events = externalEventMapper.mapList(serverEvents);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("#username == authentication.name or hasAnyRole('PRESIDENTE','COORDINADOR')")
    @PostMapping("/{eventId}/users/{username}")
    public ResponseEntity<ResponseDTO> addUserToEvent(@PathVariable int eventId, @PathVariable String username){
        try {
            Response serverResponse = eventClient.addUserToEvent(eventId, username);
            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }

    @PreAuthorize("#username == authentication.name or hasAnyRole('PRESIDENTE','COORDINADOR')")
    @DeleteMapping("/{eventId}/users/{username}")
    public ResponseEntity<ResponseDTO> removeUserFromEvent(@PathVariable int eventId, @PathVariable String username){
        try {
            Response serverResponse = eventClient.removeUserFromEvent(eventId, username);
            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }

    @PostMapping("/{eventId}/donations/{donationId}")
    public ResponseEntity<ResponseDTO> useDonationsInEvent(@PathVariable int eventId, @PathVariable int donationId){
        try {
            Response serverResponse = eventClient.useDonationsInEvent(eventId, donationId);
            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }

    @PostMapping("/publish")
    public ResponseEntity<ResponseDTO> publishEvent(@RequestBody EventPublicationDTO event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            kafkaProducer.sendMessage(Topic.EVENTOS_SOLIDARIOS.getName(), jsonMessage);
            ResponseDTO response = new ResponseDTO(true, "Mensaje enviado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false, e.getMessage()));
        }
    }

    @PreAuthorize("#username == authentication.name")
    @PostMapping("/{remoteId}/organization/{originOrganizationId}/user/{username}")
    public ResponseEntity<ResponseDTO> addToRemoteEvent(@PathVariable int originOrganizationId,
                                                        @PathVariable int remoteId,
                                                        @PathVariable String username) {

        try {

            User user = uSerClient.getUserByUsername(username);

            VoluntaryDTO voluntary= new VoluntaryDTO(
                    Organization.ONG_SOMOS_MAS.getId(),
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhone(),
                    user.getEmail());

            EventVoluntaryDTO eventVoluntary= new EventVoluntaryDTO(remoteId,originOrganizationId,voluntary);

            String jsonMessage = objectMapper.writeValueAsString(eventVoluntary);
            kafkaProducer.sendMessage(Topic.ADHESION_EVENTO.getName(), jsonMessage);
            ResponseDTO response = new ResponseDTO(true, "Mensaje enviado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false, e.getMessage()));
        }
    }
}
