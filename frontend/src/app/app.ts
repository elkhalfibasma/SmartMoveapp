import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MapComponent } from './components/map/map.component';
import { TripPlannerComponent } from './components/trip-planner/trip-planner.component';
import { PredictionResultsComponent } from './components/prediction-results/prediction-results.component';
import { AlertsComponent } from './components/alerts/alerts.component';
import { Prediction, PredictionService } from './services/prediction.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    CommonModule,
    MapComponent,
    TripPlannerComponent,
    PredictionResultsComponent,
    AlertsComponent
  ],
  template: `
    <!-- HORIZONTAL DASHBOARD LAYOUT -->
    <div class="dashboard-horizontal">
      
      <!-- Header Bar -->
      <header class="header-bar">
        <div class="logo">
          <span class="logo-icon">🚗</span>
          <span class="logo-text">SmartMove</span>
        </div>
        <div class="header-info">
          <span class="status-badge">
            <span class="status-dot"></span>
            Temps Réel
          </span>
          <span class="time-display">{{ currentTime }}</span>
        </div>
      </header>

      <!-- Main Horizontal Content -->
      <main class="main-content">
        
        <!-- LEFT PANEL - Trip Planner (Fixed Width) -->
        <section class="panel panel-left">
          <app-trip-planner 
            [isLoading]="isLoading" 
            (planTrip)="onPlanTrip($event)">
          </app-trip-planner>
        </section>

        <!-- CENTER PANEL - Map -->
        <section class="panel panel-center">
          <div class="map-wrapper">
            <app-map></app-map>
          </div>
          
          <!-- Route Info Overlay -->
          <div class="route-overlay" *ngIf="currentPrediction">
            <span class="route-origin">📍 {{ currentPrediction.origin }}</span>
            <span class="route-arrow">→</span>
            <span class="route-dest">🏁 {{ currentPrediction.destination }}</span>
          </div>
        </section>

        <!-- RIGHT PANEL - Results -->
        <section class="panel panel-right">
          
          <!-- Loading State -->
          <div class="loading-box" *ngIf="isLoading">
            <div class="loader"></div>
            <span>Analyse en cours...</span>
          </div>

          <!-- Error State -->
          <div class="error-box" *ngIf="error">
            <span>⚠️ {{ error }}</span>
            <button (click)="error = null">✕</button>
          </div>

          <!-- Results -->
          <div *ngIf="currentPrediction && !isLoading">
            <app-prediction-results [prediction]="currentPrediction"></app-prediction-results>
          </div>

          <!-- Empty State -->
          <div class="empty-box" *ngIf="!currentPrediction && !isLoading && !error">
            <span class="empty-icon">🗺️</span>
            <p>Entrez un trajet pour voir les résultats</p>
          </div>

        </section>

      </main>

    </div>
  `,
  styles: [`
    /* ===== HORIZONTAL DASHBOARD ===== */
    .dashboard-horizontal {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: #f0f4f8;
      font-family: 'Segoe UI', -apple-system, sans-serif;
    }

    /* Header */
    .header-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 24px;
      background: #1e3a5f;
      color: white;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .logo-icon {
      font-size: 28px;
    }

    .logo-text {
      font-size: 22px;
      font-weight: 700;
    }

    .header-info {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .status-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 14px;
      background: rgba(255,255,255,0.15);
      border-radius: 20px;
      font-size: 13px;
    }

    .status-dot {
      width: 8px;
      height: 8px;
      background: #22c55e;
      border-radius: 50%;
      animation: pulse 2s infinite;
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.4; }
    }

    .time-display {
      font-size: 14px;
      font-weight: 500;
      padding: 6px 14px;
      background: rgba(255,255,255,0.1);
      border-radius: 8px;
    }

    /* Main Content - HORIZONTAL LAYOUT */
    .main-content {
      flex: 1;
      display: flex;
      flex-direction: row;
      gap: 16px;
      padding: 16px;
      overflow: hidden;
    }

    /* Panels */
    .panel {
      background: white;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.08);
      overflow: hidden;
    }

    .panel-left {
      width: 340px;
      flex-shrink: 0;
      overflow-y: auto;
    }

    .panel-center {
      flex: 1;
      min-width: 400px;
      position: relative;
    }

    .map-wrapper {
      height: 100%;
      min-height: 500px;
    }

    .panel-right {
      width: 380px;
      flex-shrink: 0;
      overflow-y: auto;
      padding: 16px;
    }

    /* Route Overlay */
    .route-overlay {
      position: absolute;
      bottom: 16px;
      left: 16px;
      right: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      padding: 12px 20px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.15);
      font-size: 14px;
      font-weight: 500;
      color: #1e293b;
    }

    .route-arrow {
      color: #64748b;
      font-size: 18px;
    }

    /* Loading Box */
    .loading-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      color: #475569;
    }

    .loader {
      width: 40px;
      height: 40px;
      border: 4px solid #e2e8f0;
      border-top-color: #3b82f6;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin-bottom: 16px;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    /* Error Box */
    .error-box {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 14px 18px;
      background: #fef2f2;
      border: 1px solid #fecaca;
      border-radius: 10px;
      color: #b91c1c;
      margin-bottom: 16px;
    }

    .error-box button {
      background: none;
      border: none;
      color: #b91c1c;
      cursor: pointer;
      font-size: 16px;
    }

    /* Empty Box */
    .empty-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      text-align: center;
      color: #64748b;
    }

    .empty-icon {
      font-size: 50px;
      margin-bottom: 12px;
    }

    .empty-box p {
      margin: 0;
      font-size: 14px;
    }

    /* Responsive */
    @media (max-width: 1200px) {
      .main-content {
        flex-wrap: wrap;
      }
      
      .panel-left, .panel-right {
        width: 100%;
        max-width: none;
      }
      
      .panel-center {
        width: 100%;
        order: -1;
        height: 350px;
        min-height: 350px;
      }

      .map-wrapper {
        min-height: 350px;
      }
    }
  `]
})
export class App {
  title = 'SmartMove Dashboard';
  currentPrediction: Prediction | null = null;
  isLoading = false;
  error: string | null = null;
  currentTime: string = '';

  private timeInterval: any;

  constructor(private predictionService: PredictionService) {
    this.updateTime();
    this.timeInterval = setInterval(() => this.updateTime(), 1000);
  }

  updateTime() {
    const now = new Date();
    this.currentTime = now.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  onPlanTrip(event: { origin: string, destination: string, date: string, time: string }) {
    console.log('Planning trip:', event);

    this.isLoading = true;
    this.currentPrediction = null;
    this.error = null;

    this.predictionService.analyzeEnrichedTrip(
      event.origin,
      event.destination,
      event.date,
      event.time
    ).subscribe({
      next: (prediction) => {
        console.log('Prediction received:', prediction);
        this.currentPrediction = prediction;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Prediction error:', err);
        this.error = 'Erreur lors de l\'analyse. Réessayez.';
        this.isLoading = false;
      }
    });
  }

  ngOnDestroy() {
    if (this.timeInterval) {
      clearInterval(this.timeInterval);
    }
  }
}
