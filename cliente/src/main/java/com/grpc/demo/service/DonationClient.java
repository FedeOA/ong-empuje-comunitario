package com.grpc.demo.service;

import java.util.List;

import com.grpc.demo.service.donation.*;
import org.springframework.stereotype.Service;

import com.grpc.demo.exception.GrpcClientException;

import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class DonationClient {

    @GrpcClient("donation-service")
    private DonationServiceGrpc.DonationServiceBlockingStub stub;
    
    public Response createDonation(Donation donation){
        try {
            return stub.createDonation(donation);
        } catch (Exception e) {
            throw new GrpcClientException("Error al ingresar nueva donacion", e);
        }
    }

    public Response updateDonation(Donation donation){
        try {
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
