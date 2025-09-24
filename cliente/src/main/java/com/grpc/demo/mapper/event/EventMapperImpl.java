package com.grpc.demo.mapper.event;

import com.grpc.demo.dto.out.EventResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.event.Event;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventMapperImpl implements IMapper <Event, EventResponseDTO>{
    @Override
    public EventResponseDTO map(Event source) {

        List<String> users = new ArrayList<>();

        for(int i = 0;i<source.getUsersList().size();i++){
            users.add(source.getUsers(i));
        }

        return new EventResponseDTO(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getFechaHora(),
                users
        );
    }
}
