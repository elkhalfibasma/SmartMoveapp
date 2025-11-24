package org.example.meteoservice.model.openmeteo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OpenMeteoResponse {
    
    @JsonProperty("latitude")
    private double latitude;
    
    @JsonProperty("longitude")
    private double longitude;
    
    @JsonProperty("generationtime_ms")
    private double generationtimeMs;
    
    @JsonProperty("utc_offset_seconds")
    private int utcOffsetSeconds;
    
    @JsonProperty("timezone")
    private String timezone;
    
    @JsonProperty("timezone_abbreviation")
    private String timezoneAbbreviation;
    
    @JsonProperty("elevation")
    private double elevation;
    
    @JsonProperty("current_weather")
    private CurrentWeather currentWeather;
    
    @JsonProperty("hourly")
    private HourlyData hourly;
    
    @JsonProperty("daily")
    private DailyData daily;

    // Getters and Setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public double getGenerationtimeMs() { return generationtimeMs; }
    public void setGenerationtimeMs(double generationtimeMs) { this.generationtimeMs = generationtimeMs; }
    
    public int getUtcOffsetSeconds() { return utcOffsetSeconds; }
    public void setUtcOffsetSeconds(int utcOffsetSeconds) { this.utcOffsetSeconds = utcOffsetSeconds; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getTimezoneAbbreviation() { return timezoneAbbreviation; }
    public void setTimezoneAbbreviation(String timezoneAbbreviation) { this.timezoneAbbreviation = timezoneAbbreviation; }
    
    public double getElevation() { return elevation; }
    public void setElevation(double elevation) { this.elevation = elevation; }
    
    public CurrentWeather getCurrentWeather() { return currentWeather; }
    public void setCurrentWeather(CurrentWeather currentWeather) { this.currentWeather = currentWeather; }
    
    public HourlyData getHourly() { return hourly; }
    public void setHourly(HourlyData hourly) { this.hourly = hourly; }
    
    public DailyData getDaily() { return daily; }
    public void setDaily(DailyData daily) { this.daily = daily; }
}
