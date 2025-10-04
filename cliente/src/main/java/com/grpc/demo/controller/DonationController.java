package com.grpc.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grpc.demo.dto.in.DonationDTO;
import com.grpc.demo.dto.out.DonationResponseDTO;
import com.grpc.demo.dto.out.ResponseDTO;
import com.grpc.demo.dto.producer.DonationTransferItemDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.DonationClient;
import com.grpc.demo.service.donation.Donation;
import com.grpc.demo.service.donation.Response;
import com.grpc.demo.service.producer.IProducer;


@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationClient donationClient;
    private final IMapper<Donation, DonationResponseDTO> mapper;
    private final IProducer kafkaProducer;

    public DonationController(DonationClient donationClient, IMapper<Donation, DonationResponseDTO> mapper, IProducer kafkaProducer){
        this.donationClient = donationClient;
        this.mapper = mapper;
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping()
    public ResponseEntity<ResponseDTO> createDonation(@RequestBody DonationDTO donation) {
        try {

            Response serverResponse = donationClient.createDonation(donation);

            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO(false,e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO> updateDonation(@PathVariable int id, @RequestBody DonationDTO donation) {
        try {
            Response serverResponse = donationClient.updateDonation(id,donation);
            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage())
            );
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteDonation(@PathVariable int id){
        try {
            Donation donation = Donation.newBuilder().setId(id).build();
            Response serverResponse = donationClient.deleteDonation(donation);
            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage())
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<List<DonationResponseDTO>> listDonations() {
        try {
            List<Donation> serverDonations = donationClient.listDonations();
            List<DonationResponseDTO> donations = mapper.mapList(serverDonations);
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    } 
    
    @PostMapping("/transfer/{targetOrganizationId}/{requestId}")
    public ResponseEntity<ResponseDTO> transferDonation(@PathVariable int targetOrganizationId, @PathVariable int requestId, @RequestBody List<DonationTransferItemDTO> items){
        try {
            //verificamos inventario
            if(!hasInventory(items)){
                return ResponseEntity.badRequest().body(new ResponseDTO(false,"Insufficient inventory"));
            }
            kafkaProducer.publicDonationTransfer(targetOrganizationId, requestId, items);
            return ResponseEntity.ok(new ResponseDTO(
                true,"Transfer made to the organization: " + targetOrganizationId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO(false,"Transfer Error: " + e.getMessage())
            );
        }
    }




    private boolean hasInventory(List<DonationTransferItemDTO> items){
        return true;
    }
}
