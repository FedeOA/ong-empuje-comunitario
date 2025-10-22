package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventService {

    Optional<Event> save(Event event);

    List<Event> findAll();

    Optional<Event> findByRemoteId(Integer remoteId);

    Event createEvent(Event event);

    Event updateEvent(Event event);

    boolean deleteEvent(Integer remoteId, Integer organizationId);

    Optional<Event> findById(Integer id);
}