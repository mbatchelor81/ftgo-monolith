package com.ftgo.common.domain.config;

import com.ftgo.common.CommonConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.ftgo.common.domain")
@EntityScan(basePackages = "com.ftgo.common.domain")
@EnableJpaRepositories(basePackages = "com.ftgo.common.domain")
@Import(CommonConfiguration.class)
public class DomainConfiguration {
}
