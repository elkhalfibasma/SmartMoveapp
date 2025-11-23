package org.example.meteoservice.model;

import java.time.LocalDateTime;
import java.util.List;

public class FogAnalysis {
    
    private String location;
    private double latitude;
    private double longitude;
    private FogStatus currentFogStatus;
    private List<FogForecast> hourlyForecast;
    private List<DailyFogSummary> dailyForecast;
    private boolean safeForTravel;
    private String travelRecommendation;
    
    public static class FogStatus {
        private boolean hasFog;
        private String fogIntensity;
        private double visibility; // in meters
        private String condition;
        private LocalDateTime timestamp;
        
        // Getters and Setters
        public boolean isHasFog() { return hasFog; }
        public void setHasFog(boolean hasFog) { this.hasFog = hasFog; }
        
        public String getFogIntensity() { return fogIntensity; }
        public void setFogIntensity(String fogIntensity) { this.fogIntensity = fogIntensity; }
        
        public double getVisibility() { return visibility; }
        public void setVisibility(double visibility) { this.visibility = visibility; }
        
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class FogForecast {
        private LocalDateTime timestamp;
        private boolean hasFog;
        private String fogIntensity;
        private double visibility;
        private String condition;
        private boolean safeForTravel;
        
        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public boolean isHasFog() { return hasFog; }
        public void setHasFog(boolean hasFog) { this.hasFog = hasFog; }
        
        public String getFogIntensity() { return fogIntensity; }
        public void setFogIntensity(String fogIntensity) { this.fogIntensity = fogIntensity; }
        
        public double getVisibility() { return visibility; }
        public void setVisibility(double visibility) { this.visibility = visibility; }
        
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        
        public boolean isSafeForTravel() { return safeForTravel; }
        public void setSafeForTravel(boolean safeForTravel) { this.safeForTravel = safeForTravel; }
    }
    
    public static class DailyFogSummary {
        private LocalDateTime date;
        private boolean hasFogExpected;
        private String worstFogIntensity;
        private double worstVisibility;
        private List<Integer> fogHours;
        private boolean safeForTravel;
        
        // Getters and Setters
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
        
        public boolean isHasFogExpected() { return hasFogExpected; }
        public void setHasFogExpected(boolean hasFogExpected) { this.hasFogExpected = hasFogExpected; }
        
        public String getWorstFogIntensity() { return worstFogIntensity; }
        public void setWorstFogIntensity(String worstFogIntensity) { this.worstFogIntensity = worstFogIntensity; }
        
        public double getWorstVisibility() { return worstVisibility; }
        public void setWorstVisibility(double worstVisibility) { this.worstVisibility = worstVisibility; }
        
        public List<Integer> getFogHours() { return fogHours; }
        public void setFogHours(List<Integer> fogHours) { this.fogHours = fogHours; }
        
        public boolean isSafeForTravel() { return safeForTravel; }
        public void setSafeForTravel(boolean safeForTravel) { this.safeForTravel = safeForTravel; }
    }
    
    // Getters and Setters
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public FogStatus getCurrentFogStatus() { return currentFogStatus; }
    public void setCurrentFogStatus(FogStatus currentFogStatus) { this.currentFogStatus = currentFogStatus; }
    
    public List<FogForecast> getHourlyForecast() { return hourlyForecast; }
    public void setHourlyForecast(List<FogForecast> hourlyForecast) { this.hourlyForecast = hourlyForecast; }
    
    public List<DailyFogSummary> getDailyForecast() { return dailyForecast; }
    public void setDailyForecast(List<DailyFogSummary> dailyForecast) { this.dailyForecast = dailyForecast; }
    
    public boolean isSafeForTravel() { return safeForTravel; }
    public void setSafeForTravel(boolean safeForTravel) { this.safeForTravel = safeForTravel; }
    
    public String getTravelRecommendation() { return travelRecommendation; }
    public void setTravelRecommendation(String travelRecommendation) { this.travelRecommendation = travelRecommendation; }
}
