import { Component, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MapComponent } from '../map/map.component';
import { TripPlannerComponent } from '../trip-planner/trip-planner.component';
import { PredictionResultsComponent } from '../prediction-results/prediction-results.component';
import { WeeklyForecastComponent } from '../weekly-forecast/weekly-forecast.component';
import { AlertsComponent } from '../alerts/alerts.component';
import { PredictionService } from '../../services/prediction.service';
import { PredictionLogicService } from '../../services/prediction-logic.service';
import { MeteoService } from '../../services/meteo.service';
import { PredictionUIModel } from '../../models/prediction-ui.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MapComponent,
    TripPlannerComponent,
    PredictionResultsComponent,
    WeeklyForecastComponent,
    AlertsComponent
  ],
  template: `
    <div class="dashboard-horizontal">
      <!-- Main Horizontal Content -->
      <main class="main-content">
        
        <!-- LEFT PANEL - Trip Planner (Fixed Width) -->
        <section class="panel panel-left">
          <app-trip-planner 
            [isLoading]="isLoading" 
            (planTrip)="onPlanTrip($event)"
            (monitorTrip)="onMonitorTrip($event)">
          </app-trip-planner>
        </section>

        <!-- CENTER PANEL - Map -->
        <section class="panel panel-center">
          <div class="map-wrapper">
            <app-map></app-map>
          </div>
          
          <!-- Route Info Overlay -->
          <div class="route-overlay" *ngIf="currentPrediction">
            <span class="route-origin">📍 {{ currentPrediction.mode === 'transit' ? 'Transport' : 'Départ' }}</span>
            <span class="route-arrow">→</span>
            <span class="route-dest">🏁 Arrivée</span>
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
            
            <!-- Weekly Forecast for the location -->
            <app-weekly-forecast 
                *ngIf="targetLocation" 
                [lat]="targetLocation.lat" 
                [lng]="targetLocation.lng">
            </app-weekly-forecast>
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
    /* Same styles as before + Logout button */
    :host {
      display: block;
      height: 100%;
      overflow: hidden;
    }

    .dashboard-horizontal {
      height: 100%;
      display: flex;
      flex-direction: column;
      background: #f0f4f8;
      font-family: 'Segoe UI', -apple-system, sans-serif;
    }

    .main-content { 
      flex: 1; 
      display: flex; 
      flex-direction: row; 
      gap: 16px; 
      padding: 16px; 
      overflow: hidden; /* Constrain content to the flex container */
    }

    .panel { 
      background: white; 
      border-radius: 20px; 
      box-shadow: 0 4px 25px rgba(0,0,0,0.06); 
      overflow: hidden; 
      display: flex;
      flex-direction: column;
    }
    
    .panel-left { width: 350px; flex-shrink: 0; overflow-y: auto; }
    .panel-center { flex: 1; min-width: 400px; position: relative; display: flex; flex-direction: column; }
    .map-wrapper { flex: 1; min-height: 0; }
    .panel-right { width: 380px; flex-shrink: 0; overflow-y: auto; padding: 16px; }

    .route-overlay {
      position: absolute; 
      bottom: 24px; 
      left: 50%;
      transform: translateX(-50%);
      width: 90%;
      max-width: 600px;
      display: flex; 
      align-items: center; 
      justify-content: center; 
      gap: 20px;
      padding: 14px 30px; 
      background: rgba(255, 255, 255, 1); 
      border-radius: 100px;
      box-shadow: 0 10px 40px rgba(0,0,0,0.15); 
      color: #1e293b;
      z-index: 1000;
      animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
    }
    
    .route-origin, .route-dest { 
      font-size: 16px; 
      font-weight: 700; 
      color: #1e293b; 
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .route-arrow { 
      color: #94a3b8; 
      font-size: 20px; 
      font-weight: 300;
    }

    @keyframes slideUp { from { opacity: 0; transform: translate(-50%, 40px); } to { opacity: 1; transform: translate(-50%, 0); } }

    .loading-box { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 20px; color: #475569; }
    .loader { width: 40px; height: 40px; border: 4px solid #e2e8f0; border-top-color: #3b82f6; border-radius: 50%; animation: spin 0.8s linear infinite; margin-bottom: 16px; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .error-box { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; background: #fef2f2; border: 1px solid #fecaca; border-radius: 10px; color: #b91c1c; margin-bottom: 16px; }
    .error-box button { background: none; border: none; color: #b91c1c; cursor: pointer; font-size: 16px; }

    .empty-box { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 20px; text-align: center; color: #64748b; }
    .empty-icon { font-size: 50px; margin-bottom: 12px; }
    .empty-box p { margin: 0; font-size: 14px; }

    @media (max-width: 1200px) {
      .main-content { flex-wrap: wrap; }
      .panel-left, .panel-right { width: 100%; max-width: none; }
      .panel-center { width: 100%; order: -1; height: 350px; min-height: 350px; }
      .map-wrapper { min-height: 350px; }
    }
  `]
})
export class DashboardComponent implements OnDestroy {
  currentPrediction: PredictionUIModel | null = null;
  targetLocation: {lat: number, lng: number} | null = null;
  isLoading = false;
  error: string | null = null;
  currentTime: string = '';
  username: string | null = null;

