package com.ong.empuje.comunitario.web_services.service;

import com.ong.empuje.comunitario.web_services.model.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
}