package org.example.predictionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.predictionservice.model.EnrichedPrediction;
import org.example.predictionservice.model.ImpactFactors;
import org.example.predictionservice.model.WeatherImpactAnalysis;
import org.example.predictionservice.model.Prediction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    private final WeatherImpactService weatherImpactService;

    // Service URLs (via Eureka)
    private static final String TRAFFIC_SERVICE_URL = "http://TRAFFIC-SERVICE/api/traffic/route?origin={origin}&destination={destination}";
    private static final String METEO_SERVICE_COORDS_URL = "http://METEO-SERVICE/api/meteo/live?latitude={lat}&longitude={lon}";
    private static final String INCIDENT_SERVICE_URL = "http://INCIDENT-SERVICE/api/incidents";

    // Peak hours configuration (Morocco time)
    private static final int MORNING_PEAK_START = 7;
    private static final int MORNING_PEAK_END = 9;
    private static final int EVENING_PEAK_START = 17;
    private static final int EVENING_PEAK_END = 20;

    // ========== CASABLANCA DISTANCE MATRIX (km) ==========
    // Realistic distances between Casablanca neighborhoods
    private static final Map<String, Map<String, Double>> CASABLANCA_DISTANCES = new HashMap<>();

    static {
        // Initialize distance matrix for Casablanca neighborhoods
        addDistance("maarif", "casa port", 6.5);
        addDistance("maarif", "technopark", 8.0);
        addDistance("maarif", "ain diab", 5.0);
        addDistance("maarif", "sidi maarouf", 7.5);
        addDistance("maarif", "centre ville", 3.0);
        addDistance("maarif", "anfa", 2.5);
        addDistance("maarif", "bourgogne", 1.5);
        addDistance("maarif", "racine", 1.0);
        addDistance("maarif", "gauthier", 1.2);
        addDistance("maarif", "oasis", 4.0);
        addDistance("maarif", "hay hassani", 6.0);

        addDistance("casa port", "technopark", 12.0);
        addDistance("casa port", "ain diab", 8.0);
        addDistance("casa port", "centre ville", 2.5);
        addDistance("casa port", "sidi maarouf", 14.0);
        addDistance("casa port", "anfa", 4.5);
        addDistance("casa port", "medina", 1.0);

        addDistance("technopark", "ain diab", 10.0);
        addDistance("technopark", "sidi maarouf", 3.0);
        addDistance("technopark", "bouskoura", 5.0);
        addDistance("technopark", "centre ville", 15.0);

        addDistance("ain diab", "anfa", 3.0);
        addDistance("ain diab", "centre ville", 6.0);
        addDistance("ain diab", "corniche", 1.0);

        addDistance("centre ville", "anfa", 2.5);
        addDistance("centre ville", "medina", 2.0);
        addDistance("centre ville", "sidi maarouf", 12.0);

        // Inter-city distances
        addDistance("casablanca", "rabat", 87.0);
        addDistance("casablanca", "marrakech", 240.0);
        addDistance("casablanca", "tanger", 340.0);
        addDistance("casablanca", "fes", 295.0);
        addDistance("casablanca", "agadir", 460.0);
        addDistance("casablanca", "el jadida", 100.0);
        addDistance("casablanca", "mohammedia", 25.0);
        addDistance("rabat", "tanger", 250.0);
        addDistance("rabat", "fes", 200.0);
        addDistance("rabat", "marrakech", 330.0);
        addDistance("marrakech", "agadir", 250.0);
        addDistance("maarif", "rabat", 90.0);
        addDistance("technopark", "rabat", 85.0);
    }

    private static void addDistance(String from, String to, double distance) {
        CASABLANCA_DISTANCES.computeIfAbsent(from.toLowerCase(), k -> new HashMap<>())
                .put(to.toLowerCase(), distance);
        CASABLANCA_DISTANCES.computeIfAbsent(to.toLowerCase(), k -> new HashMap<>())
                .put(from.toLowerCase(), distance);
    }

    // ========== COORDINATES DATABASE ==========
    private static final Map<String, double[]> LOCATION_COORDS = new HashMap<>();

    static {
        // Casablanca neighborhoods
        LOCATION_COORDS.put("maarif", new double[] { 33.5833, -7.6333 });
        LOCATION_COORDS.put("casa port", new double[] { 33.6033, -7.6164 });
        LOCATION_COORDS.put("technopark", new double[] { 33.5167, -7.6500 });
        LOCATION_COORDS.put("ain diab", new double[] { 33.5900, -7.6700 });
        LOCATION_COORDS.put("anfa", new double[] { 33.5783, -7.6481 });
        LOCATION_COORDS.put("centre ville", new double[] { 33.5950, -7.6200 });
        LOCATION_COORDS.put("sidi maarouf", new double[] { 33.5350, -7.6650 });
        LOCATION_COORDS.put("bourgogne", new double[] { 33.5800, -7.6250 });
        LOCATION_COORDS.put("racine", new double[] { 33.5850, -7.6350 });
        LOCATION_COORDS.put("gauthier", new double[] { 33.5870, -7.6280 });
        LOCATION_COORDS.put("oasis", new double[] { 33.5650, -7.6450 });
        LOCATION_COORDS.put("hay hassani", new double[] { 33.5550, -7.6800 });
        LOCATION_COORDS.put("medina", new double[] { 33.6000, -7.6100 });
        LOCATION_COORDS.put("corniche", new double[] { 33.5920, -7.6650 });
        LOCATION_COORDS.put("bouskoura", new double[] { 33.4500, -7.6500 });

        // Major Moroccan cities
        LOCATION_COORDS.put("casablanca", new double[] { 33.5731, -7.5898 });
        LOCATION_COORDS.put("rabat", new double[] { 34.0209, -6.8416 });
        LOCATION_COORDS.put("marrakech", new double[] { 31.6295, -7.9811 });
        LOCATION_COORDS.put("tanger", new double[] { 35.7595, -5.8340 });
        LOCATION_COORDS.put("fes", new double[] { 34.0181, -5.0078 });
        LOCATION_COORDS.put("agadir", new double[] { 30.4278, -9.5981 });
        LOCATION_COORDS.put("el jadida", new double[] { 33.2549, -8.5074 });
        LOCATION_COORDS.put("mohammedia", new double[] { 33.6861, -7.3833 });
    }

    /**
     * Wrapper for prediction using request object
     */
    public EnrichedPrediction predictEnriched(org.example.predictionservice.model.PredictionRequest request) {
        return analyzeEnrichedTrip(
                request.getOrigin(),
                request.getDestination(),
                request.getDepartureDate(),
                request.getDepartureTime(),
                request.getTransportMode());
    }

    /**
     * Main method for enriched trip analysis with all dynamic factors
     */
    public EnrichedPrediction analyzeEnrichedTrip(String origin, String destination, String departureDate,
            String departureTime, String transportMode) { // Added transportMode
        log.info("Analyzing trip: {} -> {} at {} {} via {}", origin, destination, departureDate, departureTime, transportMode);

        // Defaut mode logic
        String mode = (transportMode != null) ? transportMode.toLowerCase() : "driving";

        // Step 1: Get Google/TomTom Base Duration (The "Solid Estimate")
        Map<String, Object> routeData = fetchRouteData(origin, destination, mode);
        double baseDuration = getDoubleValue(routeData, "durationMinutes", 25.0); // Fallback to 25 if fails
        double distanceKm = getDoubleValue(routeData, "distanceKm", 10.0);

        // Step 2: Fetch Context Data (The "Local Factors")
        Map<String, Object> weatherData = fetchWeatherData(origin);
        List<Map<String, Object>> incidents = fetchIncidents();
        boolean isPeakHour = checkPeakHour(departureTime);

        // Step 3: Calculate ML Delta (The "Correction")
        // Delta = WeatherDelta + TrafficDelta + IncidentDelta + PeakDelta
        double weatherDelta = calculateWeatherDelta(weatherData, mode);
        double incidentDelta = calculateIncidentDelta(incidents, mode);
        double peakDelta = calculatePeakDelta(isPeakHour, mode);
        
        // Traffic "Delta" is often included in baseDuration if it's real-time, 
        // but we might want to add a "historical bias" correction for Morocco.
        double biasCorrection = calculateHistoricalBias(baseDuration, mode); 

        double totalDelta = weatherDelta + incidentDelta + peakDelta + biasCorrection;

        // Step 4: Final Prediction
        double predictedDuration = baseDuration + totalDelta;

        // Step 5: Confidence & Intelligence
        double confidence = calculateConfidence(baseDuration, weatherData, incidents, mode);
        String recommendation = generateAIRecommendation(isPeakHour, extractWeatherCondition(weatherData), incidents.size(), departureTime, totalDelta);

        return buildEnrichedPrediction(origin, destination, routeData, weatherData, incidents, isPeakHour,
                departureTime, departureDate, predictedDuration, baseDuration, distanceKm, totalDelta, confidence, recommendation);
    }

    private double calculateWeatherDelta(Map<String, Object> weatherData, String mode) {
         if (mode.equals("walking")) return 0; // Rain doesn't slow walking speed technically, just comfort
         
         String condition = extractWeatherCondition(weatherData).toLowerCase();
         if (condition.contains("rain") || condition.contains("pluie")) return 5.0; // +5 mins for rain
         if (condition.contains("fog") || condition.contains("brouillard")) return 8.0; // +8 mins for fog
         return 0.0;
    }

    private double calculateIncidentDelta(List<Map<String, Object>> incidents, String mode) {
        if (mode.equals("walking") || mode.equals("transit")) return 0; // Assume rail/walking unaffected for MVP
        
        // Simple linear model for accidents
        return incidents.size() * 10.0; // +10 min per reported incident
    }

    private double calculatePeakDelta(boolean isPeakHour, String mode) {
        if (!isPeakHour) return 0.0;
        if (mode.equals("walking")) return 0.0;
        if (mode.equals("transit")) return 15.0; // Bus delays
        return 20.0; // Driving delays in Casablanca peak
    }

    private double calculateHistoricalBias(double baseDuration, String mode) {
        // "SmartMove" learns that estimates in Casablanca are often 10% too optimistic
        if (mode.equals("driving")) return baseDuration * 0.10;
        return 0.0;
    }

    private double calculateConfidence(double baseDuration, Map<String, Object> weatherData, List<Map<String, Object>> incidents, String mode) {
        double score = 0.95; // Start high
        if (weatherData == null) score -= 0.10;
        if (mode.equals("transit")) score -= 0.15; // Transit prediction is harder
        // Normalize 0-1
        return Math.max(0.5, score);
    }
    
    // Updated builder method to accept calculated values
    private EnrichedPrediction buildEnrichedPrediction(
            String origin, String destination,
            Map<String, Object> routeData,
            Map<String, Object> weatherData,
            List<Map<String, Object>> incidents,
            boolean isPeakHour,
            String departureTime,
            String departureDate,
            double predictedDuration,
            double baseDuration,
            double distanceKm,
            double totalDelta,
            double confidence,
            String aiRecommendation) {

        // ... Existing extraction logic ... 
        String weatherCondition = extractWeatherCondition(weatherData); 
        double temperature = extractTemperature(weatherData);
        double visibility = extractVisibility(weatherData);
        double windSpeed = extractWindSpeed(weatherData);
        
        // Calculate percentages for UI
        ImpactFactors impactFactors = calculateImpactFactors(0, 0, 0, 0); // TODO: Refactor to usage
        
        // Generate Details
        List<String> explanationPoints = new ArrayList<>();
        if (totalDelta > 0) explanationPoints.add("⏱️ Retard estimé: +" + Math.round(totalDelta) + " min");
        if (isPeakHour) explanationPoints.add("⏰ Correction Heure de Pointe appliquée");
        
        // Risk
        int riskScore = (int)((totalDelta / baseDuration) * 100);
        String riskLevel = riskScore > 30 ? "HIGH" : (riskScore > 10 ? "MEDIUM" : "LOW");

        return EnrichedPrediction.builder()
                .origin(origin)
                .destination(destination)
                .timestamp(LocalDateTime.now().toString())
                .predictedDuration(Math.round(predictedDuration * 10.0) / 10.0)
                .baseDuration(Math.round(baseDuration * 10.0) / 10.0)
                .distanceKm(Math.round(distanceKm * 10.0) / 10.0)
                .riskLevel(riskLevel)
                .riskScore(riskScore)
                .impactFactors(impactFactors) // Pass simplified or recalculated
                .isPeakHour(isPeakHour)
                .hasIncidents(!incidents.isEmpty())
                .weatherCondition(weatherCondition)
                .trafficCondition("Normal") // Simplify
                .explanationPoints(explanationPoints)
                .aiRecommendation(aiRecommendation)
                .temperature(temperature)
                .visibility(visibility)
                .windSpeed(windSpeed)
                .incidentCount(incidents.size())
                .incidentSeverity(getIncidentSeverity(incidents))
                .durationText(formatDuration(predictedDuration))
                .arrivalTime(calculateArrivalTime(departureTime != null ? departureTime : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), predictedDuration))
                .departureTime(departureTime != null ? departureTime : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
                .routeGeometry((List<Object>) routeData.get("routeGeometry"))
                .confidenceScore(confidence)
                .recommendationOffset(totalDelta > 10 ? totalDelta * 0.4 : 0)
                .build();
    }

    /**
     * Fetch route data from Traffic Service (TomTom integration)
     */
    private Map<String, Object> fetchRouteData(String origin, String destination, String mode) {
        try {
            // TODO: Append mode to Traffic Service URL if supported
            @SuppressWarnings("unchecked")
            Map<String, Object> routeData = restTemplate.getForObject(
                    TRAFFIC_SERVICE_URL, Map.class, origin, destination);

            if (routeData != null && routeData.containsKey("durationMinutes")) {
                log.info("Traffic data received from TomTom: {}", routeData);
                return routeData;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch route data from TomTom: {}", e.getMessage());
        }

        // Use smart fallback with accurate distance matrix
        return createSmartRouteData(origin, destination);
    }

    /**
     * Create smart route data using distance matrix and realistic speed
     * calculations
     */
    private Map<String, Object> createSmartRouteData(String origin, String destination) {
        Map<String, Object> routeData = new HashMap<>();

        // Get realistic distance
        double distance = getRealisticDistance(origin, destination);

        // Calculate duration based on distance and road type
        double baseDuration = calculateRealisticDuration(distance, origin, destination);

        // Add small random traffic variation (0-15% for realism)
        double trafficVariation = random.nextDouble() * 0.15;
        double trafficDelay = baseDuration * trafficVariation;

        routeData.put("distanceKm", Math.round(distance * 10.0) / 10.0);
        routeData.put("durationMinutes", Math.round((baseDuration + trafficDelay) * 10.0) / 10.0);
        routeData.put("trafficDelayMinutes", Math.round(trafficDelay * 10.0) / 10.0);
        routeData.put("riskLevel", trafficDelay > 5 ? "MEDIUM" : "LOW");

        log.info("Smart route data: {} -> {} = {} km, {} min", origin, destination, distance, baseDuration);

        return routeData;
    }

    /**
     * Get realistic distance using the distance matrix or Haversine formula
     */
    private double getRealisticDistance(String origin, String destination) {
        String originLower = normalizeLocation(origin);
        String destLower = normalizeLocation(destination);

        // Check direct match in distance matrix
        if (CASABLANCA_DISTANCES.containsKey(originLower)) {
            Map<String, Double> distances = CASABLANCA_DISTANCES.get(originLower);
            if (distances.containsKey(destLower)) {
                return distances.get(destLower);
            }

            // Try partial match
            for (Map.Entry<String, Double> entry : distances.entrySet()) {
                if (destLower.contains(entry.getKey()) || entry.getKey().contains(destLower)) {
                    return entry.getValue();
                }
            }
        }

        // Try reverse or partial match in any key
        for (Map.Entry<String, Map<String, Double>> outer : CASABLANCA_DISTANCES.entrySet()) {
            if (originLower.contains(outer.getKey()) || outer.getKey().contains(originLower)) {
                for (Map.Entry<String, Double> inner : outer.getValue().entrySet()) {
                    if (destLower.contains(inner.getKey()) || inner.getKey().contains(destLower)) {
                        return inner.getValue();
                    }
                }
            }
        }

        // Use Haversine formula for unknown locations
        double[] originCoords = getCoordinates(origin);
        double[] destCoords = getCoordinates(destination);

        if (originCoords != null && destCoords != null) {
            return haversineDistance(originCoords[0], originCoords[1], destCoords[0], destCoords[1]);
        }

        // Last resort: estimate based on same city or different cities
        if (isSameCityArea(origin, destination)) {
            return 5 + random.nextDouble() * 10; // 5-15 km within city
        }

        return 50 + random.nextDouble() * 50; // 50-100 km between cities
    }

    /**
     * Normalize location name for matching
     */
    private String normalizeLocation(String location) {
        if (location == null)
            return "";
        return location.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Check if two locations are in the same city area
     */
    private boolean isSameCityArea(String origin, String destination) {
        String[] casaNeighborhoods = { "maarif", "technopark", "ain diab", "anfa", "centre", "sidi maarouf",
                "bourgogne", "racine", "gauthier", "oasis", "hay hassani", "medina", "corniche", "port", "casa" };

        String originLower = origin.toLowerCase();
        String destLower = destination.toLowerCase();

        boolean originInCasa = Arrays.stream(casaNeighborhoods).anyMatch(n -> originLower.contains(n));
        boolean destInCasa = Arrays.stream(casaNeighborhoods).anyMatch(n -> destLower.contains(n));

        return originInCasa && destInCasa;
    }

    /**
     * Calculate realistic duration based on distance and road type
     */
    private double calculateRealisticDuration(double distanceKm, String origin, String destination) {
        double averageSpeed;

        if (distanceKm <= 5) {
            // Very short urban trip: 15-20 km/h average (traffic lights, turns)
            averageSpeed = 18;
        } else if (distanceKm <= 15) {
            // Short urban trip: 20-25 km/h average
            averageSpeed = 22;
        } else if (distanceKm <= 30) {
            // Medium urban/suburban: 25-35 km/h average
            averageSpeed = 30;
        } else if (distanceKm <= 100) {
            // Inter-city with some highway: 50-70 km/h average
            averageSpeed = 60;
        } else {
            // Long distance highway: 80-100 km/h average
            averageSpeed = 90;
        }

        // Calculate base duration in minutes
        double durationMinutes = (distanceKm / averageSpeed) * 60;

        // Add minimum 2 minutes for any trip
        return Math.max(2, durationMinutes);
    }

    /**
     * Haversine formula to calculate distance between two coordinates
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Add 30% for road vs straight-line distance
        return R * c * 1.3;
    }

    /**
     * Get coordinates for a location
     */
    private double[] getCoordinates(String location) {
        String normalized = normalizeLocation(location);

        // Direct lookup
        if (LOCATION_COORDS.containsKey(normalized)) {
            return LOCATION_COORDS.get(normalized);
        }

        // Partial match
        for (Map.Entry<String, double[]> entry : LOCATION_COORDS.entrySet()) {
            if (normalized.contains(entry.getKey()) || entry.getKey().contains(normalized)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Fetch weather data from Meteo Service
     */
    private Map<String, Object> fetchWeatherData(String origin) {
        try {
            double[] coords = getCoordinates(origin);
            if (coords == null) {
                coords = new double[] { 33.5731, -7.5898 }; // Default to Casablanca
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> weatherData = restTemplate.getForObject(
                    METEO_SERVICE_COORDS_URL, Map.class, coords[0], coords[1]);

            if (weatherData != null) {
                log.info("Weather data received: {}", weatherData);
                return weatherData;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch weather data: {}", e.getMessage());
        }

        return createMockWeatherData();
    }

    /**
     * Fetch incidents from Incident Service
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchIncidents() {
        try {
            Object response = restTemplate.getForObject(INCIDENT_SERVICE_URL, Object.class);

            if (response instanceof List) {
                log.info("Incidents data received: {} incidents", ((List<?>) response).size());
                return (List<Map<String, Object>>) response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch incidents: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * Check if the departure time is during peak hours
     */
    private boolean checkPeakHour(String departureTime) {
        if (departureTime == null || departureTime.isEmpty()) {
            int currentHour = LocalTime.now().getHour();
            return isPeakHourTime(currentHour);
        }

        try {
            LocalTime time = LocalTime.parse(departureTime, DateTimeFormatter.ofPattern("HH:mm"));
            return isPeakHourTime(time.getHour());
        } catch (Exception e) {
            log.warn("Failed to parse departure time: {}", departureTime);
            return false;
        }
    }

    private boolean isPeakHourTime(int hour) {
        return (hour >= MORNING_PEAK_START && hour <= MORNING_PEAK_END) ||
                (hour >= EVENING_PEAK_START && hour <= EVENING_PEAK_END);
    }

    /**
     * Build the enriched prediction with all factors calculated
     */
    private EnrichedPrediction buildEnrichedPrediction(
            String origin, String destination,
            Map<String, Object> routeData,
            Map<String, Object> weatherData,
            List<Map<String, Object>> incidents,
            boolean isPeakHour,
            String departureTime,
            String departureDate,
            double predictedDuration,
            double baseDuration,
            double distanceKm,
            double totalDelta,
            double confidence,
            String aiRecommendation) {

        // Extract basic data for other parts
        String weatherCondition = extractWeatherCondition(weatherData); 
        double temperature = extractTemperature(weatherData);
        double visibility = extractVisibility(weatherData);
        double windSpeed = extractWindSpeed(weatherData);
        
        // Calculate percentages for UI (Simplified logic based on deltas if possible, or keep existing for now)
        // For MVP, we use the previously calculated "delta" contributors to weight the impact factors
        // Calculate percentages for UI (Simplified logic based on deltas if possible, or keep existing for now)
        // For MVP, we use the previously calculated "delta" contributors to weight the impact factors
        int trafficPct = (int) Math.min(100, Math.max(0, ((totalDelta > 0) ? (totalDelta * 0.4) / baseDuration : 0.1) * 100));
        int weatherPct = (int) Math.min(100, Math.max(0, ((totalDelta > 0) ? (totalDelta * 0.2) / baseDuration : 0.1) * 100));
        int incidentPct = !incidents.isEmpty() ? 30 : 0;
        int peakPct = isPeakHour ? 20 : 0;

        ImpactFactors impactFactors = new ImpactFactors(
            trafficPct,
            weatherPct,
            incidentPct,
            peakPct
        );
        
        // Generate Details
        List<String> explanationPoints = new ArrayList<>();
        if (totalDelta > 0) explanationPoints.add("⏱️ Retard estimé: +" + Math.round(totalDelta) + " min");
        if (isPeakHour) explanationPoints.add("⏰ Correction Heure de Pointe appliquée");
        if (weatherCondition.toLowerCase().contains("rain")) explanationPoints.add("🌧️ Ralentissement pluie (+5 min)");

        // Route Geometry
        List<Object> routeGeometry = null;
        if (routeData != null && routeData.containsKey("routeGeometry")) {
             routeGeometry = (List<Object>) routeData.get("routeGeometry");
        }

        // Calculate arrival time
        String arrivalTime = calculateArrivalTime(departureTime, predictedDuration);

        // Format duration
        String durationText = formatDuration(predictedDuration);
        String trafficCondition = (totalDelta > 10) ? "Dense" : ((totalDelta > 5) ? "Modéré" : "Fluide");

        // Risk
        int riskScore = (int)((totalDelta / baseDuration) * 100);
        String riskLevel = riskScore > 30 ? "HIGH" : (riskScore > 10 ? "MEDIUM" : "LOW");

        return EnrichedPrediction.builder()
                .origin(origin)
                .destination(destination)
                .timestamp(LocalDateTime.now().toString())
                // Ensure precision
                .predictedDuration(Math.round(predictedDuration * 10.0) / 10.0)
                .baseDuration(Math.round(baseDuration * 10.0) / 10.0)
                .distanceKm(Math.round(distanceKm * 10.0) / 10.0)
                .riskLevel(riskLevel)
                .riskScore(riskScore)
                .impactFactors(impactFactors)
                .isPeakHour(isPeakHour)
                .hasIncidents(!incidents.isEmpty())
                .weatherCondition(weatherCondition)
                .trafficCondition(trafficCondition)
                .explanationPoints(explanationPoints)
                .aiRecommendation(aiRecommendation)
                .temperature(temperature)
                .visibility(visibility)
                .windSpeed(windSpeed)
                .incidentCount(incidents.size())
                .incidentSeverity(getIncidentSeverity(incidents))
                .durationText(durationText)
                .arrivalTime(arrivalTime)
                .departureTime(departureTime != null ? departureTime : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
                .routeGeometry(routeGeometry)
                .confidenceScore(confidence)
                .recommendationOffset(totalDelta > 10 ? totalDelta * 0.4 : 0) // Simple rule: avoiding now saves ~40% of delay
                .build();
    }

    /**
     * Format duration as "X h Y min" or "Y min"
     */
    private String formatDuration(double durationMinutes) {
        int totalMinutes = (int) Math.round(durationMinutes);

        if (totalMinutes >= 60) {
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            if (minutes == 0) {
                return hours + " h";
            }
            return hours + " h " + minutes + " min";
        }

        return totalMinutes + " min";
    }

    /**
     * Calculate arrival time based on departure time and duration
     */
    private String calculateArrivalTime(String departureTime, double durationMinutes) {
        try {
            LocalTime departure;
            if (departureTime == null || departureTime.isEmpty()) {
                departure = LocalTime.now();
            } else {
                departure = LocalTime.parse(departureTime, DateTimeFormatter.ofPattern("HH:mm"));
            }

            long totalMinutes = (long) Math.round(durationMinutes);
            LocalTime arrival = departure.plusMinutes(totalMinutes);

            return arrival.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return "--:--";
        }
    }

    /**
     * Calculate impact factors as percentages
     * Always shows meaningful percentages by adding baseline values
     */
    private ImpactFactors calculateImpactFactors(double trafficMod, int weatherPercentage, double incidentMod,
            double peakMod) {

        // Add baseline values so all factors have some representation
        // This makes the display more informative even when some factors are minimal
        double baseTraffic = 0.15; // Traffic always has some base impact
        // Weather is now EXPLICIT passed as percentage (e.g. 15), convert to ratio for
        // total calc if needed
        // But here we want relative weight.

        double baseIncident = 0.05; // Potential for incidents
        double basePeak = 0.07; // Time of day matters

        // Combine actual modifiers with baseline
        double totalTraffic = baseTraffic + trafficMod;
        // Weather input is already a percentage (e.g. 15). We treat it as 0.15 for this
        // weight calculation
        double totalWeather = (weatherPercentage / 100.0) + 0.05; // Add small baseline

        double totalIncident = baseIncident + incidentMod;
        double totalPeak = basePeak + peakMod;

        double total = totalTraffic + totalWeather + totalIncident + totalPeak;

        if (total < 0.01) {
            // Fallback baseline distribution
            return ImpactFactors.builder()
                    .traffic(40)
                    .weather(25)
                    .incidents(15)
                    .peakHour(20)
                    .build();
        }

        int trafficPct = (int) Math.round((totalTraffic / total) * 100);
        int weatherPct = (int) Math.round((totalWeather / total) * 100);
        int incidentPct = (int) Math.round((totalIncident / total) * 100);
        int peakPct = (int) Math.round((totalPeak / total) * 100);

        // Ensure sum is 100
        int sum = trafficPct + weatherPct + incidentPct + peakPct;
        if (sum != 100 && sum > 0) {
            // Adjust the largest value
            int diff = 100 - sum;
            if (trafficPct >= weatherPct && trafficPct >= incidentPct && trafficPct >= peakPct) {
                trafficPct += diff;
            } else if (weatherPct >= trafficPct && weatherPct >= incidentPct && weatherPct >= peakPct) {
                weatherPct += diff;
            } else if (incidentPct >= trafficPct && incidentPct >= weatherPct && incidentPct >= peakPct) {
                incidentPct += diff;
            } else {
                peakPct += diff;
            }
        }

        // Ensure minimum values for visibility
        trafficPct = Math.max(5, trafficPct);
        weatherPct = Math.max(5, weatherPct);
        incidentPct = Math.max(5, incidentPct);
        peakPct = Math.max(5, peakPct);

        // Re-normalize after ensuring minimums
        sum = trafficPct + weatherPct + incidentPct + peakPct;
        if (sum != 100) {
            double scale = 100.0 / sum;
            trafficPct = (int) Math.round(trafficPct * scale);
            weatherPct = (int) Math.round(weatherPct * scale);
            incidentPct = (int) Math.round(incidentPct * scale);
            peakPct = 100 - trafficPct - weatherPct - incidentPct; // Ensure exact 100
        }

        return ImpactFactors.builder()
                .traffic(Math.max(5, trafficPct))
                .weather(Math.max(5, weatherPct))
                .incidents(Math.max(5, incidentPct))
                .peakHour(Math.max(5, peakPct))
                .build();
    }

    // ========== CALCULATION HELPERS ==========

    private double calculateTrafficModifier(double trafficDelay, double baseDuration) {
        if (baseDuration == 0)
            return 0;
        double ratio = trafficDelay / baseDuration;
        return Math.min(ratio, 0.3); // Cap at 30% increase
    }

    private double calculateIncidentModifier(List<Map<String, Object>> incidents) {
        if (incidents == null || incidents.isEmpty())
            return 0.0;

        int count = incidents.size();
        boolean hasMajor = incidents.stream()
                .anyMatch(i -> {
                    Object severity = i.get("severity");
                    return severity != null &&
                            (severity.toString().equalsIgnoreCase("HIGH") ||
                                    severity.toString().equalsIgnoreCase("MAJOR"));
                });

        if (hasMajor)
            return 0.25;
        if (count >= 3)
            return 0.15;
        if (count >= 1)
            return 0.08;
        return 0.0;
    }

    private int calculateRiskScore(double trafficMod, double weatherMod, double incidentMod, boolean isPeakHour) {
        double score = 0;
        score += trafficMod * 80;
        score += weatherMod * 100;
        score += incidentMod * 120;
        score += isPeakHour ? 10 : 0;

        return (int) Math.min(100, Math.max(0, score));
    }

    private String getRiskLevel(int riskScore) {
        if (riskScore >= 50)
            return "HIGH";
        if (riskScore >= 25)
            return "MEDIUM";
        return "LOW";
    }

    // ========== EXPLANATION GENERATORS ==========

    private List<String> generateExplanations(double trafficDelay, String weather, int incidentCount,
            boolean isPeakHour, double visibility) {
        List<String> explanations = new ArrayList<>();

        if (trafficDelay > 10) {
            explanations.add("🚗 Trafic dense détecté (+" + Math.round(trafficDelay) + " min)");
        } else if (trafficDelay > 3) {
            explanations.add("🚗 Trafic modéré (+" + Math.round(trafficDelay) + " min)");
        } else {
            explanations.add("🚗 Trafic fluide");
        }

        if (weather != null && !weather.equalsIgnoreCase("Clear") && !weather.equalsIgnoreCase("Sunny")) {
            explanations.add("🌤️ Météo: " + weather);
        } else {
            explanations.add("🌤️ Météo favorable");
        }

        if (visibility < 3000) {
            explanations.add("👁️ Visibilité réduite (" + Math.round(visibility / 1000.0) + " km)");
        }

        if (incidentCount > 0) {
            explanations.add("⚠️ " + incidentCount + " incident(s) signalé(s)");
        } else {
            explanations.add("✅ Aucun accident signalé");
        }

        if (isPeakHour) {
            explanations.add("⏰ Heure de pointe");
        } else {
            explanations.add("⏰ Heure creuse");
        }

        return explanations;
    }

    private String generateAIRecommendation(boolean isPeakHour, String weather, int incidents,
            String departureTime, double additionalTime) {
        List<String> recommendations = new ArrayList<>();

        if (isPeakHour && additionalTime > 5) {
            recommendations.add("Partir 15-20 minutes plus tard pourrait réduire votre trajet de 20%.");
        } else if (isPeakHour) {
            recommendations.add("Heure de pointe - trafic légèrement ralenti.");
        }

        if (weather != null && (weather.toLowerCase().contains("rain") || weather.toLowerCase().contains("pluie"))) {
            recommendations.add("Pluie signalée - augmentez vos distances de sécurité.");
        }

        if (weather != null
                && (weather.toLowerCase().contains("fog") || weather.toLowerCase().contains("brouillard"))) {
            recommendations.add("Brouillard détecté - utilisez vos feux et roulez prudemment.");
        }

        if (incidents > 1) {
            recommendations.add("Plusieurs incidents signalés - envisagez un itinéraire alternatif.");
        } else if (incidents > 0) {
            recommendations.add("Incident signalé - restez vigilant.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Conditions optimales pour votre trajet. Bonne route!");
        }

        return String.join(" ", recommendations);
    }

    // ========== UTILITY METHODS ==========

    private String generateAIRecommendation(boolean isPeakHour, String weatherCondition, int incidentCount, String time, double totalDelay) {
        StringBuilder rec = new StringBuilder();
        
        if (totalDelay > 15) {
            rec.append("⚠️ Retard significatif prévu. ");
            rec.append("Partir 30 min plus tard réduirait votre trajet de ~").append(Math.round(totalDelay * 0.4)).append(" min.");
        } else if (isPeakHour) {
            rec.append("🕒 Trafic de pointe. Un départ décalé de 20 min est conseillé.");
        } else if (weatherCondition.toLowerCase().contains("rain")) {
            rec.append("🌧️ Chaussée glissante. Réduisez votre vitesse et augmentez les distances de sécurité.");
        } else {
             rec.append("✅ Conditions optimales. Aucune restriction particulière.");
        }
        
        return rec.toString();
    }

    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        if (map == null || !map.containsKey(key))
            return defaultValue;
        Object value = map.get(key);
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String extractWeatherCondition(Map<String, Object> weatherData) {
        if (weatherData == null)
            return "Clear";

        if (weatherData.containsKey("current")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) weatherData.get("current");
            if (current != null && current.containsKey("condition")) {
                return current.get("condition").toString();
            }
        }

        if (weatherData.containsKey("condition")) {
            return weatherData.get("condition").toString();
        }

        return "Clear";
    }

    private double extractTemperature(Map<String, Object> weatherData) {
        if (weatherData == null)
            return 20.0;

        if (weatherData.containsKey("current")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) weatherData.get("current");
            if (current != null && current.containsKey("temperature")) {
                return getDoubleValue(current, "temperature", 20.0);
            }
        }

        return getDoubleValue(weatherData, "temperature", 20.0);
    }

    private double extractVisibility(Map<String, Object> weatherData) {
        if (weatherData == null)
            return 10000;

        if (weatherData.containsKey("current")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) weatherData.get("current");
            if (current != null && current.containsKey("visibility")) {
                return getDoubleValue(current, "visibility", 10000);
            }
        }

        return getDoubleValue(weatherData, "visibility", 10000);
    }

    private double extractWindSpeed(Map<String, Object> weatherData) {
        if (weatherData == null)
            return 0;

        if (weatherData.containsKey("current")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) weatherData.get("current");
            if (current != null && current.containsKey("windSpeed")) {
                return getDoubleValue(current, "windSpeed", 0);
            }
        }

        return getDoubleValue(weatherData, "windSpeed", 0);
    }

    private String getTrafficConditionString(double delay, double baseDuration) {
        if (baseDuration == 0)
            return "Normal";
        double ratio = delay / baseDuration;

        if (ratio > 0.2)
            return "Dense";
        if (ratio > 0.1)
            return "Modéré";
        return "Fluide";
    }

    private String getIncidentSeverity(List<Map<String, Object>> incidents) {
        if (incidents == null || incidents.isEmpty())
            return "NONE";

        boolean hasMajor = incidents.stream()
                .anyMatch(i -> {
                    Object severity = i.get("severity");
                    return severity != null &&
                            (severity.toString().equalsIgnoreCase("HIGH") ||
                                    severity.toString().equalsIgnoreCase("MAJOR"));
                });

        if (hasMajor)
            return "MAJOR";
        return "MINOR";
    }

    private Map<String, Object> createMockWeatherData() {
        Map<String, Object> mock = new HashMap<>();
        Map<String, Object> current = new HashMap<>();

        String[] conditions = { "Clear", "Partly cloudy", "Overcast", "Rain", "Drizzle" };
        int[] weights = { 50, 25, 15, 7, 3 };

        int rand = random.nextInt(100);
        int cumulative = 0;
        String condition = "Clear";
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (rand < cumulative) {
                condition = conditions[i];
                break;
            }
        }

        current.put("condition", condition);
        current.put("temperature", 18 + random.nextDouble() * 12);
        current.put("visibility", 8000 + random.nextInt(2000));
        current.put("windSpeed", random.nextDouble() * 25);

        mock.put("current", current);
        return mock;
    }

    // ========== LEGACY METHODS ==========

    public Prediction analyzeTrip(String origin, String destination) {
        EnrichedPrediction enriched = analyzeEnrichedTrip(origin, destination, null, null);

        Prediction prediction = new Prediction(
                origin,
                destination,
                enriched.getPredictedDuration(),
                enriched.getRiskLevel(),
                enriched.getTimestamp());

        try {
            kafkaTemplate.send("trip-predictions", UUID.randomUUID().toString(), prediction);
        } catch (Exception e) {
            log.warn("Failed to send Kafka message: {}", e.getMessage());
        }

        return prediction;
    }

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void generateMockPrediction() {
        String[] locations = { "Maarif", "Technopark", "Ain Diab", "Sidi Maarouf", "Centre Ville", "Casa Port",
                "Rabat" };
        String origin = locations[random.nextInt(locations.length)];
        String destination = locations[random.nextInt(locations.length)];

        while (origin.equals(destination)) {
            destination = locations[random.nextInt(locations.length)];
        }

        EnrichedPrediction prediction = analyzeEnrichedTrip(origin, destination, null,
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        log.info("Generated: {} -> {} = {} ({})",
                origin, destination, prediction.getDurationText(), prediction.getRiskLevel());

        try {
            kafkaTemplate.send("trip-predictions", UUID.randomUUID().toString(), prediction);
        } catch (Exception e) {
            log.warn("Failed to send Kafka message: {}", e.getMessage());
        }
    }
}
