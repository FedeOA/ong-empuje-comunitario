package com.ong.empuje.comunitario.web_services.repository;

import com.ong.empuje.comunitario.web_services.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
}