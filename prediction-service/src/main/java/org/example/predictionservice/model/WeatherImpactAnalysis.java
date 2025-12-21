package org.example.predictionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured analysis of weather impact on travel.
 * Used to provide traceable and explainable results to the user.
 */
public class WeatherImpactAnalysis {
    private double impactModifier;
    private int impactPercentage;
    private String impactLevel;
    private String explanation;

    public WeatherImpactAnalysis(double impactModifier, int impactPercentage, String impactLevel, String explanation) {
        this.impactModifier = impactModifier;
        this.impactPercentage = impactPercentage;
        this.impactLevel = impactLevel;
        this.explanation = explanation;
    }

    public static WeatherImpactAnalysisBuilder builder() {
        return new WeatherImpactAnalysisBuilder();
    }

    public double getImpactModifier() { return impactModifier; }
    public int getImpactPercentage() { return impactPercentage; }
    public String getImpactLevel() { return impactLevel; }
    public String getExplanation() { return explanation; }

    public static class WeatherImpactAnalysisBuilder {
        private double impactModifier;
        private int impactPercentage;
        private String impactLevel;
        private String explanation;

        public WeatherImpactAnalysisBuilder impactModifier(double impactModifier) {
            this.impactModifier = impactModifier;
            return this;
        }

        public WeatherImpactAnalysisBuilder impactPercentage(int impactPercentage) {
            this.impactPercentage = impactPercentage;
            return this;
        }

        public WeatherImpactAnalysisBuilder impactLevel(String impactLevel) {
            this.impactLevel = impactLevel;
            return this;
        }

        public WeatherImpactAnalysisBuilder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public WeatherImpactAnalysis build() {
            return new WeatherImpactAnalysis(impactModifier, impactPercentage, impactLevel, explanation);
        }
    }
}
