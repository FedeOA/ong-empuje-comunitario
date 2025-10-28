package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.in.OrganizationDTO;
import com.ong.empuje.comunitario.web_services.dto.in.PresidentDTO;
import com.ong.empuje.comunitario.web_services.model.Organization;
import com.ong.empuje.comunitario.web_services.model.Role;
import com.ong.empuje.comunitario.web_services.model.User;
import com.ong.empuje.comunitario.web_services.service.OrganizationService;
import com.ong.empuje.comunitario.web_services.service.RoleService;
import com.ong.empuje.comunitario.web_services.service.UserService;
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

    private final OrganizationService organizationService;
    private final UserService userService;
    private final RoleService roleService;

    public SoapController(OrganizationService organizationService, UserService userService, RoleService roleService) {
        this.organizationService = organizationService;
        this.userService = userService;
        this.roleService = roleService;
    }

    @PostMapping("/organizations")
    public ResponseEntity<List<OrganizationDTO>> getOrganizations(@RequestBody List<Integer> orgIds) {
        List<Organization> orgs = organizationService.findAllById(orgIds);
        List<OrganizationDTO> dtos = orgs.stream()
                .map(o -> new OrganizationDTO(o.getId(), o.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/presidents")
    public ResponseEntity<List<PresidentDTO>> getPresidents(@RequestBody List<Integer> orgIds) {
        Role role = roleService.findByName("PRESIDENTE").orElseThrow(() -> new RuntimeException("Role not found"));
        List<User> presidents = userService.findByRoleAndOrganizationIdIn(role, orgIds);
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