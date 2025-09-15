package com.grpc.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grpc.demo.exception.GrpcClientException;
import com.grpc.demo.service.EventServiceGrpc.EventServiceBlockingStub;
import com.grpc.demo.service.Service.DonationEventRequest;
import com.grpc.demo.service.Service.Empty;
import com.grpc.demo.service.Service.Event;
import com.grpc.demo.service.Service.EventList;
import com.grpc.demo.service.Service.Response;
import com.grpc.demo.service.Service.UserEventRequest;

import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class EventClient {
     
    @GrpcClient("event-sevice")
    private EventServiceBlockingStub stub;

    public Response createEvent(Event event){
        try {
            return stub.createEvent(event);
        } catch (Exception e) {
            throw new GrpcClientException("Error al crear Evento",e);
        }
    }

    public Response updateEvent(Event event){
        try {
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

    public Response addUserToEvent(int eventId, int userId){
        try {
            UserEventRequest request = UserEventRequest.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .build();
            return stub.addUser(request);
        } catch (Exception e) {
            throw new GrpcClientException("Error para agregar usuario al evento",e);
        }
    }


    public Response removeUserFromEvent(int eventId, int userId){
        try {
            UserEventRequest request = UserEventRequest.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
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
