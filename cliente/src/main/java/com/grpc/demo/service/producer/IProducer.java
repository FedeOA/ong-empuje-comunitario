package com.grpc.demo.service.producer;

public interface IProducer {
    public void sendMessage(String topic, String message);
}
