package com.ong.empuje.comunitario.consumer.mapper;

import com.ong.empuje.comunitario.consumer.dto.in.EventVoluntaryDTO;
import com.ong.empuje.comunitario.consumer.model.VoluntaryEvents;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VoluntaryEventMapper {
    VoluntaryEventMapper INSTANCE = Mappers.getMapper(VoluntaryEventMapper.class);

    VoluntaryEvents toEntity(EventVoluntaryDTO eventVoluntaryDTO);
}