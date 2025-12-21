/// <reference types="google.maps" />
import { Component, EventEmitter, Output, Input, ViewChild, ElementRef, AfterViewInit, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-trip-planner',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="planner">
      <div class="planner-header">
        <h3>📍 Planifier un Trajet</h3>
      </div>

      <form (ngSubmit)="onSubmit()" class="planner-form">
        
        <!-- Origin -->
        <div class="field">
          <label>Point de départ</label>
          <div class="input-wrap">
            <span class="dot green"></span>
            <input 
              #originInput
              type="text" 
              [(ngModel)]="origin" 
              name="origin" 
              placeholder="Entrez le point de départ"
            >
            <button type="button" class="loc-btn" (click)="useCurrentLocation()" title="Ma position actuelle">📍</button>
          </div>
        </div>

        <!-- Destination -->
        <div class="field">
          <label>Destination</label>
          <div class="input-wrap">
            <span class="dot red"></span>
            <input 
              #destInput
              type="text" 
              [(ngModel)]="destination" 
              name="destination" 
              placeholder="Entrez la destination"
            >
          </div>
        </div>

        <!-- Date & Time Row -->
        <div class="row">
          <div class="field half">
            <label>Date</label>
            <input type="date" [(ngModel)]="departureDate" name="date">
          </div>
          <div class="field half">
            <label>Heure</label>
            <input type="time" [(ngModel)]="departureTime" name="time">
          </div>
        </div>

        <!-- Transport Mode -->
        <div class="field">
          <label>Mode de transport</label>
          <div class="transport-modes">
            <button type="button" [class.active]="selectedMode === 'driving'" (click)="setMode('driving')" title="Conduite (Traffic intelligent)">
              🚗
            </button>
            <button type="button" [class.active]="selectedMode === 'transit'" (click)="setMode('transit')" title="Transport en commun">
              🚌
            </button>
            <button type="button" [class.active]="selectedMode === 'walking'" (click)="setMode('walking')" title="Marche (Santé)">
              🚶
            </button>
          </div>
        </div>

        <!-- Quick Time Buttons -->
        <div class="quick-btns">
          <button type="button" (click)="setNow()" [class.active]="isNow">⚡ Maintenant</button>
          <button type="button" (click)="setMorningPeak()" [class.active]="isMorningPeak">🌅 8h00</button>
          <button type="button" (click)="setEveningPeak()" [class.active]="isEveningPeak">🌆 18h00</button>
        </div>

        <!-- Action Buttons -->
        <div class="actions">
          <button type="submit" class="submit-btn" [disabled]="!origin || !destination || isLoading">
            <span *ngIf="!isLoading">🔍 Analyser</span>
            <span *ngIf="isLoading">⏳ Analyse...</span>
          </button>
          
          <button type="button" class="monitor-btn" 
                  [disabled]="!origin || !destination" 
                  (click)="onMonitor()"
                  title="Recevoir des alertes si la durée change">
            🔔 M'alerter
          </button>
        </div>

      </form>

      <!-- Popular Routes -->
      <div class="popular">
        <span class="label">Routes populaires:</span>
        <div class="chips">
          <button *ngFor="let r of popularRoutes" (click)="setRoute(r.origin, r.destination)" class="chip">
            {{r.origin}} → {{r.destination}}
          </button>
        </div>
      </div>

      <div class="info-note">
        💡 Entrez n'importe quelle adresse au Maroc. Google Maps calculera le meilleur itinéraire.
      </div>

    </div>
  `,
  styles: [`
    .planner { padding: 20px; height: 100%; display: flex; flex-direction: column; }
    .planner-header h3 { margin: 0 0 20px 0; font-size: 18px; color: #1e293b; font-weight: 600; }
    .planner-form { flex: 1; }
    .field { margin-bottom: 16px; position: relative; }
    .field label { display: block; font-size: 12px; font-weight: 600; color: #475569; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
    .input-wrap { display: flex; align-items: center; background: #f8fafc; border: 2px solid #e2e8f0; border-radius: 10px; transition: border-color 0.2s; }
    .input-wrap:focus-within { border-color: #3b82f6; }
    .dot { width: 10px; height: 10px; border-radius: 50%; margin-left: 12px; flex-shrink: 0; }
    .dot.green { background: #22c55e; }
    .dot.red { background: #ef4444; }
    input { flex: 1; border: none; background: none; padding: 12px; font-size: 14px; color: #1e293b; outline: none; }
    input::placeholder { color: #94a3b8; }
    .loc-btn { background: none; border: none; font-size: 16px; cursor: pointer; padding: 0 10px; transition: transform 0.2s; }
    .loc-btn:hover { transform: scale(1.1); }
    .row { display: flex; gap: 12px; }
    .field.half { flex: 1; }
    .field.half input { width: 100%; background: #f8fafc; border: 2px solid #e2e8f0; border-radius: 10px; padding: 12px; }
    .field.half input:focus { border-color: #3b82f6; }
    .quick-btns { display: flex; gap: 8px; margin-bottom: 16px; }
    .quick-btns button { flex: 1; padding: 10px; background: #f1f5f9; border: 2px solid transparent; border-radius: 8px; font-size: 12px; cursor: pointer; transition: all 0.2s; color: #475569; }
    .quick-btns button:hover { background: #e2e8f0; }
    .quick-btns button.active { background: #dbeafe; border-color: #3b82f6; color: #1d4ed8; }
    .submit-btn { width: 100%; padding: 14px; background: #2563eb; border: none; border-radius: 10px; color: white; font-size: 15px; font-weight: 600; cursor: pointer; transition: background 0.2s; }
    .submit-btn:hover:not(:disabled) { background: #1d4ed8; }
    .submit-btn:disabled { background: #94a3b8; cursor: not-allowed; }
    .popular { margin-top: 20px; padding-top: 16px; border-top: 1px solid #e2e8f0; }
    .popular .label { font-size: 12px; color: #64748b; display: block; margin-bottom: 8px; }
    .chips { display: flex; flex-wrap: wrap; gap: 6px; }
    .chip { padding: 6px 12px; background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 20px; font-size: 11px; color: #475569; cursor: pointer; transition: all 0.2s; }
    .chip:hover { background: #e2e8f0; }
    .actions { display: flex; gap: 10px; }
    .monitor-btn { padding: 14px; background: #f59e0b; border: none; border-radius: 10px; color: white; font-size: 15px; font-weight: 600; cursor: pointer; transition: background 0.2s; flex: 0 0 auto; }
    .monitor-btn:hover:not(:disabled) { background: #d97706; }
    .monitor-btn:disabled { background: #cbd5e1; cursor: not-allowed; }
    .transport-modes {
      display: flex;
      gap: 10px;
      margin-bottom: 2px;
    }
    .transport-modes button {
      flex: 1;
      padding: 10px;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      background: white;
      font-size: 24px;
      cursor: pointer;
       transition: all 0.2s;
    }
    .transport-modes button:hover {
      background: #f8fafc;
      transform: translateY(-2px);
    }
    .transport-modes button.active {
      background: #eff6ff;
      border-color: #3b82f6;
      box-shadow: 0 0 0 2px #3b82f6;
    }
    .info-note { margin-top: 16px; padding: 12px; background: #fef9c3; border-radius: 8px; font-size: 11px; color: #854d0e; line-height: 1.4; }
  `]
})
export class TripPlannerComponent implements AfterViewInit {
  @Input() isLoading = false;
  @Output() planTrip = new EventEmitter<any>(); // Changed to any to support flexible payload
  @Output() monitorTrip = new EventEmitter<{ origin: string, destination: string }>();

  @ViewChild('originInput') originInput!: ElementRef;
  @ViewChild('destInput') destInput!: ElementRef;

  origin = '';
  destination = '';

  constructor(private ngZone: NgZone) {}

  departureDate = new Date().toISOString().split('T')[0];
  departureTime = this.getCurrentTime();

  isNow = true;
  isMorningPeak = false;
  isEveningPeak = false;
  
  selectedMode: string = 'driving';
  originCoords: { lat: number, lng: number } | null = null;

  popularRoutes = [
    { origin: 'Maarif, Casablanca', destination: 'Casa Port, Casablanca' },
    { origin: 'Technopark, Casablanca', destination: 'Ain Diab, Casablanca' },
    { origin: 'Casablanca', destination: 'Rabat' },
  ];

  ngAfterViewInit() {
    this.initAutocomplete();
  }
  
  setMode(mode: string): void {
    this.selectedMode = mode;
  }

  // ... (initAutocomplete remains the same)

  initAutocomplete() {
    if (!this.originInput || !this.destInput) return;

    const options = {
      fields: ["formatted_address", "name", "geometry"], // Added request for geometry
      componentRestrictions: { country: "ma" }
    };

    const originAutocomplete = new google.maps.places.Autocomplete(this.originInput.nativeElement, options);
    const destAutocomplete = new google.maps.places.Autocomplete(this.destInput.nativeElement, options);

    originAutocomplete.addListener("place_changed", () => {
      const place = originAutocomplete.getPlace();
      if (place.formatted_address) {
        this.origin = place.formatted_address;
        if (place.geometry && place.geometry.location) {
             this.originCoords = { lat: place.geometry.location.lat(), lng: place.geometry.location.lng() };
        }
      }
    });

    destAutocomplete.addListener("place_changed", () => {
      const place = destAutocomplete.getPlace();
      if (place.formatted_address) {
        this.destination = place.formatted_address;
      }
    });
  }

  useCurrentLocation() {
    if (navigator.geolocation) {
      this.origin = 'Localisation en cours...';
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const pos = {
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          };
          
          this.originCoords = pos; // Save exact coords

          const geocoder = new google.maps.Geocoder();
          geocoder.geocode({ location: pos }, (results, status) => {
            if (status === 'OK' && results && results[0]) {
              this.ngZone.run(() => {
                  this.origin = results[0].formatted_address;
              });
            } else {
              this.ngZone.run(() => {
                  this.origin = `${pos.lat}, ${pos.lng}`;
              });
            }
          });
        },
        (error) => {
          console.error('Geolocation error:', error);
          this.origin = 'Erreur de géolocalisation';
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0
        }
      );
    } else {
      alert("Votre navigateur ne supporte pas la géolocalisation.");
    }
  }

  getCurrentTime(): string {
    const now = new Date();
    return now.getHours().toString().padStart(2, '0') + ':' +
      now.getMinutes().toString().padStart(2, '0');
  }

  onSubmit() {
    if (this.origin && this.destination) {
      this.planTrip.emit({
        origin: this.origin,
        destination: this.destination,
        date: this.departureDate,
        time: this.departureTime,
        transportMode: this.selectedMode,
        originCoords: this.originCoords
      });
    }
  }

  onMonitor() {
    if (this.origin && this.destination) {
      this.monitorTrip.emit({
        origin: this.origin,
        destination: this.destination
      });
      alert('Monitoring activé ! Vous serez notifié si la durée change.');
    }
  }

  setNow() {
    this.departureTime = this.getCurrentTime();
    this.departureDate = new Date().toISOString().split('T')[0];
    this.isNow = true; this.isMorningPeak = false; this.isEveningPeak = false;
  }

  setMorningPeak() {
    this.departureTime = '08:00';
    this.isNow = false; this.isMorningPeak = true; this.isEveningPeak = false;
  }

  setEveningPeak() {
    this.departureTime = '18:00';
    this.isNow = false; this.isMorningPeak = false; this.isEveningPeak = true;
  }

  setRoute(origin: string, destination: string) {
    this.origin = origin;
    this.destination = destination;
  }
}
