package com.ong.empuje.comunitario.web_services.service;

import com.ong.empuje.comunitario.web_services.model.Role;
import com.ong.empuje.comunitario.web_services.model.User;
import java.util.Optional;
import java.util.List;

public interface UserService {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    List<User> findByRoleAndOrganizationIdIn(Role role, List<Integer> orgIds);
}