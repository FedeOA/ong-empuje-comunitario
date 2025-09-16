package com.grpc.demo.controller;

import java.util.List;

import com.grpc.demo.service.donation.Donation;
import com.grpc.demo.service.donation.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.grpc.demo.service.DonationClient;




@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "*")
public class DonationController {
    
    private final DonationClient donationClient;

    public DonationController(DonationClient donationClient){
        this.donationClient = donationClient;
    }
    
    @PostMapping
    public ResponseEntity<Response> createDonation(@RequestBody Donation donation) {
        try {
            Response response = donationClient.createDonation(donation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateDonation(@PathVariable int id, @RequestBody Donation donation) {
        try {
            Donation updateDonation = donation.toBuilder().setId(id).build();
            Response response = donationClient.updateDonation(updateDonation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
              Response.newBuilder()
                .setSuccess(false)
                .setMessage(e.getMessage())
                .build()  
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteDonation(@PathVariable int id){
        try {
            Donation donation = Donation.newBuilder().setId(id).build();
            Response response = donationClient.deleteDonation(donation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Donation>> listDonations() {
        try {
            List<Donation> donations = donationClient.listDonations();
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    
}
