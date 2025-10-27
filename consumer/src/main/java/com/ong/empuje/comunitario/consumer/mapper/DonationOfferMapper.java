package com.ong.empuje.comunitario.consumer.mapper;

import com.ong.empuje.comunitario.consumer.dto.in.DonationOfferDTO;
import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DonationOfferMapper {
    DonationOfferMapper INSTANCE = Mappers.getMapper(DonationOfferMapper.class);

    @Mapping(source = "offerId", target = "offerId")
    @Mapping(source = "organizationId", target = "organizationId")
    DonationOffer toEntity(DonationOfferDTO donationOfferDTO);
}