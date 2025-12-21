
export interface Advice {
  title: string;
  text: string;
  type: 'success' | 'warning' | 'danger' | 'info';
}

export interface PredictionUIModel {
  // Core Trip Data
  mode: 'driving' | 'transit' | 'walking';
  distanceKm: number;
  durationMinutes: number;
  arrivalTime: string;
  
  // Traffic / Impact
  trafficDelayMinutes: number | null; // Null if not driving/data missing
  trafficLevel: 'FLUID' | 'MODERATE' | 'HEAVY' | 'UNKNOWN';
  
  // Weather Impact
  weatherSummary: string;
  weatherImpactLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  
  // Risk & Confidence
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  riskScore: number; // 0-100
  confidenceLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  
  // SmartMove Advice
  advice: Advice;
  
  // Data Availability Flags
  dataStatus: {
    hasTraffic: boolean;
    hasWeather: boolean;
    hasIncidents: boolean;
  };

  // Weather Raw (for display if needed)
  currentTemp?: number;
  weatherCondition?: string;
}
