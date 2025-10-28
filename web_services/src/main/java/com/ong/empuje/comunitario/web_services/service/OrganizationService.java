package com.ong.empuje.comunitario.web_services.service;

import com.ong.empuje.comunitario.web_services.model.Organization;
import java.util.List;
import java.util.Optional;

public interface OrganizationService {
    List<Organization> findAll();
    Optional<Organization> findById(Integer id);
    Organization save(Organization org);
    void deleteById(Integer id);
    List<Organization> findAllById(List<Integer> ids);
}