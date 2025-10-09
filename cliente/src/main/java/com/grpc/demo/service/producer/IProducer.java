package com.grpc.demo.service.producer;

public interface IProducer {
    void sendMessage(String topic, String message);
}
