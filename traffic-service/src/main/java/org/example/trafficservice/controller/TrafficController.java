package org.example.trafficservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.trafficservice.model.Traffic;
import org.example.trafficservice.service.TrafficService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficService trafficService;

    @GetMapping
    public List<Traffic> getAllTraffic() {
        return trafficService.getAllTraffic();
    }

    @PostMapping
    public Traffic saveTraffic(@RequestBody Traffic traffic) {
        return trafficService.saveTraffic(traffic);
    }

    // 👉 Endpoint TomTom
    @GetMapping("/tomtom")
    public Traffic getTrafficFromTomTom(
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        return trafficService.getTrafficFromTomTom(lat, lon);
    }
}
