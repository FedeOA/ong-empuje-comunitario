package com.ong.empuje.comunitario.web_services.service.impl;

import com.ong.empuje.comunitario.web_services.model.Organization;
import com.ong.empuje.comunitario.web_services.repository.OrganizationRepository;
import com.ong.empuje.comunitario.web_services.service.OrganizationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private OrganizationRepository repository;

    public List<Organization> findAll() { 
        return repository.findAll(); 
    }

    public Optional<Organization> findById(Integer id) {
        return repository.findById(id); 
    }

    public Organization save(Organization org) { 
        return repository.save(org); 
    }

    public void deleteById(Integer id) { 
        repository.deleteById(id); 
    }

    @Override
    public List<Organization> findAllById(List<Integer> ids) {
        return repository.findAllById(ids);
    }
}