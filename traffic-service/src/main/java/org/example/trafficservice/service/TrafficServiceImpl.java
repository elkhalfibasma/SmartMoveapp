package org.example.trafficservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.trafficservice.Client.TrafficTomTomClient;
import org.example.trafficservice.model.Traffic;
import org.example.trafficservice.repository.TrafficRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrafficServiceImpl implements TrafficService {

    private final TrafficRepository trafficRepository;
    private final TrafficTomTomClient tomTomClient;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<Traffic> getAllTraffic() {
        return trafficRepository.findAll();
    }

    @Override
    public Traffic saveTraffic(Traffic traffic) {
        return trafficRepository.save(traffic);
    }

    @Override
    public Traffic getTrafficFromTomTom(double latitude, double longitude) {
        String json = tomTomClient.getTomTomTraffic(latitude, longitude);
        Traffic t = new Traffic();
        t.setSource("TomTom API");
        t.setRawResponse(json);

        try {
            kafkaTemplate.send("traffic-updates", "traffic-" + System.currentTimeMillis(), t);
        } catch (Exception e) {
            System.err.println("Failed to publish traffic update: " + e.getMessage());
        }

        return t;
    }

    @Override
    public Map<String, Object> calculateRoute(String origin, String destination) {
        String startCoords = getCoordinates(origin);
        String endCoords = getCoordinates(destination);

        if (startCoords == null || endCoords == null) {
            throw new RuntimeException("Impossible de trouver les coordonnées pour l'une des adresses.");
        }

        String jsonRoute = tomTomClient.getRoute(startCoords, endCoords);
        return parseRouteResponse(jsonRoute);
    }

    private String getCoordinates(String address) {
        try {
            String json = tomTomClient.geocode(address);
            JsonNode root = objectMapper.readTree(json);
            JsonNode position = root.path("results").get(0).path("position");
            return position.path("lat").asText() + "," + position.path("lon").asText();
        } catch (Exception e) {
            System.err.println("Geocoding failed for " + address + ": " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> parseRouteResponse(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode summary = root.path("routes").get(0).path("summary");

            int travelTimeInSeconds = summary.path("travelTimeInSeconds").asInt();
            int lengthInMeters = summary.path("lengthInMeters").asInt();
            int trafficDelayInSeconds = summary.path("trafficDelayInSeconds").asInt();

            result.put("durationMinutes", travelTimeInSeconds / 60);
            result.put("distanceKm", lengthInMeters / 1000.0);
            result.put("trafficDelayMinutes", trafficDelayInSeconds / 60);

            // Determine Risk Level based on traffic delay ratio
            double delayRatio = (double) trafficDelayInSeconds / travelTimeInSeconds;
            String risk = "LOW";
            if (delayRatio > 0.2)
                risk = "HIGH";
            else if (delayRatio > 0.1)
                risk = "MEDIUM";

            result.put("riskLevel", risk);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "Parsing failed");
        }
        return result;
    }
}
