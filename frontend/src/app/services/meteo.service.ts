import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap, shareReplay, map } from 'rxjs/operators';

export interface CurrentWeather {
    temperature: number;
    windSpeed: number;
    windDirection: number;
    condition: string;
    timestamp: string;
    visibility: number;
    hasFog: boolean;
    fogIntensity: string;
}

export interface LiveWeatherResponse {
    current: CurrentWeather;
}

export interface WeeklyForecast {
    daily: {
        time: string[];
        weathercode: number[];
        temperature_2m_max: number[];
        temperature_2m_min: number[];
        precipitation_probability_max: number[];
        windspeed_10m_max: number[];
    };
}

@Injectable({
    providedIn: 'root'
})
export class MeteoService {
    private openMeteoUrl = 'https://api.open-meteo.com/v1/forecast';
    
    private cache = new Map<string, { data: any, timestamp: number }>();
    private CACHE_DURATION = 10 * 60 * 1000; // 10 minutes

    constructor(private http: HttpClient) { }

    getLiveWeather(lat: number, lng: number): Observable<LiveWeatherResponse> {
        const key = `live-${lat.toFixed(2)}-${lng.toFixed(2)}`;
        const cached = this.checkCache(key);
        if (cached) return of(cached);

        const url = `${this.openMeteoUrl}?latitude=${lat}&longitude=${lng}&current_weather=true`;
        return this.http.get<any>(url).pipe(
            map(data => {
                const response: LiveWeatherResponse = {
                    current: {
                        temperature: data.current_weather.temperature,
                        windSpeed: data.current_weather.windspeed,
                        windDirection: data.current_weather.winddirection,
                        condition: this.mapWeatherCode(data.current_weather.weathercode),
                        timestamp: new Date().toISOString(),
                        visibility: 10000,
                        hasFog: data.current_weather.weathercode === 45 || data.current_weather.weathercode === 48,
                        fogIntensity: 'None'
                    }
                };
                return response;
            }),
            tap(data => this.setCache(key, data))
        );
    }

    getWeeklyForecast(lat: number, lng: number): Observable<WeeklyForecast> {
        const key = `weekly-${lat.toFixed(2)}-${lng.toFixed(2)}`;
        const cached = this.checkCache(key);
        if (cached) return of(cached);

        const url = `${this.openMeteoUrl}?latitude=${lat}&longitude=${lng}&daily=weathercode,temperature_2m_max,temperature_2m_min,precipitation_probability_max,windspeed_10m_max&timezone=auto`;
        return this.http.get<WeeklyForecast>(url).pipe(
            tap(data => this.setCache(key, data))
        );
    }

    private checkCache(key: string): any | null {
        const item = this.cache.get(key);
        if (item) {
            if (Date.now() - item.timestamp < this.CACHE_DURATION) {
                return item.data;
            } else {
                this.cache.delete(key);
            }
        }
        return null;
    }

    private setCache(key: string, data: any): void {
        this.cache.set(key, { data, timestamp: Date.now() });
    }

    private mapWeatherCode(code: number): string {
        if (code === 0) return 'Ciel dégagé';
        if (code >= 1 && code <= 3) return 'Partiellement nuageux';
        if (code === 45 || code === 48) return 'Brouillard';
        if (code >= 51 && code <= 55) return 'Bruine';
        if (code >= 61 && code <= 65) return 'Pluie';
        if (code >= 71 && code <= 77) return 'Neige';
        if (code >= 80 && code <= 82) return 'Averses';
        if (code >= 95 && code <= 99) return 'Orage';
        return 'Variable';
    }
}
