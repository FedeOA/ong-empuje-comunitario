package com.ong.empuje.comunitario.consumer.repository;

import com.ong.empuje.comunitario.consumer.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization,Integer> {
}
