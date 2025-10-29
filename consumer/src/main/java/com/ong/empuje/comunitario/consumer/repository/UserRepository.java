package com.ong.empuje.comunitario.consumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ong.empuje.comunitario.consumer.model.User;

public interface UserRepository extends JpaRepository <User,Integer>  {
	java.util.Optional<User> findByEmail(String email);
}
