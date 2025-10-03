package com.ong.empuje.comunitario.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IConsumer {

    void listenCreateEvents(String message) throws JsonProcessingException;
    void listenDeleteEvents(String message);
    void listenAddVoluntary(String message);
}
