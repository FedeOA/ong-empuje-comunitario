package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.Voluntary;

import java.util.List;
import java.util.Optional;

public interface VoluntaryService {

    Optional<Voluntary> findById(Integer id);

    Optional<Voluntary> findByOrganizationIdAndVoluntaryId(Integer organizationId, Integer voluntaryId);

    List<Voluntary> findAll();

    Voluntary createVoluntary(Voluntary voluntary);

    Voluntary updateVoluntary(Voluntary voluntary);

    boolean deleteVoluntary(Integer id);
}