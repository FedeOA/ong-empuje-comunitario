package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.Event;
import com.ong.empuje.comunitario.consumer.model.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationService {

    Optional<Organization> save(Organization organization);

    Optional<Organization> findById(Integer id);

    List<Organization> findAll();

    Organization createOrganization(Organization organization);

    Organization updateOrganization(Organization organization);

    boolean deleteOrganization(Integer id);
}