package com.ong.empuje.comunitario.web_services.service;

import com.ong.empuje.comunitario.web_services.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<Role> findAll();
    Optional<Role> findById(Integer id);
    Optional<Role> findByName(String name);
    Role save(Role role);
    void deleteById(Integer id);
}