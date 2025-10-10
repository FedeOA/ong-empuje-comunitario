package com.grpc.demo.service.producer;

import java.util.List;

import com.grpc.demo.dto.producer.DonationOfferItemDTO;
import com.grpc.demo.dto.producer.DonationTransferItemDTO;



public interface IProducer {
    void sendMessage(String topic, String message);
    
    void publicDonationTransfer(int targetOrganizationId, int requestId, List<DonationTransferItemDTO> items);

    void publicDonationOffer(int offerId, List<DonationOfferItemDTO> items);
}
