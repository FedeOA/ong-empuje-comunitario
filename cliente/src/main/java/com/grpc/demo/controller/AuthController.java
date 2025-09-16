package com.grpc.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grpc.demo.dto.LoginRequestDTO;
import com.grpc.demo.service.AuthClient;
import com.grpc.demo.service.authorize.LoginResponse;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthClient auth;
    
    public AuthController(AuthClient auth){
        this.auth = auth;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponse response = auth.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                LoginResponse.newBuilder()
                .setSuccess(false)
                .setMessage(e.getMessage())
                .build());
        }
    }
    
}
