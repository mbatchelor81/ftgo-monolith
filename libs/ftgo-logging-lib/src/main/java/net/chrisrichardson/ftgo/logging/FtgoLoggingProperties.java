package net.chrisrichardson.ftgo.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.logging")
public class FtgoLoggingProperties {

    private boolean enabled = true;
    private String serviceName = "ftgo-service";
    private boolean jsonEnabled = true;
    private boolean correlationIdEnabled = true;
    private Logstash logstash = new Logstash();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isJsonEnabled() {
        return jsonEnabled;
    }

    public void setJsonEnabled(boolean jsonEnabled) {
        this.jsonEnabled = jsonEnabled;
    }

    public boolean isCorrelationIdEnabled() {
        return correlationIdEnabled;
    }

    public void setCorrelationIdEnabled(boolean correlationIdEnabled) {
        this.correlationIdEnabled = correlationIdEnabled;
    }

    public Logstash getLogstash() {
        return logstash;
    }

    public void setLogstash(Logstash logstash) {
        this.logstash = logstash;
    }

    public static class Logstash {

        private String destination = "localhost:5000";

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }
    }
}
