package com.grpc.demo.service;

import com.grpc.demo.dto.out.LoginResponseDTO;
import com.grpc.demo.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import com.grpc.demo.dto.in.LoginRequestDTO;
import com.grpc.demo.exception.GrpcClientException;
import com.grpc.demo.service.authorize.AuthServiceGrpc;
import com.grpc.demo.service.authorize.LoginRequest;
import com.grpc.demo.service.authorize.LoginResponse;

import net.devh.boot.grpc.client.inject.GrpcClient;

import java.util.Date;

import static com.grpc.demo.Constants.Constants.SECRET_KEY_TOKEN;


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

    public LoginResponseDTO generateToken(LoginResponse loginResponse) {

        if (loginResponse.getSuccess()) {

            Role role = Role.fromLevel(loginResponse.getRoleId());
            String authority = role.authority();

            String jwt = Jwts.builder()
                    .setSubject(loginResponse.getUsername())
                    .claim("role", authority)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1 hora
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY_TOKEN.getBytes())
                    .compact();


            return new LoginResponseDTO(loginResponse.getSuccess(),loginResponse.getMessage(),authority,loginResponse.getUsername(),jwt);
        }

        return new LoginResponseDTO(false,loginResponse.getMessage(),"Empty",loginResponse.getUsername(),"Empty");
    }


}
