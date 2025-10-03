package com.ong.empuje.comunitario.consumer.repository;

import com.ong.empuje.comunitario.consumer.model.Voluntary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoluntaryRepository  extends JpaRepository<Voluntary,Integer> {

    Optional<Voluntary> findByOrganizationIdAndVoluntaryId(Integer organizationId, Integer VoluntaryId);
}
