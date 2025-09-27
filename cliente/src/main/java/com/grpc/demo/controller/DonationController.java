package com.grpc.demo.controller;

import java.util.List;

import com.grpc.demo.dto.in.DonationDTO;
import com.grpc.demo.dto.out.DonationResponseDTO;
import com.grpc.demo.dto.out.ResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.donation.Donation;
import com.grpc.demo.service.donation.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.grpc.demo.service.DonationClient;


@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationClient donationClient;
    private final IMapper<Donation, DonationResponseDTO> mapper;

    public DonationController(DonationClient donationClient, IMapper<Donation, DonationResponseDTO> mapper){
        this.donationClient = donationClient;
        this.mapper = mapper;
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
    
    
}
