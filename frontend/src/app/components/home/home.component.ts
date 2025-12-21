import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MeteoService } from '../../services/meteo.service';
import { PredictionLogicService } from '../../services/prediction-logic.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="home-wrapper">
      <!-- Background Shapes -->
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>

      <div class="content-container">
        <!-- Hero Section -->
        <div class="hero-section">
          <div class="badge-container animate-fade-in">
            <span class="live-badge">
              <span class="pulse-dot"></span>
              Analyse IA en temps réel
            </span>
          </div>
          
          <h1 class="hero-title animate-slide-up">
            Prévoyez vos trajets avec <span class="gradient-text">SmartMove</span>
          </h1>
          
          <p class="hero-subtitle animate-slide-up delay-1">
            L'intelligence artificielle au service de votre mobilité urbaine. 
            Évitez les imprévus grâce à notre moteur de prédiction avancé.
          </p>

          <div class="cta-container animate-slide-up delay-2">
            <button class="primary-btn" routerLink="/dashboard">
              <span class="btn-text">Démarrer un trajet</span>
              <span class="btn-icon">🚀</span>
            </button>
            <div class="ia-indicator">
              <div class="spinner-small"></div>
              <span>Intelligence active</span>
            </div>
          </div>
        </div>

        <!-- Dashboard Preview / Cards -->
        <div class="stats-grid animate-fade-in delay-3">
          <!-- Traffic Card -->
          <div class="glass-card stat-item">
            <div class="card-header">
              <span class="card-icon traffic-icon">🚗</span>
              <span class="card-label">Trafic Urbain</span>
            </div>
            <div class="card-body">
              <div class="status-value" [ngClass]="isPeak ? 'text-orange' : 'text-green'">
                 {{ isPeak ? 'Dense' : 'Fluide' }}
              </div>
              <div class="status-desc">Casablanca : {{ isPeak ? 'Heure de pointe' : 'Conditions optimales' }}</div>
            </div>
            <div class="card-footer">
              <div class="progress-bar">
                   <div class="progress-fill" [ngClass]="isPeak ? 'orange' : 'green'" [style.width.%]="isPeak ? 75 : 15"></div>
              </div>
            </div>
          </div>

          <!-- Weather Card -->
          <div class="glass-card stat-item">
            <div class="card-header">
              <span class="card-icon weather-icon">☀️</span>
              <span class="card-label">Météo Locale</span>
            </div>
            <div class="card-body">
              <div class="status-value">{{ weatherTemp !== null ? (weatherTemp | number:'1.0-0') + '°C' : '--' }}</div>
              <div class="status-desc">{{ weatherDesc || 'Chargement...' }}</div>
            </div>
            <div class="card-footer">
              <div class="weather-trend" *ngIf="weatherTemp">📍 Casablanca</div>
            </div>
          </div>

          <!-- Alerts Card -->
          <div class="glass-card stat-item">
            <div class="card-header">
              <span class="card-icon alert-icon">🔔</span>
              <span class="card-label">Alertes Actives</span>
            </div>
            <div class="card-body">
              <div class="status-value text-blue">0</div>
              <div class="status-desc">Aucun incident majeur</div>
            </div>
            <div class="card-footer">
              <a class="card-link">Système vigilant</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      height: 100%;
      overflow-y: auto;
      background: #0f172a;
    }

    .home-wrapper {
      position: relative;
      min-height: 100%;
      padding: 80px 40px;
      display: flex;
      justify-content: center;
      align-items: center;
      overflow: hidden;
    }

    .bg-shape {
      position: absolute;
      filter: blur(80px);
      z-index: 0;
      opacity: 0.15;
    }
    .shape-1 { width: 400px; height: 400px; background: #3b82f6; top: -100px; left: -100px; }
    .shape-2 { width: 400px; height: 400px; background: #8b5cf6; bottom: -100px; right: -100px; }

    .content-container {
      position: relative;
      z-index: 1;
      max-width: 1100px;
      width: 100%;
      text-align: center;
    }

    .hero-section { margin-bottom: 80px; }

    .live-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      padding: 6px 16px;
      border-radius: 100px;
      color: #94a3b8;
      font-size: 13px;
      font-weight: 600;
      letter-spacing: 0.5px;
      text-transform: uppercase;
      margin-bottom: 24px;
    }

    .pulse-dot {
      width: 8px;
      height: 8px;
      background: #22c55e;
      border-radius: 50%;
      box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7);
      animation: pulse-ring 2s infinite;
    }

    @keyframes pulse-ring {
      0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7); }
      70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(34, 197, 94, 0); }
      100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
    }

    .hero-title {
      font-size: clamp(40px, 6vw, 68px);
      font-weight: 800;
      color: white;
      line-height: 1.1;
      margin-bottom: 24px;
      letter-spacing: -2px;
    }

    .gradient-text {
      background: linear-gradient(to right, #60a5fa, #c084fc);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }

    .hero-subtitle {
      font-size: 20px;
      color: #94a3b8;
      max-width: 650px;
      margin: 0 auto 40px;
      line-height: 1.6;
    }

    .cta-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
    }

    .primary-btn {
      position: relative;
      display: flex;
      align-items: center;
      gap: 12px;
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      color: white;
      border: none;
      padding: 18px 40px;
      border-radius: 16px;
      font-size: 18px;
      font-weight: 700;
      cursor: pointer;
      box-shadow: 0 10px 25px -5px rgba(37, 99, 235, 0.5);
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }

    .primary-btn:hover {
      transform: translateY(-4px) scale(1.02);
      box-shadow: 0 20px 35px -5px rgba(37, 99, 235, 0.6);
      background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
    }

    .btn-icon { font-size: 22px; transition: transform 0.3s; }
    .primary-btn:hover .btn-icon { transform: rotate(15deg) translateX(4px); }

    .ia-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #64748b;
    }

    .spinner-small {
      width: 14px;
      height: 14px;
      border: 2px solid rgba(148, 163, 184, 0.2);
      border-top-color: #3b82f6;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 24px;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 24px;
    }

    .glass-card {
      background: rgba(30, 41, 59, 0.5);
      backdrop-filter: blur(12px);
      border: 1px solid rgba(255, 255, 255, 0.05);
      border-radius: 24px;
      padding: 24px;
      text-align: left;
      transition: all 0.4s;
    }

    .glass-card:hover {
      transform: translateY(-8px);
      background: rgba(30, 41, 59, 0.7);
      border-color: rgba(255, 255, 255, 0.1);
      box-shadow: 0 20px 40px rgba(0,0,0,0.2);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
    }

    .card-icon {
      width: 40px;
      height: 40px;
      background: rgba(37, 99, 235, 0.1);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
    }

    .card-label { font-size: 14px; font-weight: 600; color: #64748b; }

    .status-value { font-size: 32px; font-weight: 700; color: white; margin-bottom: 4px; }
    .status-desc { font-size: 14px; color: #94a3b8; }

    .text-green { color: #22c55e; }
    .text-orange { color: #f97316; }
    .text-blue { color: #3b82f6; }

    .card-footer { margin-top: 24px; }
    .progress-bar { height: 6px; background: rgba(255, 255, 255, 0.05); border-radius: 10px; overflow: hidden; }
    .progress-fill { height: 100%; border-radius: 10px; }
    .progress-fill.green { background: #22c55e; }
    .progress-fill.orange { background: #f97316; }

    .card-link { color: #3b82f6; text-decoration: none; font-size: 14px; font-weight: 600; }
    .card-link:hover { text-underline-offset: 4px; text-decoration: underline; }

    .animate-fade-in { animation: fadeIn 1s both; }
    .animate-slide-up { animation: slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) both; }
    .delay-1 { animation-delay: 0.1s; }
    .delay-2 { animation-delay: 0.2s; }
    .delay-3 { animation-delay: 0.4s; }

    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes slideUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }

    @media (max-width: 768px) {
      .home-wrapper { padding: 40px 20px; }
      .hero-title { font-size: 36px; }
      .stats-grid { grid-template-columns: 1fr !important; }
    }
  `]
})
export class HomeComponent implements OnInit {
  isPeak: boolean = false;
  weatherTemp: number | null = null;
  weatherDesc: string | null = null;

  constructor(
      public authService: AuthService,
      private meteoService: MeteoService,
      private predictionLogic: PredictionLogicService
  ) { }

  ngOnInit(): void {
    // 1. Calculate Peak Hour
    this.isPeak = this.predictionLogic.isPeakHour(new Date());

    // 2. Fetch Weather for Casablanca (default)
    // 33.5731° N, 7.5898° W
    this.meteoService.getLiveWeather(33.5731, -7.5898).subscribe({
        next: (data) => {
            this.weatherTemp = data.current.temperature;
            this.weatherDesc = data.current.condition;
        },
        error: (err) => {
            console.error(err);
            this.weatherDesc = "Non disponible";
        }
    });
  }
}
