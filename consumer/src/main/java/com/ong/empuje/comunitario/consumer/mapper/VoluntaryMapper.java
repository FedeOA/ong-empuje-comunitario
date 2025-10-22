package com.ong.empuje.comunitario.consumer.mapper;

import com.ong.empuje.comunitario.consumer.dto.in.VoluntaryDTO;
import com.ong.empuje.comunitario.consumer.model.Voluntary;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VoluntaryMapper {
    VoluntaryMapper INSTANCE = Mappers.getMapper(VoluntaryMapper.class);

    Voluntary toEntity(VoluntaryDTO voluntary);
}
