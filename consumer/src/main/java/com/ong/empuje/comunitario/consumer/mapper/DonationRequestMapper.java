//  consumer\src\main\java\com\ong\empuje\comunitario\consumer\mapper\DonationRequestMapper.java

package com.ong.empuje.comunitario.consumer.mapper;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestItemDTO;
import com.ong.empuje.comunitario.consumer.model.DonationRequest;
import com.ong.empuje.comunitario.consumer.model.DonationRequestItem;

@Mapper(componentModel = "spring")
public interface DonationRequestMapper {
    DonationRequestMapper INSTANCE = Mappers.getMapper(DonationRequestMapper.class);

    @Mapping(source = "requestId", target = "requestId")
    @Mapping(source = "organizationId", target = "organizationId")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(source = "items", target = "items")
    DonationRequest toEntity(DonationRequestDTO dto);

    @Mapping(source = "categoryId", target = "categoryId")
    @Mapping(source = "description", target = "description")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "request", ignore = true)
    DonationRequestItem toEntity(DonationRequestItemDTO itemDto);
}