package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.in.EventFilterDTO;
import com.ong.empuje.comunitario.web_services.dto.out.ResponseDTO;
import com.ong.empuje.comunitario.web_services.repository.EventJdbcRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/filter")
public class EventRestController {

    private final EventJdbcRepository eventRepository;

    public EventRestController(EventJdbcRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @PostMapping("/save")
    public ResponseEntity<ResponseDTO> saveFilter(@RequestBody EventFilterDTO eventFilter){

        try {
            eventRepository.saveFilter(eventFilter);
            return ResponseEntity.ok(new ResponseDTO(true,"Success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
