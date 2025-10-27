package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.VoluntaryEvents;

import java.util.List;
import java.util.Optional;

public interface VoluntaryEventsService {

    Optional<VoluntaryEvents> findById(Integer id);

    List<VoluntaryEvents> findAll();

    VoluntaryEvents createVoluntaryEvents(VoluntaryEvents voluntaryEvents);

    VoluntaryEvents updateVoluntaryEvents(VoluntaryEvents voluntaryEvents);

    boolean deleteVoluntaryEvents(Integer id);
}