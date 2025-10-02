// consumer\src\main\java\com\ong\empuje\comunitario\consumer\service\IConsumer.java

package com.ong.empuje.comunitario.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IConsumer {

    void listenCreateEvents(String message) throws JsonProcessingException;
    
    void listenDonationRequests(String message) throws JsonProcessingException;
    
    void listenDonationCancellations(String message) throws JsonProcessingException;
}