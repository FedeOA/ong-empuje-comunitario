package com.grpc.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grpc.demo.service.EventClient;
import com.grpc.demo.service.Service.Event;
import com.grpc.demo.service.Service.Response;



@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventClient eventClient;

    public EventController(EventClient eventClient){
        this.eventClient = eventClient;
    }

    @PostMapping
    public ResponseEntity<Response> createEvent(@RequestBody Event event) {
        try {
            Response response = eventClient.createEvent(event);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateEvent(@PathVariable int id, @RequestBody Event event) {
        try {
            Event updateEvent = event.toBuilder().setId(id).build();
            Response response = eventClient.updateEvent(updateEvent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteEvent(@PathVariable int id){
        try {
            Event event = Event.newBuilder().setId(id).build();
            Response response = eventClient.deleteEvent(event);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Event>> listEvents() {
        try {
            List<Event> events = eventClient.listEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{eventId}/users/{userId}")
    public ResponseEntity<Response> addUserToEvent(@PathVariable int eventId, @PathVariable int userId){
        try {
            Response response = eventClient.addUserToEvent(eventId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }

    @DeleteMapping("/{eventId}/users/{userId}")
    public ResponseEntity<Response> removeUserFromEvent(@PathVariable int eventId, @PathVariable int userId){
        try {
            Response response = eventClient.removeUserFromEvent(eventId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }

    @PostMapping("/{eventId}/donations/{donationId}")
    public ResponseEntity<Response> useDonationsInEvent(@PathVariable int eventId, @PathVariable int donationId){
        try {
            Response response = eventClient.useDonationsInEvent(eventId, donationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }     
}