  private timeInterval: any;

  constructor(
    private predictionService: PredictionService,
    private predictionLogicService: PredictionLogicService,
    private meteoService: MeteoService
  ) { }

  logout() {
    // Moved to App shell
  }

  @ViewChild(MapComponent) mapComponent!: MapComponent;

  onPlanTrip(event: { origin: string, destination: string, date: string, time: string, transportMode: string, originCoords?: {lat: number, lng: number} }) {
    console.log('Planning trip:', event);
    this.isLoading = true;
    this.currentPrediction = null;
    this.targetLocation = null;
    this.error = null;

    // 1. Get Route from Map
    const routeObservable = this.mapComponent ? 
        (event.originCoords ? 
            this.mapComponent.calculateRoute(event.originCoords, event.destination, event.transportMode) : 
            this.mapComponent.calculateRouteFromStrings(event.origin, event.destination, event.transportMode)
        ) : null;

    if (!routeObservable) {
        this.error = "Erreur interne: Carte non initialisée";
        this.isLoading = false;
        return;
    }

    routeObservable.subscribe({
        next: (routeResult) => {
            if (!routeResult) {
                this.error = "Trajet introuvable. Veuillez vérifier vos adresses.";
                this.isLoading = false;
                return;
            }

            // 2. Get Weather for Midpoint or Origin
            const leg = routeResult.routes[0].legs[0];
            // Use midpoint for better accuracy on long trips, or start location
            // Simple approach: Start location
            const startLoc = leg.start_location; 
            
            const lat = startLoc.lat();
            const lng = startLoc.lng();
            this.targetLocation = { lat, lng };

            this.meteoService.getLiveWeather(lat, lng).subscribe({
                next: (weatherData) => {
                    // 3. Build Prediction Model
                    try {
                        const mode = this.mapModeToEnum(event.transportMode);
                        this.currentPrediction = this.predictionLogicService.buildUIModel(routeResult, weatherData, mode);
                        this.isLoading = false;
                    } catch (e) {
                         console.error("Error building prediction model", e);
                         this.error = "Erreur lors du calcul des prévisions.";
                         this.isLoading = false;
                    }
                },
                error: (err) => {
                    console.error("Weather error", err);
                    // Fallback without weather
                    try {
                         const mode = this.mapModeToEnum(event.transportMode);
                         this.currentPrediction = this.predictionLogicService.buildUIModel(routeResult, null, mode);
                         this.isLoading = false;
                    } catch (e2) {
                        this.error = "Impossible de récupérer les données.";
                        this.isLoading = false;
                    }
                }
            });
        },
        error: (err) => {
            console.error("Route error", err);
            this.error = "Impossible de calculer l'itinéraire.";
            this.isLoading = false;
        }
    });

    // Also update current location if user provided it
    // if (event.originCoords) ...
  }

  private mapModeToEnum(mode: string): 'driving' | 'transit' | 'walking' {
      const lower = mode.toLowerCase();
      if (lower === 'walking' || lower === 'marche') return 'walking';
      if (lower === 'transit' || lower === 'transport') return 'transit';
      return 'driving';
  }

  onMonitorTrip(event: { origin: string, destination: string }) {
    this.predictionService.monitorTrip(event.origin, event.destination).subscribe({
      next: () => console.log('Monitoring started successfully'),
      error: (err) => console.error('Failed to start monitoring', err)
    });
  }

  ngOnDestroy() {
  }
}
