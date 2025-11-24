package org.example.meteoservice.client;

import org.example.meteoservice.model.openmeteo.OpenMeteoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WeatherClient {
    
    private final RestTemplate restTemplate;
    private final String openMeteoUrl;
    
    public WeatherClient(RestTemplate restTemplate, 
                        @Value("${openmeteo.api.url:https://api.open-meteo.com/v1/forecast}") String openMeteoUrl) {
        this.restTemplate = restTemplate;
        this.openMeteoUrl = openMeteoUrl;
    }
    
    public OpenMeteoResponse getWeatherData(double latitude, double longitude) {
        String url = UriComponentsBuilder.fromHttpUrl(openMeteoUrl)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("current_weather", "true")
                .queryParam("hourly", "temperature_2m,windspeed_10m,visibility,weathercode")
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum,weathercode")
                .queryParam("timezone", "auto")
                .build()
                .toUriString();
        
        return restTemplate.getForObject(url, OpenMeteoResponse.class);
    }
}
