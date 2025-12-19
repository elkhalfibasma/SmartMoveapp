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
}
