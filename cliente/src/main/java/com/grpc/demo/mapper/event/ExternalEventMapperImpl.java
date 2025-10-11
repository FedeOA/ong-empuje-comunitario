package com.grpc.demo.mapper.event;

import com.grpc.demo.dto.out.ExternalEventResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.event.ExternalEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalEventMapperImpl implements IMapper  <ExternalEvent, ExternalEventResponseDTO> {

    @Override
    public ExternalEventResponseDTO map(ExternalEvent source) {

        List<String> users = new ArrayList<>();

        for(int i = 0;i<source.getUsersList().size();i++){
            users.add(source.getUsers(i));
        }

        return new ExternalEventResponseDTO(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getFechaHora(),
                source.getIsPublished(),
                source.getRemoteId(),
                source.getOrganizationId(),
                users
        );
    }
}
