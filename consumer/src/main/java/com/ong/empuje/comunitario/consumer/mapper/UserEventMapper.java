package com.ong.empuje.comunitario.consumer.mapper;

import com.ong.empuje.comunitario.consumer.dto.in.UserEventDTO;
import com.ong.empuje.comunitario.consumer.model.UserEvents;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class UserEventMapper {
    public static final UserEventMapper INSTANCE = Mappers.getMapper(UserEventMapper.class);

    @Mapping(target = "registrationDate", expression = "java(new java.util.Date())")
    public abstract UserEvents toEntity(UserEventDTO userEventDTO);
}