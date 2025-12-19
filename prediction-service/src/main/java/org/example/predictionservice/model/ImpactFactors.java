package org.example.predictionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the impact factors contributing to the prediction.
 * Each factor is a percentage (0-100) representing its contribution.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImpactFactors {
    private int traffic; // Traffic impact percentage
    private int weather; // Weather impact percentage
    private int incidents; // Incidents impact percentage
    private int peakHour; // Peak hour impact percentage
}
