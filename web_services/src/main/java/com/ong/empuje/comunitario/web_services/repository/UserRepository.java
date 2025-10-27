package com.ong.empuje.comunitario.web_services.repository;

import com.ong.empuje.comunitario.web_services.model.Role;
import com.ong.empuje.comunitario.web_services.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);    
    // Add this method to UserRepository
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.organization.id IN :orgIds")
    List<User> findByRoleAndOrganizationIdIn(@Param("role") Role role, @Param("orgIds") List<Integer> orgIds);
}