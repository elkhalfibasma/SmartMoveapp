package org.example.predictionservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitoredTrip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origin;
    private String destination;
    private double originalDuration; // Minutes
    private double lastDuration; // Minutes
    private String userId; // Or deviceId
    private boolean isActive;

    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getUserId() { return userId; }
}
