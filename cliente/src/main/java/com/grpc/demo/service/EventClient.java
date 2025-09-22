package com.grpc.demo.service;

import java.util.List;

import com.grpc.demo.dto.in.EventDTO;
import com.grpc.demo.service.event.*;
import org.springframework.stereotype.Service;

import com.grpc.demo.exception.GrpcClientException;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class EventClient {
     
    @GrpcClient("event-service")
    private EventServiceGrpc.EventServiceBlockingStub stub;

    public Response createEvent(EventDTO eventReq){
        try {

            Event event =Event
                    .newBuilder()
                    .setName(eventReq.name())
                    .setDescription(eventReq.description())
                    .setFechaHora(eventReq.datetime())
                    .build();

            return stub.createEvent(event);
        } catch (Exception e) {
            throw new GrpcClientException("Error al crear Evento",e);
        }
    }

    public Response updateEvent(int id,EventDTO eventReq){
        try {

            Event event = Event
                    .newBuilder()
                    .setId(id)
                    .setName(eventReq.name())
                    .setDescription(eventReq.description())
                    .setFechaHora(eventReq.datetime().toString())
                    .build();

            return stub.updateEvent(event);
        } catch (Exception e) {
            throw new GrpcClientException("Error al actualizar evento",e);
        }
    }

    public Response deleteEvent(Event event){
        try {
            return stub.deleteEvent(event);
        } catch (Exception e) {
            throw new GrpcClientException("Error al eliminar evento",e);
        }
    }

    public List<Event> listEvents(){
        try {
            EventList list = stub.listEvents(Empty.newBuilder().build());
            return list.getEventList();
        } catch (Exception e) {
            throw new GrpcClientException("Error en la lista de eventos",e);
        }
    }

    public Response addUserToEvent(int eventId, String username){
        try {
            UserEventRequest request = UserEventRequest.newBuilder()
                .setEventId(eventId)
                .setUsername(username)
                .build();
            return stub.addUser(request);
        } catch (Exception e) {
            throw new GrpcClientException("Error para agregar usuario al evento",e);
        }
    }


    public Response removeUserFromEvent(int eventId, String username){
        try {
            UserEventRequest request = UserEventRequest.newBuilder()
                .setEventId(eventId)
                .setUsername(username)
                .build();
            return stub.removeUser(request);
        } catch (Exception e) {
            throw new GrpcClientException("Error para eliminar usuario del evento");
        }
    }

    public Response useDonationsInEvent(int eventId, int donationId){
        try {
            DonationEventRequest request = DonationEventRequest.newBuilder()
                .setEventId(eventId)
                .setDonationId(donationId)
                .build();
            return stub.useDonations(request);
        } catch (Exception e) {
            throw new GrpcClientException("Error al cargar donacion para el evento");
        }
    }

}
