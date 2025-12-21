import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MeteoService, WeeklyForecast } from '../../services/meteo.service';

@Component({
  selector: 'app-weekly-forecast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="weekly-forecast animate-fade-in" *ngIf="forecast">
      <h4 class="title">Prévisions sur 7 jours</h4>
      <div class="forecast-scroll">
        <div class="forecast-card" *ngFor="let day of dailyForecasts">
          <span class="day-name">{{ day.name }}</span>
          <span class="weather-icon">{{ day.icon }}</span>
          <div class="temps">
            <span class="max">{{ day.maxTemp | number:'1.0-0' }}°</span>
            <span class="min">{{ day.minTemp | number:'1.0-0' }}°</span>
          </div>
          <span class="rain" *ngIf="day.rainProb > 0">💧 {{ day.rainProb }}%</span>
        </div>
      </div>
    </div>
    
    <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
    </div>

    <div *ngIf="error" class="error">
        {{ error }}
    </div>
  `,
  styles: [`
    .weekly-forecast {
      margin-top: 0;
      padding: 16px;
      background: white;
      border-radius: 20px;
      box-shadow: 0 -4px 20px rgba(0,0,0,0.05);
      border: 1px solid #f1f5f9;
      display: flex;
      flex-direction: column;
    }

    .title {
      font-size: 15px;
      font-weight: 700;
      color: #334155;
      margin: 0 0 12px 0;
    }

    .forecast-scroll {
      display: flex;
      overflow-x: auto;
      gap: 12px;
      padding-bottom: 8px; /* For scrollbar */
      /* Center items if enough space? No, keep left aligned usually better for timeline */
    }

    .forecast-scroll::-webkit-scrollbar {
        height: 6px;
    }
    .forecast-scroll::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 10px;
    }

    .forecast-card {
      min-width: 80px;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px;
      border-radius: 16px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
    }

    .day-name {
      font-size: 13px;
      font-weight: 600;
      color: #64748b;
      margin-bottom: 8px;
    }

    .weather-icon {
      font-size: 24px;
      margin-bottom: 8px;
    }

    .temps {
      display: flex;
      gap: 8px;
      font-size: 14px;
      font-weight: 700;
    }

    .max { color: #1e293b; }
    .min { color: #94a3b8; }

    .rain {
      font-size: 11px;
      color: #3b82f6;
      font-weight: 600;
      margin-top: 6px;
    }

    .loading { padding: 20px; display: flex; justify-content: center; }
    .error { padding: 10px; color: #ef4444; font-size: 13px; text-align: center; }
    
    .animate-fade-in { animation: fadeIn 0.5s ease-out both; }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
  `]
})
export class WeeklyForecastComponent implements OnInit, OnChanges {
  @Input() lat!: number;
  @Input() lng!: number;

  forecast: WeeklyForecast | null = null;
  dailyForecasts: any[] = [];
  loading = false;
  error: string | null = null;

  constructor(private meteoService: MeteoService) {}

  ngOnInit() {
    // If inputs are already available
    if (this.lat && this.lng) {
      this.fetchForecast();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if ((changes['lat'] || changes['lng']) && this.lat && this.lng) {
        this.fetchForecast();
    }
  }

  private fetchForecast() {
    this.loading = true;
    this.error = null;
    this.meteoService.getWeeklyForecast(this.lat, this.lng).subscribe({
        next: (data) => {
            this.forecast = data;
            this.processData(data);
            this.loading = false;
        },
        error: (err) => {
            console.error('Forecast error', err);
            this.error = 'Prévisions indisponibles';
            this.loading = false;
        }
    });
  }

  private processData(data: WeeklyForecast) {
      if (!data || !data.daily) return;

      const daily = data.daily;
      this.dailyForecasts = daily.time.map((time, index) => {
          const date = new Date(time);
          const dayName = new Intl.DateTimeFormat('fr-FR', { weekday: 'short' }).format(date);
          const code = daily.weathercode[index];
          
          return {
              name: dayName,
              icon: this.getWeatherIcon(code),
              maxTemp: daily.temperature_2m_max[index],
              minTemp: daily.temperature_2m_min[index],
              rainProb: daily.precipitation_probability_max[index]
          };
      });
  }

  private getWeatherIcon(code: number): string {
      // WMO Weather interpretation codes (WW)
      // 0: Clear sky
      if (code === 0) return '☀️';
      // 1-3: Mainly clear, partly cloudy, and overcast
      if (code >= 1 && code <= 3) return '⛅';
      // 45, 48: Fog
      if (code === 45 || code === 48) return '🌫️';
      // 51-55: Drizzle
      if (code >= 51 && code <= 55) return '🌦️';
      // 61-65: Rain
      if (code >= 61 && code <= 65) return '🌧️';
      // 71-77: Snow
      if (code >= 71 && code <= 77) return '❄️';
      // 80-82: Rain showers
      if (code >= 80 && code <= 82) return '🌦️';
      // 95-99: Thunderstorm
      if (code >= 95 && code <= 99) return '⛈️';
      
      return '❓';
  }
}
