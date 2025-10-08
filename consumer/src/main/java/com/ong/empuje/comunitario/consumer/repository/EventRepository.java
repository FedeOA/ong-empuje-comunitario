package com.ong.empuje.comunitario.consumer.repository;

import com.ong.empuje.comunitario.consumer.model.Event;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event,Integer> {
    @Transactional
    void deleteByRemoteIdAndOrganizationId(Integer remoteId, Integer organizationId);

    @Transactional
    Optional<Event> findByRemoteId(Integer remoteId);
}
