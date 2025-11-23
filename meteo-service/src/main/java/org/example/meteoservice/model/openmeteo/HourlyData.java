package org.example.meteoservice.model.openmeteo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class HourlyData {
    
    @JsonProperty("time")
    private List<String> time;
    
    @JsonProperty("temperature_2m")
    private List<Double> temperature2m;
    
    @JsonProperty("windspeed_10m")
    private List<Double> windspeed10m;
    
    @JsonProperty("visibility")
    private List<Double> visibility;
    
    @JsonProperty("weathercode")
    private List<Integer> weathercode;

    // Getters and Setters
    public List<String> getTime() { return time; }
    public void setTime(List<String> time) { this.time = time; }
    
    public List<Double> getTemperature2m() { return temperature2m; }
    public void setTemperature2m(List<Double> temperature2m) { this.temperature2m = temperature2m; }
    
    public List<Double> getWindspeed10m() { return windspeed10m; }
    public void setWindspeed10m(List<Double> windspeed10m) { this.windspeed10m = windspeed10m; }
    
    public List<Double> getVisibility() { return visibility; }
    public void setVisibility(List<Double> visibility) { this.visibility = visibility; }
    
    public List<Integer> getWeathercode() { return weathercode; }
    public void setWeathercode(List<Integer> weathercode) { this.weathercode = weathercode; }
}
