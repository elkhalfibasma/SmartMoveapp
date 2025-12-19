import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Prediction } from '../../services/prediction.service';

@Component({
  selector: 'app-prediction-results',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="results" *ngIf="prediction">
      
      <!-- Main Duration Card -->
      <div class="main-card" [class]="'risk-' + prediction.riskLevel.toLowerCase()">
        <div class="duration-section">
          <span class="duration-icon">⏱️</span>
          <div class="duration-info">
            <span class="duration-value">{{ prediction.durationText || formatDuration(prediction.predictedDuration) }}</span>
            <span class="duration-label">Durée estimée</span>
          </div>
        </div>
        
        <div class="time-row">
          <div class="time-item">
            <span>🚀 Départ</span>
            <strong>{{ prediction.departureTime || '--:--' }}</strong>
          </div>
          <span class="arrow">→</span>
          <div class="time-item">
            <span>🏁 Arrivée</span>
            <strong>{{ prediction.arrivalTime || '--:--' }}</strong>
          </div>
        </div>
        
        <div class="info-row">
          <span class="info-item">📍 {{ prediction.distanceKm | number:'1.1-1' }} km</span>
          <span class="info-item risk-badge" [class]="'badge-' + prediction.riskLevel.toLowerCase()">
            {{ getRiskEmoji() }} {{ getRiskLabel() }}
          </span>
        </div>
      </div>

      <!-- Impact Factors -->
      <div class="section">
        <h4>📊 Facteurs d'Impact</h4>
        <div class="impact-list" *ngIf="prediction.impactFactors">
          <div class="impact-item">
            <span class="impact-label">🚗 Trafic</span>
            <div class="impact-bar-bg">
              <div class="impact-bar traffic" [style.width.%]="prediction.impactFactors.traffic"></div>
            </div>
            <span class="impact-pct">{{ prediction.impactFactors.traffic }}%</span>
          </div>
          <div class="impact-item">
            <span class="impact-label">🌤️ Météo</span>
            <div class="impact-bar-bg">
              <div class="impact-bar weather" [style.width.%]="prediction.impactFactors.weather"></div>
            </div>
            <span class="impact-pct">{{ prediction.impactFactors.weather }}%</span>
          </div>
          <div class="impact-item">
            <span class="impact-label">⚠️ Accidents</span>
            <div class="impact-bar-bg">
              <div class="impact-bar incidents" [style.width.%]="prediction.impactFactors.incidents"></div>
            </div>
            <span class="impact-pct">{{ prediction.impactFactors.incidents }}%</span>
          </div>
          <div class="impact-item">
            <span class="impact-label">⏰ Heure</span>
            <div class="impact-bar-bg">
              <div class="impact-bar peakhour" [style.width.%]="prediction.impactFactors.peakHour"></div>
            </div>
            <span class="impact-pct">{{ prediction.impactFactors.peakHour }}%</span>
          </div>
        </div>
      </div>

      <!-- Conditions -->
      <div class="section">
        <h4>🔍 Conditions Actuelles</h4>
        <div class="conditions-grid">
          <div class="condition" [class.active]="prediction.isPeakHour">
            ⏰ {{ prediction.isPeakHour ? 'Heure de pointe' : 'Heure creuse' }}
          </div>
          <div class="condition" [class.warning]="prediction.hasIncidents">
            🚧 {{ prediction.hasIncidents ? prediction.incidentCount + ' incident(s)' : 'Aucun incident' }}
          </div>
          <div class="condition">
            {{ getWeatherIcon() }} {{ prediction.weatherCondition }} ({{ prediction.temperature | number:'1.0-0' }}°C)
          </div>
          <div class="condition traffic-{{ prediction.trafficCondition?.toLowerCase() }}">
            🚦 Trafic {{ prediction.trafficCondition }}
          </div>
        </div>
      </div>

      <!-- AI Recommendation -->
      <div class="ai-card" *ngIf="prediction.aiRecommendation">
        <span class="ai-icon">🤖</span>
        <div class="ai-content">
          <strong>Conseil SmartMove</strong>
          <p>{{ prediction.aiRecommendation }}</p>
        </div>
      </div>

      <!-- Details -->
      <div class="section" *ngIf="prediction.explanationPoints?.length">
        <h4>📋 Détails</h4>
        <ul class="details-list">
          <li *ngFor="let point of prediction.explanationPoints">{{ point }}</li>
        </ul>
      </div>

    </div>
  `,
  styles: [`
    .results {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    /* Main Card */
    .main-card {
      padding: 20px;
      border-radius: 14px;
      background: linear-gradient(135deg, #22c55e, #16a34a);
      color: white;
    }

    .main-card.risk-medium {
      background: linear-gradient(135deg, #f59e0b, #d97706);
    }

    .main-card.risk-high {
      background: linear-gradient(135deg, #ef4444, #dc2626);
    }

    .duration-section {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
    }

    .duration-icon {
      font-size: 36px;
    }

    .duration-info {
      display: flex;
      flex-direction: column;
    }

    .duration-value {
      font-size: 32px;
      font-weight: 700;
      line-height: 1;
    }

    .duration-label {
      font-size: 12px;
      opacity: 0.9;
      text-transform: uppercase;
      letter-spacing: 1px;
    }

    .time-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: rgba(255,255,255,0.15);
      border-radius: 10px;
      padding: 12px;
      margin-bottom: 12px;
    }

    .time-item {
      display: flex;
      flex-direction: column;
      font-size: 12px;
    }

    .time-item strong {
      font-size: 18px;
      font-weight: 700;
    }

    .arrow {
      font-size: 20px;
      opacity: 0.7;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
    }

    .risk-badge {
      padding: 4px 10px;
      background: rgba(255,255,255,0.2);
      border-radius: 20px;
      font-weight: 600;
    }

    /* Section */
    .section {
      background: #f8fafc;
      border-radius: 12px;
      padding: 16px;
    }

    .section h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      color: #1e293b;
      font-weight: 600;
    }

    /* Impact Bars */
    .impact-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .impact-item {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .impact-label {
      width: 90px;
      font-size: 12px;
      color: #475569;
    }

    .impact-bar-bg {
      flex: 1;
      height: 8px;
      background: #e2e8f0;
      border-radius: 4px;
      overflow: hidden;
    }

    .impact-bar {
      height: 100%;
      border-radius: 4px;
      transition: width 0.5s ease;
    }

    .impact-bar.traffic { background: #3b82f6; }
    .impact-bar.weather { background: #f59e0b; }
    .impact-bar.incidents { background: #ef4444; }
    .impact-bar.peakhour { background: #8b5cf6; }

    .impact-pct {
      width: 36px;
      font-size: 12px;
      font-weight: 600;
      color: #64748b;
      text-align: right;
    }

    /* Conditions */
    .conditions-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
    }

    .condition {
      padding: 10px 12px;
      background: white;
      border-radius: 8px;
      font-size: 12px;
      color: #475569;
      border: 1px solid #e2e8f0;
    }

    .condition.active {
      background: #fef3c7;
      border-color: #fcd34d;
      color: #92400e;
    }

    .condition.warning {
      background: #fee2e2;
      border-color: #fca5a5;
      color: #b91c1c;
    }

    .condition.traffic-fluide {
      background: #dcfce7;
      border-color: #86efac;
      color: #166534;
    }

    .condition.traffic-modéré {
      background: #fef3c7;
      border-color: #fcd34d;
      color: #92400e;
    }

    .condition.traffic-dense {
      background: #fee2e2;
      border-color: #fca5a5;
      color: #b91c1c;
    }

    /* AI Card */
    .ai-card {
      display: flex;
      gap: 12px;
      padding: 14px;
      background: #e0f2fe;
      border-radius: 12px;
      border: 1px solid #7dd3fc;
    }

    .ai-icon {
      font-size: 24px;
    }

    .ai-content {
      flex: 1;
    }

    .ai-content strong {
      font-size: 12px;
      color: #0369a1;
      display: block;
      margin-bottom: 4px;
    }

    .ai-content p {
      margin: 0;
      font-size: 13px;
      color: #0c4a6e;
      line-height: 1.4;
    }

    /* Details */
    .details-list {
      margin: 0;
      padding: 0;
      list-style: none;
    }

    .details-list li {
      padding: 8px 0;
      border-bottom: 1px solid #e2e8f0;
      font-size: 13px;
      color: #475569;
    }

    .details-list li:last-child {
      border-bottom: none;
    }
  `]
})
export class PredictionResultsComponent {
  @Input() prediction: Prediction | null = null;

  formatDuration(minutes: number): string {
    if (!minutes) return '--';
    const totalMinutes = Math.round(minutes);
    if (totalMinutes >= 60) {
      const hours = Math.floor(totalMinutes / 60);
      const mins = totalMinutes % 60;
      return mins > 0 ? `${hours}h ${mins}min` : `${hours}h`;
    }
    return `${totalMinutes} min`;
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
      case 'HIGH': return 'Risque élevé';
      case 'MEDIUM': return 'Risque moyen';
      default: return 'Risque faible';
    }
  }

  getWeatherIcon(): string {
    if (!this.prediction?.weatherCondition) return '☀️';
    const c = this.prediction.weatherCondition.toLowerCase();
    if (c.includes('rain') || c.includes('pluie')) return '🌧️';
    if (c.includes('cloud') || c.includes('overcast')) return '☁️';
    if (c.includes('fog')) return '🌫️';
    if (c.includes('storm')) return '⛈️';
    return '☀️';
  }
}
