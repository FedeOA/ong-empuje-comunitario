package com.ong.empuje.comunitario.consumer.mapper;

import com.ong.empuje.comunitario.consumer.model.User;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.ong.empuje.comunitario.consumer.dto.in.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toEntity(UserDTO userDTO);
}