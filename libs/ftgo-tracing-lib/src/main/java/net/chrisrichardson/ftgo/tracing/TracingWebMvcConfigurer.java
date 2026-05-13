package net.chrisrichardson.ftgo.tracing;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class TracingWebMvcConfigurer implements WebMvcConfigurer {

    private final TracingInterceptor tracingInterceptor;

    public TracingWebMvcConfigurer(TracingInterceptor tracingInterceptor) {
        this.tracingInterceptor = tracingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tracingInterceptor);
    }
}
