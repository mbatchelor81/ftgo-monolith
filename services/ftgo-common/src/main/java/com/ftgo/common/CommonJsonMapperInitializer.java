package com.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

/**
 * Initializes the shared {@link ObjectMapper} with common modules and settings.
 */
public class CommonJsonMapperInitializer {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Registers common Jackson modules and disables date-as-timestamp serialization.
     */
    @PostConstruct
    public void initialize() {
        objectMapper.registerModule(new MoneyModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
