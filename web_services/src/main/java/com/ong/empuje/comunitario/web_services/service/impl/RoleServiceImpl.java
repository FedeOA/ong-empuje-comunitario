package com.ong.empuje.comunitario.web_services.service.impl;

import com.ong.empuje.comunitario.web_services.model.Role;
import com.ong.empuje.comunitario.web_services.repository.RoleRepository;
import com.ong.empuje.comunitario.web_services.service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository repository;

    public List<Role> findAll() { 
        return repository.findAll(); 
    }

    public Optional<Role> findById(Integer id) { 
        return repository.findById(id); 
    }

    public Optional<Role> findByName(String name) { 
        return repository.findByName(name); 
    }

    public Role save(Role role) { 
        return repository.save(role); 
    }

    public void deleteById(Integer id) {
        repository.deleteById(id); 
    }
}