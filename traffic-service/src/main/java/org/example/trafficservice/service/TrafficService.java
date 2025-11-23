package org.example.trafficservice.service;


import org.example.trafficservice.model.Traffic;

import java.util.List;

public interface TrafficService {
    List<Traffic> getAllTraffic();
    Traffic saveTraffic(Traffic traffic);
}