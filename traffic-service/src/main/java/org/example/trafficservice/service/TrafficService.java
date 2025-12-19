package org.example.trafficservice.service;

import org.example.trafficservice.model.Traffic;

import java.util.List;
import java.util.Map;

public interface TrafficService {
    List<Traffic> getAllTraffic();

    Traffic saveTraffic(Traffic traffic);

    Traffic getTrafficFromTomTom(double latitude, double longitude);

    Map<String, Object> calculateRoute(String origin, String destination);
}