package org.example.meteoservice.model.openmeteo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DailyData {
    
    @JsonProperty("time")
    private List<String> time;
    
    @JsonProperty("temperature_2m_max")
    private List<Double> temperature2mMax;
    
    @JsonProperty("temperature_2m_min")
    private List<Double> temperature2mMin;
    
    @JsonProperty("precipitation_sum")
    private List<Double> precipitationSum;
    
    @JsonProperty("weathercode")
    private List<Integer> weathercode;

    // Getters and Setters
    public List<String> getTime() { return time; }
    public void setTime(List<String> time) { this.time = time; }
    
    public List<Double> getTemperature2mMax() { return temperature2mMax; }
    public void setTemperature2mMax(List<Double> temperature2mMax) { this.temperature2mMax = temperature2mMax; }
    
    public List<Double> getTemperature2mMin() { return temperature2mMin; }
    public void setTemperature2mMin(List<Double> temperature2mMin) { this.temperature2mMin = temperature2mMin; }
    
    public List<Double> getPrecipitationSum() { return precipitationSum; }
    public void setPrecipitationSum(List<Double> precipitationSum) { this.precipitationSum = precipitationSum; }
    
    public List<Integer> getWeathercode() { return weathercode; }
    public void setWeathercode(List<Integer> weathercode) { this.weathercode = weathercode; }
}
