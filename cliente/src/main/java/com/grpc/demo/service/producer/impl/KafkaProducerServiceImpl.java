package com.grpc.demo.service.producer.impl;

import com.grpc.demo.service.producer.IProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerServiceImpl implements IProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Mensaje enviado: " + message);
    }
}
