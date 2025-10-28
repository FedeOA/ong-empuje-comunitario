package com.ong.empuje.comunitario.web_services.service.impl;

import com.ong.empuje.comunitario.web_services.model.User;
import com.ong.empuje.comunitario.web_services.repository.UserRepository;
import com.ong.empuje.comunitario.web_services.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import com.ong.empuje.comunitario.web_services.model.Role;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    @Override
    public List<User> findByRoleAndOrganizationIdIn(Role role, List<Integer> orgIds) {
        return userRepository.findByRoleAndOrganizationIdIn(role, orgIds);
    }
}