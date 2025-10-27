package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.in.EventFilterDTO;
import com.ong.empuje.comunitario.web_services.dto.out.EventFilterResponseDTO;
import com.ong.empuje.comunitario.web_services.dto.out.ResponseDTO;
import com.ong.empuje.comunitario.web_services.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/filter")
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/save")
    public ResponseEntity<ResponseDTO> saveFilter(@RequestBody EventFilterDTO eventFilter){

        try {
            eventService.saveFilter(eventFilter);
            return ResponseEntity.ok(new ResponseDTO(true,"Success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<EventFilterResponseDTO>> getFilter(@PathVariable String username){
        return ResponseEntity.ok(eventService.getFilters(username));
    }
}
