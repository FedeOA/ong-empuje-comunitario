package com.ong.empuje.comunitario.web_services.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ong.empuje.comunitario.web_services.dto.OrganizationDTO;
import com.ong.empuje.comunitario.web_services.dto.PresidentDTO;
import com.ong.empuje.comunitario.web_services.service.SoapClientService;


@RestController
@RequestMapping("/api/soap")
public class SoapController {
    
    private static final Logger logger = LoggerFactory.getLogger(SoapController.class);

    private final SoapClientService soapClientService;

    public SoapController(SoapClientService soapClientService){
        this.soapClientService = soapClientService;
    }


    @PostMapping("/organizations")
    public ResponseEntity<List<OrganizationDTO>> getOrganizations(@RequestBody List<Integer> orgIds) {
        try {
            logger.info("Consultando organizaciones con IDs: {}", orgIds);
            List<OrganizationDTO> organizations = soapClientService.getOrganizations(orgIds);
            return ResponseEntity.ok(organizations);
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage(),e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/presidents")
    public ResponseEntity<List<PresidentDTO>> getPresidents(@RequestBody List<Integer> orgIds) {
        try{
            logger.info("consultando presidentes para IDs: {}",orgIds);
            List<PresidentDTO> presidents = soapClientService.getPresidents(orgIds);
            return ResponseEntity.ok(presidents);
        }catch(Exception e){
            logger.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    

}
