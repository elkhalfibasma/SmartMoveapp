package org.example.meteoservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meteo")
public class Meteo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Double temperature;
    
    @Column(name = "precipitation")
    private Double precipitation;
    
    @Column(nullable = false)
    private Double vent;
    
    @Column(nullable = false)
    private String condition;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public Meteo() {}
    
    public Meteo(Double temperature, Double precipitation, Double vent, String condition) {
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.vent = vent;
        this.condition = condition;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
