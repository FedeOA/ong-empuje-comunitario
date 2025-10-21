// web_services/src/main/java/com/ong/empuje/comunitario/web_services/controller/SoapController.java
package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.in.OrganizationDTO;
import com.ong.empuje.comunitario.web_services.dto.in.PresidentDTO;
import com.ong.empuje.comunitario.web_services.model.Organization;
import com.ong.empuje.comunitario.web_services.model.Role;
import com.ong.empuje.comunitario.web_services.model.User;
import com.ong.empuje.comunitario.web_services.repository.OrganizationRepository;
import com.ong.empuje.comunitario.web_services.repository.RoleRepository;
import com.ong.empuje.comunitario.web_services.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/soap")
public class SoapController {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public SoapController(OrganizationRepository organizationRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/organizations")
    public ResponseEntity<List<OrganizationDTO>> getOrganizations(@RequestBody List<Integer> orgIds) {
        List<Organization> orgs = organizationRepository.findAllById(orgIds);
        List<OrganizationDTO> dtos = orgs.stream()
                .map(o -> new OrganizationDTO(o.getId(), o.getName(), null, null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/presidents")
    public ResponseEntity<List<PresidentDTO>> getPresidents(@RequestBody List<Integer> orgIds) {
        Role role = roleRepository.findByName("PRESIDENTE").orElseThrow(() -> new RuntimeException("Role not found"));
        List<User> presidents = userRepository.findByRoleAndOrganizationIdIn(role, orgIds);
        List<PresidentDTO> dtos = presidents.stream()
                .map(u -> new PresidentDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getPhone(),
                        u.getEmail(),
                        u.getRole().getName(),
                        u.getOrganizationId()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}