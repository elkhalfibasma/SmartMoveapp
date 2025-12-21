package org.example.predictionservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.predictionservice.model.WeatherImpactAnalysis;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service dedicated to calculating Weather Impact on road traffic.
 * <p>
 * ACADEMIC JUSTIFICATION:
 * Road traffic speed is physically constrained by adherence (friction) and
 * visibility.
 * - **Rain**: Reduces tire-road friction coefficient (mu) by 20-40%, increasing
 * braking distance and necessitating speed reduction.
 * (Ref: H. Rakha et al., "Impact of Inclement Weather on Freeway Traffic
 * Operations")
 * - **Fog**: Reduces sight distance. Drivers fundamentally reduce speed to
 * maintain a stopping distance within their visual range.
 * (Ref: "Highway Capacity Manual 2010")
 * - **Wind**: High crosswinds destabilize vehicles, especially trucks, forcing
 * general traffic slowing for safety.
 */
@Service
@Service
public class WeatherImpactService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WeatherImpactService.class);

    private static final double BASE_FRICTION_IMPACT_RAIN = 0.10; // 10% reduction in capacity/speed
    private static final double BASE_VISIBILITY_IMPACT_FOG = 0.15; // 15% reduction
    private static final double HEAVY_WEATHER_IMPACT = 0.25; // Significant reduction for storms

    /**
     * Calculates weather impact for a specific departure time using hourly forecast
     * if available.
     * 
     * @param weatherData   Raw weather data from MeteoService (LiveWeatherResponse
     *                      structure)
     * @param departureTime Requested departure time (HH:mm)
     * @return Structured analysis
     */
    public WeatherImpactAnalysis analyzeImpact(Map<String, Object> weatherData, String departureTime) {
        log.info("Analyzing weather impact for time: {}", departureTime);

        // 1. Extract specific weather conditions for the target hour
        WeatherSnaphot snapshot = extractWeatherForHour(weatherData, departureTime);

        // 2. Apply Physics-based Rules
        return calculatePhysicsBasedImpact(snapshot);
    }

    private WeatherImpactAnalysis calculatePhysicsBasedImpact(WeatherSnaphot snapshot) {
        double modifier = 0.0;
        StringBuilder reason = new StringBuilder();

        // Rule 1: Precipitation Impact (Friction Reduction)
        if (snapshot.condition != null) {
            String cond = snapshot.condition.toLowerCase();
            if (cond.contains("rain") || cond.contains("pluie")) {
                modifier += BASE_FRICTION_IMPACT_RAIN; // +10%
                reason.append("Pluie (adhérence réduite). ");
            } else if (cond.contains("storm") || cond.contains("orage")) {
                modifier += HEAVY_WEATHER_IMPACT; // +25%
                reason.append("Tempête (danger élevé). ");
            } else if (cond.contains("snow") || cond.contains("neige")) {
                modifier += 0.30; // +30% (Severe friction loss)
                reason.append("Neige (glissance majeure). ");
            }
        }

        // Rule 2: Visibility Impact (Visual Range reduction)
        // HCM 2010 suggests significant speed drops when visibility < 0.5km
        if (snapshot.visibility < 1000 && snapshot.visibility > 0) {
            double visImpact = (1000 - snapshot.visibility) / 1000.0 * 0.20; // Up to 20%
            modifier += visImpact;
            reason.append(String.format("Visibilité faible (%.1fkm). ", snapshot.visibility / 1000.0));
        } else if (snapshot.hasFog) {
            modifier += BASE_VISIBILITY_IMPACT_FOG;
            reason.append("Brouillard présent. ");
        }

        // Rule 3: Wind Impact (Stability)
        if (snapshot.windSpeed > 50) {
            modifier += 0.05 + ((snapshot.windSpeed - 50) / 100.0 * 0.10);
            reason.append(String.format("Vent fort (%.0f km/h). ", snapshot.windSpeed));
        }

        // Cap modifier to realistic max (e.g., 50% increase max for open road models)
        double finalModifier = Math.min(modifier, 0.50);

        return WeatherImpactAnalysis.builder()
                .impactModifier(finalModifier)
                .impactPercentage((int) Math.round(finalModifier * 100))
                .impactLevel(determineLevel(finalModifier))
                .explanation(reason.length() > 0 ? reason.toString().trim() : "Conditions optimales.")
                .build();
    }

    private String determineLevel(double modifier) {
        if (modifier >= 0.25)
            return "ÉLEVÉ";
        if (modifier >= 0.10)
            return "MOYEN";
        if (modifier > 0)
            return "FAIBLE";
        return "AUCUN";
    }

    // --- Temporal Logic Helper Classes & Methods ---

    private WeatherSnaphot extractWeatherForHour(Map<String, Object> weatherData, String targetTimeStr) {
        if (weatherData == null)
            return new WeatherSnaphot("Unknown", 10000, 0, false);

        try {
            // Target hour
            int targetHour = LocalTime.now().getHour();
            if (targetTimeStr != null && !targetTimeStr.isBlank()) {
                targetHour = LocalTime.parse(targetTimeStr, DateTimeFormatter.ofPattern("HH:mm")).getHour();
            }

            // Check Hourly Forecast availability
            if (weatherData.containsKey("hourlyForecast")) {
                List<Map<String, Object>> hourly = (List<Map<String, Object>>) weatherData.get("hourlyForecast");

                // Find matching hour (closest)
                // Assuming hourly list is sorted or we search for the specific hour match
                for (Map<String, Object> h : hourly) {
                    if (h.containsKey("timestamp")) {
                        LocalDateTime ts = LocalDateTime.parse(getTimestampStr(h.get("timestamp")));
                        if (ts.getHour() == targetHour && ts.getDayOfYear() == LocalDateTime.now().getDayOfYear()) {
                            log.info("Found matching hourly forecast for {}:00", targetHour);
                            return mapSnapshot(h);
                        }
                    }
                }
            }

            // Fallback to Current
            if (weatherData.containsKey("current")) {
                log.info("No hourly match found, using current weather.");
                return mapSnapshot((Map<String, Object>) weatherData.get("current"));
            }

        } catch (Exception e) {
            log.warn("Error parsing hourly weather data: {}", e.getMessage());
        }

        // Fallback default
        return new WeatherSnaphot("Clear", 10000, 10, false);
    }

    private String getTimestampStr(Object tsObj) {
        // Handle potential array format [year, month, day, hour, min]
        if (tsObj instanceof List) {
            List<?> list = (List<?>) tsObj;
            if (list.size() >= 5) {
                return String.format("%04d-%02d-%02dT%02d:%02d:00",
                        list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));
            }
        }
        return tsObj.toString();
    }

    private WeatherSnaphot mapSnapshot(Map<String, Object> data) {
        String cond = (String) data.getOrDefault("condition", "Clear");
        double vis = getDouble(data, "visibility", 10000);
        double wind = getDouble(data, "windSpeed", 0);
        boolean fog = (boolean) data.getOrDefault("hasFog", false);
        return new WeatherSnaphot(cond, vis, wind, fog);
    }

    private double getDouble(Map<String, Object> m, String k, double def) {
        Object v = m.get(k);
        if (v instanceof Number)
            return ((Number) v).doubleValue();
        return def;
    }

    /**
     * Internal DTO for normalized weather data
     */
    private record WeatherSnaphot(String condition, double visibility, double windSpeed, boolean hasFog) {
    }
}
