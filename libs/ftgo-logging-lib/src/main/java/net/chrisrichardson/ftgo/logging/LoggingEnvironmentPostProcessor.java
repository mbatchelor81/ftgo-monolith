package net.chrisrichardson.ftgo.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggingEnvironmentPostProcessor.class);

    public void logStartup(String serviceName) {
        log.info("FTGO Logging initialized for service: {}", serviceName);
    }
}
