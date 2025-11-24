package org.example.meteoservice.model;

public class MeteoRequest {
    
    private Double temperature;
    private Double precipitation;
    private Double vent;
    private String condition;
    
    public MeteoRequest() {}
    
    public MeteoRequest(Double temperature, Double precipitation, Double vent, String condition) {
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.vent = vent;
        this.condition = condition;
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
}
