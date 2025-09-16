package com.grpc.demo.service;

import org.springframework.stereotype.Service;

import com.grpc.demo.dto.LoginRequestDTO;
import com.grpc.demo.exception.GrpcClientException;
import com.grpc.demo.service.authorize.AuthServiceGrpc;
import com.grpc.demo.service.authorize.LoginRequest;
import com.grpc.demo.service.authorize.LoginResponse;

import net.devh.boot.grpc.client.inject.GrpcClient;



@Service
public class AuthClient {
 
    @GrpcClient("auth-service")
    private AuthServiceGrpc.AuthServiceBlockingStub stub;

    public LoginResponse login(LoginRequestDTO loginRequest){
        try {
            LoginRequest request = LoginRequest.newBuilder()
                .setUsernameOrEmail(loginRequest.getUsernameOrEmail())
                .setPassword(loginRequest.getPassword())
                .build();
            return stub.login(request);
        } catch (Exception e) {
            throw new GrpcClientException("Error login",e);
        }
    }

}
