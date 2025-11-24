package org.example.trafficservice.model;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrafficConfig {

    @Value("${tomtom.api.key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
