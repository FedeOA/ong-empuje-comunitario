package com.ong.empuje.comunitario.consumer.repository;

import com.ong.empuje.comunitario.consumer.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event,Integer> {
}
