package org.example.meteoservice.model;

import java.time.LocalDateTime;
import java.util.List;

public class LiveWeatherResponse {
    
    private CurrentWeather current;
    private List<HourlyForecast> hourlyForecast;
    private List<DailyForecast> dailyForecast;
    
    public static class CurrentWeather {
        private double temperature;
        private double windSpeed;
        private double windDirection;
        private String condition;
        private LocalDateTime timestamp;
        private double visibility;
        private boolean hasFog;
        private String fogIntensity;
        
        // Getters and Setters
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        
        public double getWindDirection() { return windDirection; }
        public void setWindDirection(double windDirection) { this.windDirection = windDirection; }
        
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public double getVisibility() { return visibility; }
        public void setVisibility(double visibility) { this.visibility = visibility; }
        
        public boolean isHasFog() { return hasFog; }
        public void setHasFog(boolean hasFog) { this.hasFog = hasFog; }
        
        public String getFogIntensity() { return fogIntensity; }
        public void setFogIntensity(String fogIntensity) { this.fogIntensity = fogIntensity; }
    }
    
    public static class HourlyForecast {
        private LocalDateTime timestamp;
        private double temperature;
        private double windSpeed;
        private double visibility;
        private boolean hasFog;
        private String fogIntensity;
        
        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        
        public double getVisibility() { return visibility; }
        public void setVisibility(double visibility) { this.visibility = visibility; }
        
        public boolean isHasFog() { return hasFog; }
        public void setHasFog(boolean hasFog) { this.hasFog = hasFog; }
        
        public String getFogIntensity() { return fogIntensity; }
        public void setFogIntensity(String fogIntensity) { this.fogIntensity = fogIntensity; }
    }
    
    public static class DailyForecast {
        private LocalDateTime date;
        private double temperatureMax;
        private double temperatureMin;
        private double precipitation;
        private boolean hasFogExpected;
        private String worstFogIntensity;
        private double worstVisibility;
        
        // Getters and Setters
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
        
        public double getTemperatureMax() { return temperatureMax; }
        public void setTemperatureMax(double temperatureMax) { this.temperatureMax = temperatureMax; }
        
        public double getTemperatureMin() { return temperatureMin; }
        public void setTemperatureMin(double temperatureMin) { this.temperatureMin = temperatureMin; }
        
        public double getPrecipitation() { return precipitation; }
        public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }
        
        public boolean isHasFogExpected() { return hasFogExpected; }
        public void setHasFogExpected(boolean hasFogExpected) { this.hasFogExpected = hasFogExpected; }
        
        public String getWorstFogIntensity() { return worstFogIntensity; }
        public void setWorstFogIntensity(String worstFogIntensity) { this.worstFogIntensity = worstFogIntensity; }
        
        public double getWorstVisibility() { return worstVisibility; }
        public void setWorstVisibility(double worstVisibility) { this.worstVisibility = worstVisibility; }
    }
    
    // Getters and Setters
    public CurrentWeather getCurrent() { return current; }
    public void setCurrent(CurrentWeather current) { this.current = current; }
    
    public List<HourlyForecast> getHourlyForecast() { return hourlyForecast; }
    public void setHourlyForecast(List<HourlyForecast> hourlyForecast) { this.hourlyForecast = hourlyForecast; }
    
    public List<DailyForecast> getDailyForecast() { return dailyForecast; }
    public void setDailyForecast(List<DailyForecast> dailyForecast) { this.dailyForecast = dailyForecast; }
}
