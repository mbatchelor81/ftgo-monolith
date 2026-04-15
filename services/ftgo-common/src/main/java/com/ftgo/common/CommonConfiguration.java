package com.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Spring configuration that provides shared beans for the ftgo-common module. */
@Configuration
@ComponentScan
public class CommonConfiguration {

    /**
     * Creates the shared {@link ObjectMapper} bean.
     *
     * @return a new ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Creates the {@link CommonJsonMapperInitializer} bean.
     *
     * @return a new CommonJsonMapperInitializer instance
     */
    @Bean
    public CommonJsonMapperInitializer commonJsonMapperInitializer() {
        return new CommonJsonMapperInitializer();
    }
}
