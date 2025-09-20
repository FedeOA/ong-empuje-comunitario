package com.grpc.demo.mapper.event;

import com.grpc.demo.dto.out.EventResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements IMapper <Event, EventResponseDTO>{
    @Override
    public EventResponseDTO map(Event source) {

        return new EventResponseDTO(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getFechaHora()
        );
    }
}
