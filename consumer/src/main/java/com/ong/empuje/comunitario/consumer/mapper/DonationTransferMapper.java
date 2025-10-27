package com.ong.empuje.comunitario.consumer.mapper;

import com.ong.empuje.comunitario.consumer.dto.in.DonationTransferDTO;
import com.ong.empuje.comunitario.consumer.model.DonationTransfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DonationTransferMapper {
    DonationTransferMapper INSTANCE = Mappers.getMapper(DonationTransferMapper.class);

    @Mapping(source = "organizationId", target = "organizationId")
    DonationTransfer toEntity(DonationTransferDTO donationTransferDTO);
}