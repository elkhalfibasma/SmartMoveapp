package org.example.meteoservice.service;

import org.example.meteoservice.entity.Meteo;
import org.example.meteoservice.model.MeteoRequest;
import org.example.meteoservice.model.MeteoResponse;
import org.example.meteoservice.model.LiveWeatherResponse;
import org.example.meteoservice.model.FogAnalysis;
import org.example.meteoservice.model.openmeteo.OpenMeteoResponse;
import org.example.meteoservice.repository.MeteoRepository;
import org.example.meteoservice.client.WeatherClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MeteoService {
    
    @Autowired
    private MeteoRepository meteoRepository;
    
    @Autowired
    private WeatherClient weatherClient;
    
    @Value("${weather.default.latitude:35.0}")
    private double defaultLatitude;
    
    @Value("${weather.default.longitude:-5.0}")
    private double defaultLongitude;

    public MeteoResponse mettreAJourMeteo(MeteoRequest meteoRequest) {
        Meteo meteo = new Meteo();
        meteo.setTemperature(meteoRequest.getTemperature());
        meteo.setPrecipitation(meteoRequest.getPrecipitation());
        meteo.setVent(meteoRequest.getVent());
        meteo.setCondition(meteoRequest.getCondition());
        
        Meteo savedMeteo = meteoRepository.save(meteo);
        
        return convertToResponse(savedMeteo);
    }
    
    public MeteoResponse getCondition() {
        Optional<Meteo> latestMeteo = meteoRepository.findTopByOrderByCreatedAtDesc();
        return latestMeteo.map(this::convertToResponse).orElse(null);
    }
    
    public List<MeteoResponse> getHistorique(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Meteo> meteoHistory = meteoRepository.findRecentMeteo(since);
        return meteoHistory.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<MeteoResponse> getMeteoByCondition(String condition) {
        List<Meteo> meteoList = meteoRepository.findByConditionOrderByCreatedAtDesc(condition);
        return meteoList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public MeteoResponse getLatestMeteo() {
        Optional<Meteo> latestMeteo = meteoRepository.findTopByOrderByCreatedAtDesc();
        return latestMeteo.map(this::convertToResponse).orElse(null);
    }
    
    public void deleteMeteo(Long id) {
        meteoRepository.deleteById(id);
    }
    
    private MeteoResponse convertToResponse(Meteo meteo) {
        MeteoResponse response = new MeteoResponse();
        response.setId(meteo.getId());
        response.setTemperature(meteo.getTemperature());
        response.setPrecipitation(meteo.getPrecipitation());
        response.setVent(meteo.getVent());
        response.setCondition(meteo.getCondition());
        response.setCreatedAt(meteo.getCreatedAt());
        response.setUpdatedAt(meteo.getUpdatedAt());
        return response;
    }
    
    public LiveWeatherResponse getLiveWeather() {
        OpenMeteoResponse openMeteoResponse = weatherClient.getWeatherData(defaultLatitude, defaultLongitude);
        return mapToLiveWeatherResponse(openMeteoResponse);
    }
    
    public LiveWeatherResponse getLiveWeather(double latitude, double longitude) {
        OpenMeteoResponse openMeteoResponse = weatherClient.getWeatherData(latitude, longitude);
        return mapToLiveWeatherResponse(openMeteoResponse);
    }
    
    private LiveWeatherResponse mapToLiveWeatherResponse(OpenMeteoResponse response) {
        LiveWeatherResponse liveWeather = new LiveWeatherResponse();
        
        // Map current weather
        if (response.getCurrentWeather() != null) {
            LiveWeatherResponse.CurrentWeather current = new LiveWeatherResponse.CurrentWeather();
            current.setTemperature(response.getCurrentWeather().getTemperature());
            current.setWindSpeed(response.getCurrentWeather().getWindspeed());
            current.setWindDirection(response.getCurrentWeather().getWinddirection());
            current.setCondition(mapWeatherCodeToCondition(response.getCurrentWeather().getWeathercode()));
            current.setTimestamp(parseDateTime(response.getCurrentWeather().getTime()));
            
            // Add visibility and fog data
            current.setVisibility(getVisibilityFromHourly(response.getHourly(), 0));
            current.setHasFog(isFogCondition(response.getCurrentWeather().getWeathercode()));
            current.setFogIntensity(getFogIntensity(response.getCurrentWeather().getWeathercode()));
            
            liveWeather.setCurrent(current);
            
            // Save current weather to database
            saveCurrentWeatherToDatabase(current);
        }
        
        // Map hourly forecast (48 hours)
        if (response.getHourly() != null && response.getHourly().getTime() != null) {
            List<LiveWeatherResponse.HourlyForecast> hourlyForecasts = new ArrayList<>();
            int hoursToMap = Math.min(48, response.getHourly().getTime().size());
            
            for (int i = 0; i < hoursToMap; i++) {
                LiveWeatherResponse.HourlyForecast hourly = new LiveWeatherResponse.HourlyForecast();
                hourly.setTimestamp(parseDateTime(response.getHourly().getTime().get(i)));
                hourly.setTemperature(response.getHourly().getTemperature2m().get(i));
                hourly.setWindSpeed(response.getHourly().getWindspeed10m().get(i));
                
                // Add visibility and fog data
                hourly.setVisibility(getVisibilityFromHourly(response.getHourly(), i));
                int weatherCode = response.getHourly().getWeathercode().get(i);
                hourly.setHasFog(isFogCondition(weatherCode));
                hourly.setFogIntensity(getFogIntensity(weatherCode));
                
                hourlyForecasts.add(hourly);
            }
            liveWeather.setHourlyForecast(hourlyForecasts);
        }
        
        // Map daily forecast (7 days)
        if (response.getDaily() != null && response.getDaily().getTime() != null) {
            List<LiveWeatherResponse.DailyForecast> dailyForecasts = new ArrayList<>();
            int daysToMap = Math.min(7, response.getDaily().getTime().size());
            
            for (int i = 0; i < daysToMap; i++) {
                LiveWeatherResponse.DailyForecast daily = new LiveWeatherResponse.DailyForecast();
                daily.setDate(parseDateTime(response.getDaily().getTime().get(i)));
                daily.setTemperatureMax(response.getDaily().getTemperature2mMax().get(i));
                daily.setTemperatureMin(response.getDaily().getTemperature2mMin().get(i));
                daily.setPrecipitation(response.getDaily().getPrecipitationSum().get(i));
                
                // Add fog analysis for the day
                boolean hasFogExpected = false;
                String worstFogIntensity = "None";
                double worstVisibility = Double.MAX_VALUE;
                
                // Check hourly data for this day to find fog periods
                if (response.getHourly() != null && response.getHourly().getWeathercode() != null) {
                    for (int hour = 0; hour < 24; hour++) {
                        int hourlyIndex = i * 24 + hour;
                        if (hourlyIndex < response.getHourly().getWeathercode().size()) {
                            int hourlyWeatherCode = response.getHourly().getWeathercode().get(hourlyIndex);
                            if (isFogCondition(hourlyWeatherCode)) {
                                hasFogExpected = true;
                                String intensity = getFogIntensity(hourlyWeatherCode);
                                if (getFogSeverityLevel(intensity) > getFogSeverityLevel(worstFogIntensity)) {
                                    worstFogIntensity = intensity;
                                }
                                
                                double hourlyVisibility = getVisibilityFromHourly(response.getHourly(), hourlyIndex);
                                if (hourlyVisibility < worstVisibility) {
                                    worstVisibility = hourlyVisibility;
                                }
                            }
                        }
                    }
                }
                
                daily.setHasFogExpected(hasFogExpected);
                daily.setWorstFogIntensity(worstFogIntensity);
                daily.setWorstVisibility(worstVisibility == Double.MAX_VALUE ? 0 : worstVisibility);
                
                dailyForecasts.add(daily);
            }
            liveWeather.setDailyForecast(dailyForecasts);
        }
        
        return liveWeather;
    }
    
    private void saveCurrentWeatherToDatabase(LiveWeatherResponse.CurrentWeather current) {
        Meteo meteo = new Meteo();
        meteo.setTemperature(current.getTemperature());
        meteo.setPrecipitation(0.0); // Current weather doesn't include precipitation
        meteo.setVent(current.getWindSpeed());
        meteo.setCondition(current.getCondition());
        
        meteoRepository.save(meteo);
    }
    
    private String mapWeatherCodeToCondition(int weatherCode) {
        // WMO Weather interpretation codes
        switch (weatherCode) {
            case 0: return "Clear";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: case 48: return "Fog";
            case 51: case 53: case 55: return "Drizzle";
            case 56: case 57: return "Freezing Drizzle";
            case 61: case 63: case 65: return "Rain";
            case 66: case 67: return "Freezing Rain";
            case 71: case 73: case 75: return "Snow";
            case 77: return "Snow grains";
            case 80: case 81: case 82: return "Showers";
            case 85: case 86: return "Snow showers";
            case 95: return "Thunderstorm";
            case 96: case 99: return "Thunderstorm with hail";
            default: return "Unknown";
        }
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // Try parsing with different formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(dateTimeStr, formatter);
                } catch (Exception e) {
                    // Continue to next formatter
                }
            }
            
            // If all formatters fail, try parsing as date and add time
            return LocalDate.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            
        } catch (Exception e) {
            // Return current time as fallback
            return LocalDateTime.now();
        }
    }
    
    public FogAnalysis getFogAnalysis(double latitude, double longitude, String locationName) {
        OpenMeteoResponse openMeteoResponse = weatherClient.getWeatherData(latitude, longitude);
        return mapToFogAnalysis(openMeteoResponse, latitude, longitude, locationName);
    }
    
    public FogAnalysis getFogAnalysis(double latitude, double longitude) {
        return getFogAnalysis(latitude, longitude, "Unknown Location");
    }
    
    private FogAnalysis mapToFogAnalysis(OpenMeteoResponse response, double latitude, double longitude, String locationName) {
        FogAnalysis fogAnalysis = new FogAnalysis();
        fogAnalysis.setLocation(locationName);
        fogAnalysis.setLatitude(latitude);
        fogAnalysis.setLongitude(longitude);
        
        // Current fog status
        if (response.getCurrentWeather() != null) {
            FogAnalysis.FogStatus currentFog = new FogAnalysis.FogStatus();
            currentFog.setTimestamp(parseDateTime(response.getCurrentWeather().getTime()));
            currentFog.setCondition(mapWeatherCodeToCondition(response.getCurrentWeather().getWeathercode()));
            
            // Determine fog based on weather code and visibility (if available)
            boolean hasFog = isFogCondition(response.getCurrentWeather().getWeathercode());
            currentFog.setHasFog(hasFog);
            currentFog.setFogIntensity(getFogIntensity(response.getCurrentWeather().getWeathercode()));
            currentFog.setVisibility(getVisibilityFromHourly(response.getHourly(), 0)); // First hour visibility
            
            fogAnalysis.setCurrentFogStatus(currentFog);
        }
        
        // Hourly fog forecast
        if (response.getHourly() != null && response.getHourly().getTime() != null) {
            List<FogAnalysis.FogForecast> hourlyFogForecast = new ArrayList<>();
            int hoursToMap = Math.min(48, response.getHourly().getTime().size());
            
            for (int i = 0; i < hoursToMap; i++) {
                FogAnalysis.FogForecast hourlyFog = new FogAnalysis.FogForecast();
                hourlyFog.setTimestamp(parseDateTime(response.getHourly().getTime().get(i)));
                
                int weatherCode = response.getHourly().getWeathercode().get(i);
                boolean hasFog = isFogCondition(weatherCode);
                double visibility = getVisibilityFromHourly(response.getHourly(), i);
                
                hourlyFog.setHasFog(hasFog);
                hourlyFog.setFogIntensity(getFogIntensity(weatherCode));
                hourlyFog.setVisibility(visibility);
                hourlyFog.setCondition(mapWeatherCodeToCondition(weatherCode));
                hourlyFog.setSafeForTravel(isSafeForTravel(visibility, hasFog));
                
                hourlyFogForecast.add(hourlyFog);
            }
            fogAnalysis.setHourlyForecast(hourlyFogForecast);
        }
        
        // Daily fog summary
        if (response.getDaily() != null && response.getDaily().getTime() != null) {
            List<FogAnalysis.DailyFogSummary> dailyFogSummary = new ArrayList<>();
            int daysToMap = Math.min(7, response.getDaily().getTime().size());
            
            for (int i = 0; i < daysToMap; i++) {
                FogAnalysis.DailyFogSummary dailyFog = new FogAnalysis.DailyFogSummary();
                dailyFog.setDate(parseDateTime(response.getDaily().getTime().get(i)));
                
                // Check for fog in daily weather codes
                List<Integer> fogHours = new ArrayList<>();
                String worstFogIntensity = "None";
                double worstVisibility = Double.MAX_VALUE;
                boolean hasFogExpected = false;
                
                // Analyze hourly data for this day to find fog periods
                if (response.getHourly() != null && response.getHourly().getWeathercode() != null) {
                    for (int hour = 0; hour < 24; hour++) {
                        int hourlyIndex = i * 24 + hour;
                        if (hourlyIndex < response.getHourly().getWeathercode().size()) {
                            int hourlyWeatherCode = response.getHourly().getWeathercode().get(hourlyIndex);
                            if (isFogCondition(hourlyWeatherCode)) {
                                hasFogExpected = true;
                                fogHours.add(hour);
                                String intensity = getFogIntensity(hourlyWeatherCode);
                                if (getFogSeverityLevel(intensity) > getFogSeverityLevel(worstFogIntensity)) {
                                    worstFogIntensity = intensity;
                                }
                                
                                double hourlyVisibility = getVisibilityFromHourly(response.getHourly(), hourlyIndex);
                                if (hourlyVisibility < worstVisibility) {
                                    worstVisibility = hourlyVisibility;
                                }
                            }
                        }
                    }
                }
                
                dailyFog.setHasFogExpected(hasFogExpected);
                dailyFog.setWorstFogIntensity(worstFogIntensity);
                dailyFog.setWorstVisibility(worstVisibility == Double.MAX_VALUE ? 0 : worstVisibility);
                dailyFog.setFogHours(fogHours);
                dailyFog.setSafeForTravel(!hasFogExpected || worstVisibility > 1000); // Safe if no fog or visibility > 1km
                
                dailyFogSummary.add(dailyFog);
            }
            fogAnalysis.setDailyForecast(dailyFogSummary);
        }
        
        // Overall travel safety assessment
        boolean safeForTravel = assessOverallTravelSafety(fogAnalysis);
        fogAnalysis.setSafeForTravel(safeForTravel);
        fogAnalysis.setTravelRecommendation(getTravelRecommendation(safeForTravel, fogAnalysis));
        
        return fogAnalysis;
    }
    
    private boolean isFogCondition(int weatherCode) {
        // WMO Weather codes for fog conditions
        return weatherCode == 45 || // Fog
               weatherCode == 48;   // Fog rime
    }
    
    private String getFogIntensity(int weatherCode) {
        switch (weatherCode) {
            case 45: return "Moderate Fog";
            case 48: return "Dense Fog";
            default: return "No Fog";
        }
    }
    
    private int getFogSeverityLevel(String intensity) {
        switch (intensity) {
            case "Dense Fog": return 3;
            case "Moderate Fog": return 2;
            case "Light Fog": return 1;
            default: return 0;
        }
    }
    
    private double getVisibilityFromHourly(org.example.meteoservice.model.openmeteo.HourlyData hourly, int index) {
        if (hourly.getVisibility() != null && hourly.getVisibility().size() > index) {
            return hourly.getVisibility().get(index);
        }
        return 10000; // Default excellent visibility in meters
    }
    
    private boolean isSafeForTravel(double visibility, boolean hasFog) {
        // Travel safety thresholds
        if (hasFog && visibility < 1000) { // Less than 1km visibility with fog
            return false;
        }
        if (visibility < 500) { // Less than 500m visibility regardless of fog
            return false;
        }
        return true;
    }
    
    private boolean assessOverallTravelSafety(FogAnalysis fogAnalysis) {
        // Check current conditions
        if (fogAnalysis.getCurrentFogStatus() != null && 
            !isSafeForTravel(fogAnalysis.getCurrentFogStatus().getVisibility(), 
                           fogAnalysis.getCurrentFogStatus().isHasFog())) {
            return false;
        }
        
        // Check next 6 hours for travel safety
        if (fogAnalysis.getHourlyForecast() != null) {
            for (int i = 0; i < Math.min(6, fogAnalysis.getHourlyForecast().size()); i++) {
                FogAnalysis.FogForecast forecast = fogAnalysis.getHourlyForecast().get(i);
                if (!forecast.isSafeForTravel()) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private String getTravelRecommendation(boolean safeForTravel, FogAnalysis fogAnalysis) {
        if (safeForTravel) {
            return "Safe for travel. Current visibility conditions are good.";
        } else {
            String recommendation = "Caution advised for travel due to fog conditions. ";
            if (fogAnalysis.getCurrentFogStatus() != null && 
                fogAnalysis.getCurrentFogStatus().isHasFog()) {
                recommendation += "Current fog detected with " + 
                    fogAnalysis.getCurrentFogStatus().getFogIntensity() + 
                    " and visibility of " + fogAnalysis.getCurrentFogStatus().getVisibility() + " meters. ";
            }
            recommendation += "Consider delaying travel or using extra caution.";
            return recommendation;
        }
    }
}
