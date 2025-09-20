package com.grpc.demo.service;

import java.util.List;

import com.grpc.demo.dto.in.DonationDTO;
import com.grpc.demo.service.donation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.grpc.demo.exception.GrpcClientException;

import net.devh.boot.grpc.client.inject.GrpcClient;

import static com.grpc.demo.enums.Category.idFromName;

@Service
public class DonationClient {

    @GrpcClient("donation-service")
    private DonationServiceGrpc.DonationServiceBlockingStub stub;

    public Response createDonation(DonationDTO donationReq){
        try {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            Donation donation = Donation.
                    newBuilder()
                    .setCantidad(donationReq.quantity())
                    .setDescription(donationReq.description())
                    .setCategoria(idFromName(donationReq.category()))
                    .setUsername(username)
                    .build();

            return stub.createDonation(donation);
        } catch (Exception e) {
            throw new GrpcClientException("Error al ingresar nueva donacion", e);
        }
    }

    public Response updateDonation(int id,DonationDTO donationReq){
        try {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            Donation donation = Donation
                    .newBuilder()
                    .setId(id)
                    .setDescription(donationReq.description())
                    .setCantidad(donationReq.quantity())
                    .setUsername(username)
                    .build();

            return stub.updateDonation(donation);
        } catch (Exception e) {
            throw new GrpcClientException("Error al modificar donacion", e);
        }
    }

    public Response deleteDonation(Donation donation){
        try{
            return stub.deleteDonation(donation);
        }catch(Exception e){
            throw new GrpcClientException("Error al eliminar donacion",e);
        }
    }

    public List<Donation> listDonations(){
        try{
            DonationList list = stub.listDonations(Empty.newBuilder().build());
            return list.getDonationList();
        }catch(Exception e){
            throw new GrpcClientException("Error con el listado de donaciones", e);
        }
    }
}
