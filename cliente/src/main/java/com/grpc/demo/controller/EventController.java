package com.grpc.demo.controller;

import java.util.List;

import com.grpc.demo.dto.in.EventDTO;
import com.grpc.demo.dto.out.EventResponseDTO;
import com.grpc.demo.dto.out.ResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.event.Event;
import com.grpc.demo.service.event.Response;
import org.springframework.http.ResponseEntity;
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
    private final IMapper<Event, EventResponseDTO> mapper;

    public EventController(EventClient eventClient, IMapper<Event, EventResponseDTO> mapper){
        this.eventClient = eventClient;
        this.mapper = mapper;
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
}
