package com.ong.empuje.comunitario.consumer.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // Configure deserializer for YY:MM:DD format
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yy:MM:dd")) {
            @Override
            public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser parser, com.fasterxml.jackson.databind.DeserializationContext context) throws java.io.IOException {
                String dateStr = parser.getText();
                try {
                    String[] parts = dateStr.split(":");
                    if (parts.length != 3) {
                        throw new IllegalArgumentException("Invalid date format: " + dateStr);
                    }
                    String year = "20" + parts[0]; // Convert YY to YYYY
                    String formattedDate = String.format("%s-%s-%sT00:00:00", year, parts[1], parts[2]);
                    return LocalDateTime.parse(formattedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                } catch (Exception e) {
                    throw new com.fasterxml.jackson.databind.exc.InvalidFormatException(parser, "Invalid date format: " + dateStr, dateStr, LocalDateTime.class);
                }
            }
        });
        // Configure serializer to output LocalDateTime as YY:MM:DD
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yy:MM:dd")));
        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        return objectMapper;
    }
}
