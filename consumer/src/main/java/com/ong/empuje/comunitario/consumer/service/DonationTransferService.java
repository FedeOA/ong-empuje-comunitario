package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.DonationTransfer;

import java.util.List;
import java.util.Optional;

public interface DonationTransferService {

    Optional<DonationTransfer> save(DonationTransfer donationTransfer);

    Optional<DonationTransfer> findById(Integer id);

    List<DonationTransfer> findAll();
    
    Optional<DonationTransfer> findByTransferIdAndOrganizationId(int transferId, int organizationId);

    Optional<DonationTransfer> findByTransferId(int transferId);

    Optional<DonationTransfer> findById(int id);

    DonationTransfer createDonationTransfer(DonationTransfer donationTransfer);

    DonationTransfer updateDonationTransfer(DonationTransfer donationTransfer);

    boolean deleteDonationTransfer(int transferId, int organizationId);
}