package com.ong.empuje.comunitario.consumer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;

@Repository
public interface DonationOfferRepository extends JpaRepository<DonationOffer, Integer>{

    Optional<DonationOffer> findByOfferIdAndOrganizationId(int offerId, int organizationId);
    
    Optional<DonationOffer> findByOfferId(int offerId);
    
    List<DonationOffer> findByAvailableTrueAndExpiresAtAfter(java.time.LocalDateTime date);
}
