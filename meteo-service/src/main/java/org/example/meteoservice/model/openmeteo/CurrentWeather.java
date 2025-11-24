package org.example.meteoservice.model.openmeteo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CurrentWeather {
    
    @JsonProperty("temperature")
    private double temperature;
    
    @JsonProperty("windspeed")
    private double windspeed;
    
    @JsonProperty("winddirection")
    private double winddirection;
    
    @JsonProperty("weathercode")
    private int weathercode;
    
    @JsonProperty("is_day")
    private int isDay;
    
    @JsonProperty("time")
    private String time;

    // Getters and Setters
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    
    public double getWindspeed() { return windspeed; }
    public void setWindspeed(double windspeed) { this.windspeed = windspeed; }
    
    public double getWinddirection() { return winddirection; }
    public void setWinddirection(double winddirection) { this.winddirection = winddirection; }
    
    public int getWeathercode() { return weathercode; }
    public void setWeathercode(int weathercode) { this.weathercode = weathercode; }
    
    public int getIsDay() { return isDay; }
    public void setIsDay(int isDay) { this.isDay = isDay; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
