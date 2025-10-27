package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<User> save(User user);

    Optional<User> findById(Integer id);

    List<User> findAll();

    User createUser(User user);

    User updateUser(User user);

    boolean deleteUser(Integer id);
}