package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonationOfferService {

    Optional<DonationOffer> save(DonationOffer donationOffer);

    Optional<DonationOffer> findById(Integer id);

    List<DonationOffer> findAll();

    Optional<DonationOffer> findByOfferIdAndOrganizationId(int offerId, int organizationId);

    Optional<DonationOffer> findByOfferId(int offerId);

    List<DonationOffer> findByAvailableTrueAndExpiresAtAfter(LocalDateTime date);

    DonationOffer createDonationOffer(DonationOffer donationOffer);

    DonationOffer updateDonationOffer(DonationOffer donationOffer);

    boolean deleteDonationOffer(int offerId, int organizationId);
}