package org.example.meteoservice.model;

import java.time.LocalDateTime;

public class MeteoResponse {
    
    private Long id;
    private Double temperature;
    private Double precipitation;
    private Double vent;
    private String condition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public MeteoResponse() {}
    
    public MeteoResponse(Long id, Double temperature, Double precipitation, Double vent, String condition, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.vent = vent;
        this.condition = condition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Double getPrecipitation() {
        return precipitation;
    }
    
    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }
    
    public Double getVent() {
        return vent;
    }
    
    public void setVent(Double vent) {
        this.vent = vent;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
