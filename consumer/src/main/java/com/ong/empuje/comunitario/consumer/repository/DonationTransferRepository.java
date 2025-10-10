package com.ong.empuje.comunitario.consumer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ong.empuje.comunitario.consumer.model.DonationTransfer;


@Repository
public interface DonationTransferRepository extends JpaRepository<DonationTransfer, Integer> {
    
    Optional<DonationTransfer> findByTransferIdAndOrganizationId(int transferId, int organizationId);
   
    Optional<DonationTransfer> findByTransferId(int transferId);
}
