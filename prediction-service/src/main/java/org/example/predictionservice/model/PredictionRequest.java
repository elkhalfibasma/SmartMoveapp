package org.example.predictionservice.model;

import lombok.Data;

@Data
public class PredictionRequest {
    private String origin;
    private String destination;
    private String departureDate; // YYYY-MM-DD format
    private String departureTime; // HH:mm format
}
