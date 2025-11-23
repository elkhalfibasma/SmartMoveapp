package org.example.meteoservice.controller;

import org.example.meteoservice.model.MeteoRequest;
import org.example.meteoservice.model.MeteoResponse;
import org.example.meteoservice.model.LiveWeatherResponse;
import org.example.meteoservice.model.FogAnalysis;
import org.example.meteoservice.service.MeteoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meteo")
public class MeteoController {
    
    @Autowired
    private MeteoService meteoService;
    
    @PostMapping("/update")
    public ResponseEntity<MeteoResponse> mettreAJourMeteo(@RequestBody MeteoRequest meteoRequest) {
        MeteoResponse response = meteoService.mettreAJourMeteo(meteoRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/condition")
    public ResponseEntity<MeteoResponse> getCondition() {
        MeteoResponse response = meteoService.getCondition();
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/latest")
    public ResponseEntity<MeteoResponse> getLatestMeteo() {
        MeteoResponse response = meteoService.getLatestMeteo();
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<MeteoResponse>> getHistorique(@RequestParam(defaultValue = "7") int days) {
        List<MeteoResponse> history = meteoService.getHistorique(days);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/condition/{condition}")
    public ResponseEntity<List<MeteoResponse>> getMeteoByCondition(@PathVariable String condition) {
        List<MeteoResponse> meteoList = meteoService.getMeteoByCondition(condition);
        return ResponseEntity.ok(meteoList);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeteo(@PathVariable Long id) {
        meteoService.deleteMeteo(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Meteo Service is running");
    }
    
    @GetMapping("/live")
    public ResponseEntity<LiveWeatherResponse> getLiveWeather(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        
        LiveWeatherResponse liveWeather;
        if (latitude != null && longitude != null) {
            liveWeather = meteoService.getLiveWeather(latitude, longitude);
        } else {
            liveWeather = meteoService.getLiveWeather();
        }
        
        return ResponseEntity.ok(liveWeather);
    }
    
    @GetMapping("/fog")
    public ResponseEntity<FogAnalysis> getFogAnalysis(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String location) {
        
        FogAnalysis fogAnalysis;
        if (latitude != null && longitude != null) {
            String locationName = location != null ? location : "Custom Location";
            fogAnalysis = meteoService.getFogAnalysis(latitude, longitude, locationName);
        } else {
            // Use default coordinates (Morocco)
            fogAnalysis = meteoService.getFogAnalysis(35.0, -5.0, "Default Location (Morocco)");
        }
        
        return ResponseEntity.ok(fogAnalysis);
    }
    
    @GetMapping("/fog/casablanca")
    public ResponseEntity<FogAnalysis> getCasablancaFogAnalysis() {
        FogAnalysis fogAnalysis = meteoService.getFogAnalysis(33.5731, -7.5898, "Casablanca");
        return ResponseEntity.ok(fogAnalysis);
    }
    
    @GetMapping("/fog/sefrou")
    public ResponseEntity<FogAnalysis> getSefrouFogAnalysis() {
        FogAnalysis fogAnalysis = meteoService.getFogAnalysis(33.8424, -4.8775, "Sefrou");
        return ResponseEntity.ok(fogAnalysis);
    }
    
    @GetMapping("/fog/route/casablanca-sefrou")
    public ResponseEntity<FogAnalysis> getRouteFogAnalysis() {
        // Get fog analysis for both cities
        FogAnalysis casablancaFog = meteoService.getFogAnalysis(33.5731, -7.5898, "Casablanca");
        FogAnalysis sefrouFog = meteoService.getFogAnalysis(33.8424, -4.8775, "Sefrou");
        
        // Create combined analysis for the route
        FogAnalysis routeAnalysis = new FogAnalysis();
        routeAnalysis.setLocation("Route: Casablanca → Sefrou");
        routeAnalysis.setLatitude(33.70775); // Midpoint latitude
        routeAnalysis.setLongitude(-6.23365); // Midpoint longitude
        
        // Determine overall safety based on both cities
        boolean routeSafe = casablancaFog.isSafeForTravel() && sefrouFog.isSafeForTravel();
        routeAnalysis.setSafeForTravel(routeSafe);
        
        if (routeSafe) {
            routeAnalysis.setTravelRecommendation("Route is safe for travel. Both Casablanca and Sefrou have good visibility conditions.");
        } else {
            routeAnalysis.setTravelRecommendation("Caution advised for travel route. Check fog conditions at both departure (Casablanca) and destination (Sefrou).");
        }
        
        return ResponseEntity.ok(routeAnalysis);
    }
}
