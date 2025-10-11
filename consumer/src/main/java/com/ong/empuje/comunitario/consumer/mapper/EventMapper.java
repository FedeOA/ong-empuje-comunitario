package com.ong.empuje.comunitario.consumer.mapper;

import com.ong.empuje.comunitario.consumer.dto.in.EventDTO;
import com.ong.empuje.comunitario.consumer.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(source = "datetime", target = "datetime", qualifiedByName = "stringToDate")
    @Mapping(source = "eventId", target = "remoteId")
    Event toEntity(EventDTO eventDTO);

    @Named("stringToDate")
    default Date stringToDate(String date) throws Exception {
        if (date == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date);
    }
}
