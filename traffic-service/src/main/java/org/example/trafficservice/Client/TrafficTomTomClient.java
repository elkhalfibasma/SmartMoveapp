package org.example.trafficservice.Client;

import lombok.RequiredArgsConstructor;
import org.example.trafficservice.model.TrafficConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TrafficTomTomClient {

    private final TrafficConfig config;

    public String getTomTomTraffic(double lat, double lon) {
        String url = "https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json"
                + "?point=" + lat + "," + lon
                + "&key=" + config.getApiKey();

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }
}
