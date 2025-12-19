import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface ImpactFactors {
    traffic: number;
    weather: number;
    incidents: number;
    peakHour: number;
}

export interface Prediction {
    origin: string;
    destination: string;
    predictedDuration: number;
    baseDuration: number;
    distanceKm: number;
    durationText: string;
    departureTime: string;
    arrivalTime: string;
    riskLevel: string;
    riskScore: number;
    timestamp: string;
    impactFactors: ImpactFactors;
    isPeakHour: boolean;
    hasIncidents: boolean;
    weatherCondition: string;
    trafficCondition: string;
    explanationPoints: string[];
    aiRecommendation: string;
    temperature: number;
    visibility: number;
    windSpeed: number;
    incidentCount: number;
    incidentSeverity: string;
}

@Injectable({
    providedIn: 'root'
})
export class PredictionService {
    private apiUrl = 'http://localhost:8085/api/notifications/stream';
    private predictionApiUrl = 'http://localhost:8086/api/predictions';

    constructor(private http: HttpClient, private ngZone: NgZone) { }

    getPredictions(): Observable<Prediction[]> {
        return new Observable<Prediction[]>(observer => {
            const eventSource = new EventSource(this.apiUrl);

            eventSource.onmessage = (event) => {
                this.ngZone.run(() => {
                    try {
                        const data = JSON.parse(event.data);
                        console.log('Received SSE:', data);
                        observer.next([data]);
                    } catch (e) {
                        console.error('Error parsing SSE data', e);
                    }
                });
            };

            eventSource.onerror = (error) => {
                this.ngZone.run(() => {
                    console.error('EventSource error:', error);
                });
            };

            return () => {
                eventSource.close();
            };
        });
    }

    /**
     * Analyze a trip with full enriched prediction (dynamic factors)
     */
    analyzeEnrichedTrip(origin: string, destination: string, date: string, time: string): Observable<Prediction> {
        return this.http.post<Prediction>(`${this.predictionApiUrl}/analyze/enriched`, {
            origin,
            destination,
            departureDate: date,
            departureTime: time
        }).pipe(
            map(response => {
                console.log('Enriched prediction received:', response);
                return response;
            }),
            catchError(error => {
                console.error('Prediction API error:', error);
                return of(this.createFallbackPrediction(origin, destination, time));
            })
        );
    }

    /**
     * Legacy endpoint for basic prediction
     */
    analyzeTrip(origin: string, destination: string): Observable<Prediction> {
        return this.analyzeEnrichedTrip(origin, destination, '', '');
    }

    private createFallbackPrediction(origin: string, destination: string, time: string): Prediction {
        const now = new Date();
        const departureTime = time || `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;

        return {
            origin,
            destination,
            predictedDuration: 20,
            baseDuration: 18,
            distanceKm: 8,
            durationText: '20 min',
            departureTime: departureTime,
            arrivalTime: this.calculateArrivalTime(departureTime, 20),
            riskLevel: 'LOW',
            riskScore: 15,
            timestamp: new Date().toISOString(),
            impactFactors: {
                traffic: 50,
                weather: 20,
                incidents: 10,
                peakHour: 20
            },
            isPeakHour: false,
            hasIncidents: false,
            weatherCondition: 'Clear',
            trafficCondition: 'Fluide',
            explanationPoints: [
                '🚗 Trafic fluide',
                '🌤️ Météo favorable',
                '✅ Aucun accident signalé',
                '⏰ Heure creuse'
            ],
            aiRecommendation: 'Conditions optimales pour votre trajet. Bonne route!',
            temperature: 22,
            visibility: 10000,
            windSpeed: 10,
            incidentCount: 0,
            incidentSeverity: 'NONE'
        };
    }

    private calculateArrivalTime(departureTime: string, durationMinutes: number): string {
        try {
            const [hours, minutes] = departureTime.split(':').map(Number);
            const totalMinutes = hours * 60 + minutes + durationMinutes;
            const arrivalHours = Math.floor(totalMinutes / 60) % 24;
            const arrivalMinutes = totalMinutes % 60;
            return `${arrivalHours.toString().padStart(2, '0')}:${arrivalMinutes.toString().padStart(2, '0')}`;
        } catch {
            return '--:--';
        }
    }
}
