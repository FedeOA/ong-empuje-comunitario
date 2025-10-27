package com.ong.empuje.comunitario.consumer.repository;

import com.ong.empuje.comunitario.consumer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User,Integer>  {
}
