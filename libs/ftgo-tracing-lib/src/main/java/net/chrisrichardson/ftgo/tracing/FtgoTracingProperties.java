package net.chrisrichardson.ftgo.tracing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.tracing")
public class FtgoTracingProperties {

    private boolean enabled = true;
    private String serviceName = "ftgo-service";
    private float samplingProbability = 1.0f;
    private Zipkin zipkin = new Zipkin();
    private Propagation propagation = new Propagation();

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

    public float getSamplingProbability() {
        return samplingProbability;
    }

    public void setSamplingProbability(float samplingProbability) {
        this.samplingProbability = samplingProbability;
    }

    public Zipkin getZipkin() {
        return zipkin;
    }

    public void setZipkin(Zipkin zipkin) {
        this.zipkin = zipkin;
    }

    public Propagation getPropagation() {
        return propagation;
    }

    public void setPropagation(Propagation propagation) {
        this.propagation = propagation;
    }

    public static class Zipkin {

        private String endpoint = "http://localhost:9411/api/v2/spans";

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    public static class Propagation {

        private PropagationType type = PropagationType.W3C;

        public PropagationType getType() {
            return type;
        }

        public void setType(PropagationType type) {
            this.type = type;
        }
    }

    public enum PropagationType {
        W3C, B3
    }
}
