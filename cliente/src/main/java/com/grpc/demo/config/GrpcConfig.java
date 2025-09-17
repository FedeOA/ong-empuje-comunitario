package com.grpc.demo.config;

import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Configuration
public class GrpcConfig {
    
    public ManagedChannel managedChannel(){
        return ManagedChannelBuilder.forAddress("localhost",50051)
            .usePlaintext()
            .build();
    }
}
