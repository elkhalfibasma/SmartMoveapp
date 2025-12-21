package org.example.predictionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Enriched prediction response with complete analysis data.
 * Includes impact factors, explanations, and AI recommendations.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrichedPrediction {

    // Basic trip info
    private String origin;
    private String destination;
    private String timestamp;

    // Duration data
    private double predictedDuration; // Final calculated duration in minutes
    private double baseDuration; // Base duration in minutes
    private double distanceKm; // Distance in kilometers
    private String durationText; // Formatted duration "X h Y min" or "Y min"

    // Time info
    private String departureTime; // HH:mm format
    private String arrivalTime; // HH:mm format (calculated)

    // Risk assessment
    private String riskLevel; // LOW, MEDIUM, HIGH
    private int riskScore; // 0-100 score

    // Impact factors
    private ImpactFactors impactFactors;

    // Condition flags
    private boolean isPeakHour;
    private boolean hasIncidents;
    private String weatherCondition;
    private String trafficCondition;

    // Explanations
    private List<String> explanationPoints;
    private String aiRecommendation;

    // Additional weather details
    private Double temperature;
    private Double visibility;
    private Double windSpeed;

    // Incident details
    private int incidentCount;
    private String incidentSeverity;

    // SmartMove Intelligence
    private double confidenceScore; // 0.0 to 1.0
    private double recommendationOffset; // Minutes saved by following advice

    // Route Geometry
    private List<Object> routeGeometry; // List of {lat, lon} points

    // Alternatives
    private RouteOption recommendedRoute;

    public double getPredictedDuration() { return predictedDuration; }
    public String getRiskLevel() { return riskLevel; }
    public String getTimestamp() { return timestamp; }
    public String getDurationText() { return durationText; }
    public String getAiRecommendation() { return aiRecommendation; }
    public double getConfidenceScore() { return confidenceScore; }
    // Manual Builder
    public static EnrichedPredictionBuilder builder() {
        return new EnrichedPredictionBuilder();
    }

    public static class EnrichedPredictionBuilder {
        private EnrichedPrediction instance = new EnrichedPrediction();

        public EnrichedPredictionBuilder origin(String origin) { instance.origin = origin; return this; }
        public EnrichedPredictionBuilder destination(String destination) { instance.destination = destination; return this; }
        public EnrichedPredictionBuilder timestamp(String timestamp) { instance.timestamp = timestamp; return this; }
        public EnrichedPredictionBuilder predictedDuration(double predictedDuration) { instance.predictedDuration = predictedDuration; return this; }
        public EnrichedPredictionBuilder baseDuration(double baseDuration) { instance.baseDuration = baseDuration; return this; }
        public EnrichedPredictionBuilder distanceKm(double distanceKm) { instance.distanceKm = distanceKm; return this; }
        public EnrichedPredictionBuilder durationText(String durationText) { instance.durationText = durationText; return this; }
        public EnrichedPredictionBuilder departureTime(String departureTime) { instance.departureTime = departureTime; return this; }
        public EnrichedPredictionBuilder arrivalTime(String arrivalTime) { instance.arrivalTime = arrivalTime; return this; }
        public EnrichedPredictionBuilder riskLevel(String riskLevel) { instance.riskLevel = riskLevel; return this; }
        public EnrichedPredictionBuilder riskScore(int riskScore) { instance.riskScore = riskScore; return this; }
        public EnrichedPredictionBuilder impactFactors(ImpactFactors impactFactors) { instance.impactFactors = impactFactors; return this; }
        public EnrichedPredictionBuilder isPeakHour(boolean isPeakHour) { instance.isPeakHour = isPeakHour; return this; }
        public EnrichedPredictionBuilder hasIncidents(boolean hasIncidents) { instance.hasIncidents = hasIncidents; return this; }
        public EnrichedPredictionBuilder weatherCondition(String weatherCondition) { instance.weatherCondition = weatherCondition; return this; }
        public EnrichedPredictionBuilder trafficCondition(String trafficCondition) { instance.trafficCondition = trafficCondition; return this; }
        public EnrichedPredictionBuilder explanationPoints(List<String> explanationPoints) { instance.explanationPoints = explanationPoints; return this; }
        public EnrichedPredictionBuilder aiRecommendation(String aiRecommendation) { instance.aiRecommendation = aiRecommendation; return this; }
        public EnrichedPredictionBuilder temperature(Double temperature) { instance.temperature = temperature; return this; }
        public EnrichedPredictionBuilder visibility(Double visibility) { instance.visibility = visibility; return this; }
        public EnrichedPredictionBuilder windSpeed(Double windSpeed) { instance.windSpeed = windSpeed; return this; }
        public EnrichedPredictionBuilder incidentCount(int incidentCount) { instance.incidentCount = incidentCount; return this; }
        public EnrichedPredictionBuilder incidentSeverity(String incidentSeverity) { instance.incidentSeverity = incidentSeverity; return this; }
        public EnrichedPredictionBuilder confidenceScore(double confidenceScore) { instance.confidenceScore = confidenceScore; return this; }
        public EnrichedPredictionBuilder recommendationOffset(double recommendationOffset) { instance.recommendationOffset = recommendationOffset; return this; }
        public EnrichedPredictionBuilder routeGeometry(List<Object> routeGeometry) { instance.routeGeometry = routeGeometry; return this; }
        public EnrichedPredictionBuilder recommendedRoute(RouteOption recommendedRoute) { instance.recommendedRoute = recommendedRoute; return this; }

        public EnrichedPrediction build() {
            return instance;
        }
    }
}
