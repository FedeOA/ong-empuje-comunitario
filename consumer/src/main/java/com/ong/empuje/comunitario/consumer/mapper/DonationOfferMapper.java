package com.ong.empuje.comunitario.consumer.mapper;

import com.ong.empuje.comunitario.consumer.dto.in.DonationOfferDTO;
import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DonationOfferMapper {

    @Mapping(source = "offerId", target = "offerId")
    @Mapping(source = "organizationId", target = "organizationId")
    DonationOffer toEntity(DonationOfferDTO donationOfferDTO);
}