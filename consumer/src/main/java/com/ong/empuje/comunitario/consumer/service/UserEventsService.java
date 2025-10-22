package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.UserEvents;

import java.util.List;
import java.util.Optional;

public interface UserEventsService {

    Optional<UserEvents> findById(Integer id);

    List<UserEvents> findAll();

    UserEvents createUserEvents(UserEvents userEvents);

    UserEvents updateUserEvents(UserEvents userEvents);

    boolean deleteUserEvents(Integer id);
}