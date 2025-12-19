package org.example.trafficservice.Client;

import lombok.RequiredArgsConstructor;
import org.example.trafficservice.model.TrafficConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TrafficTomTomClient {

    private final TrafficConfig config;
    private final RestTemplate restTemplate;

    public String getTomTomTraffic(double lat, double lon) {
        String url = "https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json"
                + "?point=" + lat + "," + lon
                + "&key=" + config.getApiKey();

        return restTemplate.getForObject(url, String.class);
    }

    public String geocode(String query) {
        String url = "https://api.tomtom.com/search/2/search/" + query + ".json"
                + "?limit=1"
                + "&key=" + config.getApiKey();
        return restTemplate.getForObject(url, String.class);
    }

    public String getRoute(String start, String end) {
        String url = "https://api.tomtom.com/routing/1/calculateRoute/" + start + ":" + end + "/json"
                + "?key=" + config.getApiKey();
        return restTemplate.getForObject(url, String.class);
    }
}
