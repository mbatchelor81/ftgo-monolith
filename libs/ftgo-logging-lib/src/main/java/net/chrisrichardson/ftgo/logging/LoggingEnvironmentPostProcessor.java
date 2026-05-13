package net.chrisrichardson.ftgo.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

public class LoggingEnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggingEnvironmentPostProcessor.class);

    private final FtgoLoggingProperties properties;

    public LoggingEnvironmentPostProcessor(FtgoLoggingProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("FTGO Logging initialized for service: {}", properties.getServiceName());
    }
}
