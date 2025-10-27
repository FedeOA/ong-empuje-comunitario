package com.grpc.demo.service.producer.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpc.demo.dto.producer.DonationOfferDTO;
import com.grpc.demo.dto.producer.DonationOfferItemDTO;
import com.grpc.demo.dto.producer.DonationTransferDTO;
import com.grpc.demo.dto.producer.DonationTransferItemDTO;
import com.grpc.demo.enums.Topic;
import com.grpc.demo.service.producer.IProducer;

@Service
public class KafkaProducerServiceImpl implements IProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ong.id:1}") // ID de nuestra organización
    private int organizationId;

    public KafkaProducerServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Mensaje enviado: " + message);
    }

    public void publicDonationTransfer(int targetOrganizationId, int requestId, List<DonationTransferItemDTO> items) {
        try{
            DonationTransferDTO transfer = new DonationTransferDTO(requestId, organizationId, items);
            String message = objectMapper.writeValueAsString(transfer);
            String topicTransfer = Topic.TRANSFERENCIA_DONACIONES.getName() +"-"+ targetOrganizationId;
            
            sendMessage(topicTransfer, message);  
        }catch(JsonProcessingException e){
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Error posting donation transfer",e);
        }

    }

    public void publicDonationOffer(int offerId, List<DonationOfferItemDTO> items){
        try{
            DonationOfferDTO offer = new DonationOfferDTO(offerId, organizationId, items);
            String message = objectMapper.writeValueAsString(offer);

            sendMessage(Topic.OFERTA_DONACIONES.getName(), message);
        }catch(JsonProcessingException e){
            System.err.println("Error: "+ e.getMessage());
            throw new RuntimeException("Error postin donation offer",e);
        }
    }
}
