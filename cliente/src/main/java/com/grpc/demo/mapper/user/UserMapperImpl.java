package com.grpc.demo.mapper.user;

import com.grpc.demo.dto.out.UserResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements IMapper <User, UserResponseDTO>{

    @Override
    public UserResponseDTO map(User source) {

        return new UserResponseDTO(
                source.getId(),
                source.getUsername(),
                source.getFirstName(),
                source.getLastName(),
                source.getPhone(),
                source.getEmail(),
                source.getRoleId(),
                source.getIsActive()
        );
    }
}
