import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PredictionUIModel } from '../../models/prediction-ui.model';

@Component({
  selector: 'app-prediction-results',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="results animate-fade-in" *ngIf="prediction">
      
      <!-- Main Duration Card -->
      <div class="main-card animate-slide-up" [class]="'risk-' + prediction.riskLevel.toLowerCase()">
        <div class="duration-section">
          <span class="duration-icon">⏱️</span>
          <div class="duration-info">
            <span class="duration-value">{{ formatDuration(prediction.durationMinutes) }}</span>
            <span class="duration-label">Durée Estimée ({{ prediction.mode }})</span>
          </div>
        </div>
        
        <div class="time-row">
          <div class="time-item">
            <span>🏁 Arrivée estimée</span>
            <strong>{{ prediction.arrivalTime }}</strong>
          </div>
          <div class="time-item" *ngIf="prediction.dataStatus.hasTraffic">
               <span>🚦 Retard Trafic</span>
               <strong>+{{ prediction.trafficDelayMinutes }} min</strong>
          </div>
        </div>
        
        <div class="info-row">
          <span class="info-item">📍 {{ prediction.distanceKm | number:'1.1-1' }} km</span>
          <span class="info-item risk-badge">
            {{ getRiskEmoji() }} {{ getRiskLabel() }}
          </span>
        </div>
      </div>

      <!-- Advice Card -->
      <div class="ai-card animate-slide-up delay-1" [class]="'type-' + prediction.advice.type">
        <div class="ai-header">
           <span class="ai-icon">💡</span>
           <strong>{{ prediction.advice.title }}</strong>
        </div>
        <p class="ai-text">{{ prediction.advice.text }}</p>
      </div>

      <!-- Details Section -->
      <div class="section animate-slide-up delay-2">
        <h4 class="section-title">📊 Analyse du trajet</h4>
        
        <div class="impact-list">
            <!-- Traffic -->
            <div class="impact-item">
                <span class="impact-label">Trafic</span>
                <div class="impact-content">
                    <span class="status-text" [class]="getTrafficClass()">
                        {{ prediction.trafficLevel }} 
                        <span *ngIf="prediction.dataStatus.hasTraffic">({{ prediction.trafficDelayMinutes }} min délai)</span>
                        <span *ngIf="!prediction.dataStatus.hasTraffic && prediction.mode === 'driving'" class="unavailable">(Données non disp.)</span>
                    </span>
                </div>
            </div>

            <!-- Weather -->
            <div class="impact-item">
                <span class="impact-label">Météo</span>
                <div class="impact-content">
                    <span class="status-text" [class]="getWeatherClass()">
                        {{ prediction.weatherSummary }}
                        <span *ngIf="prediction.currentTemp">({{ prediction.currentTemp }}°C)</span>
                    </span>
                    <span class="impact-sub" *ngIf="!prediction.dataStatus.hasWeather">(Météo non disp.)</span>
                </div>
            </div>

            <!-- Reliability/Confidence -->
            <div class="impact-item">
                <span class="impact-label">Fiabilité</span>
                <div class="impact-content">
                    <span class="confidence-badge" [class]="'conf-' + prediction.confidenceLevel.toLowerCase()">
                        {{ prediction.confidenceLevel }}
                    </span>
                </div>
            </div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .results { padding-bottom: 24px; }
    
    /* Main Card */
    .main-card {
      padding: 24px;
      border-radius: 20px;
      background: linear-gradient(135deg, #22c55e, #16a34a);
      color: white;
      box-shadow: 0 10px 30px rgba(22, 163, 74, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.1);
      margin-bottom: 20px;
    }
    .main-card.risk-medium { background: linear-gradient(135deg, #f59e0b, #d97706); box-shadow: 0 10px 30px rgba(217, 119, 6, 0.3); }
    .main-card.risk-high { background: linear-gradient(135deg, #ef4444, #dc2626); box-shadow: 0 10px 30px rgba(220, 38, 38, 0.3); }

    .duration-section { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .duration-icon { font-size: 40px; }
    .duration-info { display: flex; flex-direction: column; }
    .duration-value { font-size: 38px; font-weight: 800; line-height: 1; }
    .duration-label { font-size: 13px; opacity: 0.9; text-transform: uppercase; font-weight: 600; margin-top: 4px; }

    .time-row { display: flex; justify-content: space-between; background: rgba(0,0,0,0.1); border-radius: 12px; padding: 16px; margin-bottom: 16px; }
    .time-item { display: flex; flex-direction: column; }
    .time-item span { font-size: 11px; opacity: 0.8; text-transform: uppercase; font-weight: 600; }
    .time-item strong { font-size: 18px; font-weight: 700; }

    .info-row { display: flex; justify-content: space-between; align-items: center; }
    .info-item { font-size: 14px; font-weight: 600; }
    .risk-badge { padding: 4px 10px; background: rgba(0,0,0,0.15); border-radius: 20px; font-size: 12px; }

    /* Advice Card */
    .ai-card { padding: 20px; background: #f0f9ff; border-radius: 20px; margin-bottom: 20px; border-left: 5px solid #0ea5e9; }
    .ai-card.type-warning { background: #fffbeb; border-left-color: #f59e0b; }
    .ai-card.type-danger { background: #fef2f2; border-left-color: #ef4444; }
    .ai-card.type-success { background: #f0fdf4; border-left-color: #22c55e; }
    .ai-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
    .ai-icon { font-size: 20px; }
    .ai-text { margin: 0; font-size: 14px; color: #334155; line-height: 1.5; }

    /* Impact Section */
    .section { background: white; border-radius: 20px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.03); }
    .section-title { margin: 0 0 16px 0; font-size: 16px; font-weight: 700; color: #1e293b; }
    .impact-list { display: flex; flex-direction: column; gap: 16px; }
    .impact-item { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 12px; }
    .impact-item:last-child { border-bottom: none; padding-bottom: 0; }
    .impact-label { width: 80px; font-size: 14px; font-weight: 600; color: #64748b; }
    .impact-content { flex: 1; text-align: right; display: flex; flex-direction: column; align-items: flex-end; }
    
    .status-text { font-size: 14px; font-weight: 600; }
    .status-good { color: #16a34a; }
    .status-mod { color: #ca8a04; }
    .status-bad { color: #dc2626; }
    .unavailable { color: #94a3b8; font-style: italic; font-weight: 400; font-size: 12px; }

    .confidence-badge { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 700; }
    .conf-high { background: #dcfce7; color: #166534; }
    .conf-medium { background: #fef3c7; color: #92400e; }
    .conf-low { background: #fee2e2; color: #991b1b; }

    .animate-fade-in { animation: fadeIn 0.5s ease-out both; }
    .animate-slide-up { animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) both; }
    .delay-1 { animation-delay: 0.1s; }
    .delay-2 { animation-delay: 0.2s; }

    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
  `]
})
export class PredictionResultsComponent {
  @Input() prediction: PredictionUIModel | null = null;

  formatDuration(minutes: number): string {
    if (!minutes) return '--';
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h > 0) return `${h}h ${m}min`;
    return `${m} min`;
  }

  getRiskEmoji(): string {
    if (!this.prediction) return '🟢';
    switch (this.prediction.riskLevel) {
      case 'HIGH': return '🔴';
      case 'MEDIUM': return '🟠';
      default: return '🟢';
    }
  }

  getRiskLabel(): string {
    if (!this.prediction) return '';
    switch (this.prediction.riskLevel) {
      case 'HIGH': return 'Risque Élevé';
      case 'MEDIUM': return 'Risque Modéré';
      default: return 'Risque Faible';
    }
  }

  getTrafficClass(): string {
      if (!this.prediction) return '';
      switch(this.prediction.trafficLevel) {
          case 'HEAVY': return 'status-bad';
          case 'MODERATE': return 'status-mod';
          case 'FLUID': return 'status-good';
          default: return 'unavailable';
      }
  }

  getWeatherClass(): string {
      if (!this.prediction) return '';
      switch(this.prediction.weatherImpactLevel) {
          case 'HIGH': return 'status-bad';
          case 'MEDIUM': return 'status-mod';
          default: return 'status-good';
      }
  }
}
