import { Injectable } from '@angular/core';
import { PredictionUIModel, Advice } from '../models/prediction-ui.model';

@Injectable({
  providedIn: 'root'
})
export class PredictionLogicService {

  constructor() { }

  /**
   * Builds the strictly typed UI model from raw Google Maps and Weather data.
   */
  public buildUIModel(
    route: google.maps.DirectionsResult,
    weather: any, // Typed as any for now, ideally strictly typed MeteoResponse
    mode: 'driving' | 'transit' | 'walking'
  ): PredictionUIModel {
    
    if (!route || !route.routes || !route.routes[0] || !route.routes[0].legs || !route.routes[0].legs[0]) {
      throw new Error('Invalid Route Data');
    }

    const leg = route.routes[0].legs[0];
    const distanceKm = leg.distance ? leg.distance.value / 1000 : 0;
    const durationSec = leg.duration ? leg.duration.value : 0;
    const durationInTrafficSec = leg.duration_in_traffic ? leg.duration_in_traffic.value : undefined;

    // 1. Calculate Duration & Traffic
    let finalDurationMinutes = Math.round(durationSec / 60);
    let trafficDelayMinutes: number | null = null;
    let trafficLevel: 'FLUID' | 'MODERATE' | 'HEAVY' | 'UNKNOWN' = 'UNKNOWN';

    if (mode === 'driving' && durationInTrafficSec !== undefined) {
      finalDurationMinutes = Math.round(durationInTrafficSec / 60);
      const baseDurationMinutes = Math.round(durationSec / 60);
      trafficDelayMinutes = Math.max(0, finalDurationMinutes - baseDurationMinutes);

      if (trafficDelayMinutes === 0) trafficLevel = 'FLUID';
      else if (trafficDelayMinutes < 5) trafficLevel = 'MODERATE';
      else trafficLevel = 'HEAVY';
    } else if (mode === 'driving') {
        // Fallback if no traffic data
        trafficLevel = 'UNKNOWN';
    } else {
        // Not driving
        trafficLevel = 'UNKNOWN'; // Or specific enum for 'NOT_APPLICABLE'
    }

    // 2. Weather Analysis
    const isRaining = weather?.current?.condition?.toLowerCase().includes('rain') || weather?.current?.condition?.toLowerCase().includes('pluie');
    const isSevere = weather?.current?.windSpeed > 40 || weather?.current?.condition?.toLowerCase().includes('storm');
    
    let weatherImpactLevel: 'LOW' | 'MEDIUM' | 'HIGH' = 'LOW';
    if (isSevere) weatherImpactLevel = 'HIGH';
    else if (isRaining) weatherImpactLevel = 'MEDIUM';

    // 3. Risk Calculation
    // Simple weighted score
    let riskScore = 0;
    if (trafficLevel === 'HEAVY') riskScore += 40;
    if (trafficLevel === 'MODERATE') riskScore += 20;
    if (weatherImpactLevel === 'HIGH') riskScore += 40;
    if (weatherImpactLevel === 'MEDIUM') riskScore += 20;
    
    // Time check (Peak hours)
    const now = new Date();
    const isPeak = this.isPeakHour(now);
    if (isPeak) riskScore += 20;

    let riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' = 'LOW';
    if (riskScore > 60) riskLevel = 'HIGH';
    else if (riskScore > 30) riskLevel = 'MEDIUM';

    // 4. Generate Advice
    const advice = this.generateAdvice(mode, trafficDelayMinutes, weatherImpactLevel, isPeak);

    // 5. Arrival Time
    const arrivalTime = this.calculateArrivalTime(finalDurationMinutes);

    return {
      mode,
      distanceKm,
      durationMinutes: finalDurationMinutes,
      arrivalTime,
      
      trafficDelayMinutes,
      trafficLevel,
      
      weatherSummary: weather?.current?.condition || 'Non disponible',
      weatherImpactLevel,
      
      riskLevel,
      riskScore,
      confidenceLevel: this.calculateConfidence(mode, !!durationInTrafficSec, !!weather),
      
      advice,
      
      dataStatus: {
        hasTraffic: mode === 'driving' && durationInTrafficSec !== undefined,
        hasWeather: !!weather,
        hasIncidents: false // For now fixed as we don't have real incidents
      },

      currentTemp: weather?.current?.temperature,
      weatherCondition: weather?.current?.condition
    };
  }

  public isPeakHour(date: Date): boolean {
    const day = date.getDay();
    if (day === 0 || day === 6) return false; // Weekend
    
    const h = date.getHours();
    // 07:00-10:00 and 16:30-20:00
    return (h >= 7 && h < 10) || (h >= 16 && h < 20); // Simplified
  }

  private generateAdvice(
    mode: 'driving' | 'transit' | 'walking',
    delay: number | null,
    weatherImpact: 'LOW' | 'MEDIUM' | 'HIGH',
    isPeak: boolean
  ): Advice {
    if (mode === 'walking' && weatherImpact !== 'LOW') {
      return {
        title: 'Conditions difficiles',
        text: 'Pluie ou vent fort détectés. Envisagez les transports en commun ou un VTC.',
        type: 'warning'
      };
    }

    if (mode === 'driving' && delay && delay > 15) {
      return {
        title: 'Trafic perturbé',
        text: `Retard estimé de ${delay} min. Départ différé conseillé si possible.`,
        type: 'danger'
      };
    }

    if (mode === 'transit' && isPeak) {
      return {
        title: 'Heure de pointe',
        text: 'Les transports peuvent être bondés. Prévoyez une marge de temps.',
        type: 'info'
      };
    }

    if (weatherImpact === 'HIGH') {
         return {
            title: 'Météo dangereuse',
            text: 'Visibilité réduite et risques de glissade. Prudence absolue.',
            type: 'danger'
         };
    }

    return {
      title: 'Conditions favorables',
      text: 'Bonne route ! Les conditions sont optimales.',
      type: 'success'
    };
  }

  private calculateConfidence(mode: string, hasTraffic: boolean, hasWeather: boolean): 'LOW' | 'MEDIUM' | 'HIGH' {
    if (mode === 'driving') {
        if (hasTraffic && hasWeather) return 'HIGH';
        if (hasTraffic || hasWeather) return 'MEDIUM';
        return 'LOW';
    }
    // Transit/Walking usually simpler, but weather matters
    if (hasWeather) return 'HIGH';
    return 'MEDIUM';
  }

  private calculateArrivalTime(durationMinutes: number): string {
    const now = new Date();
    const arrival = new Date(now.getTime() + durationMinutes * 60000);
    return `${arrival.getHours().toString().padStart(2, '0')}:${arrival.getMinutes().toString().padStart(2, '0')}`;
  }
}
