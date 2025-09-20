package com.grpc.demo.controller;

import com.grpc.demo.dto.out.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grpc.demo.dto.in.LoginRequestDTO;
import com.grpc.demo.service.AuthClient;
import com.grpc.demo.service.authorize.LoginResponse;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthClient auth;
    
    public AuthController(AuthClient auth){
        this.auth = auth;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponse serverResponse = auth.login(loginRequest);
            return ResponseEntity.ok(auth.generateToken(serverResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new LoginResponseDTO(false,"Unexpected Error","Empty","Empty","Empty"));
        }
    }
    
}
