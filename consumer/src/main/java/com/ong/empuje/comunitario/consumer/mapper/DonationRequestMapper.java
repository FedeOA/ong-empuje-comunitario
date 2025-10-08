// consumer\src\main\java\com\ong\empuje\comunitario\consumer\mapper\DonationRequestMapper.java

package com.ong.empuje.comunitario.consumer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestItemDTO;
import com.ong.empuje.comunitario.consumer.model.DonationRequest;
import com.ong.empuje.comunitario.consumer.model.DonationRequestItem;

@Mapper(componentModel = "spring")
public interface DonationRequestMapper {
    @Mapping(source = "requestId", target = "requestId")
    @Mapping(source = "organizationId", target = "organizationId")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(source = "items", target = "items")
    DonationRequest toEntity(DonationRequestDTO dto);

    @Mapping(source = "categoryId", target = "categoryId")
    @Mapping(source = "description", target = "description")
    DonationRequestItem toEntity(DonationRequestItemDTO itemDto);
}