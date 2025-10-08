package com.ong.empuje.comunitario.consumer.repository;

import com.ong.empuje.comunitario.consumer.model.UserEvents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventsRepository extends JpaRepository<UserEvents,Integer> {
}
