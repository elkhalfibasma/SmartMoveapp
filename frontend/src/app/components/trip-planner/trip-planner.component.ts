import { Component, EventEmitter, Output, Input } from '@angular/core';
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
              type="text" 
              [(ngModel)]="origin" 
              name="origin" 
              placeholder="N'importe quelle adresse..."
              (focus)="showOriginSuggestions = true"
              (blur)="hideSuggestions('origin')"
            >
          </div>
          <div class="suggestions" *ngIf="showOriginSuggestions && getFilteredSuggestions(origin).length">
            <div *ngFor="let s of getFilteredSuggestions(origin)" 
                 (mousedown)="selectSuggestion('origin', s)" 
                 class="suggestion">
              📍 {{s}}
            </div>
          </div>
        </div>

        <!-- Destination -->
        <div class="field">
          <label>Destination</label>
          <div class="input-wrap">
            <span class="dot red"></span>
            <input 
              type="text" 
              [(ngModel)]="destination" 
              name="destination" 
              placeholder="N'importe quelle adresse..."
              (focus)="showDestSuggestions = true"
              (blur)="hideSuggestions('dest')"
            >
          </div>
          <div class="suggestions" *ngIf="showDestSuggestions && getFilteredSuggestions(destination).length">
            <div *ngFor="let s of getFilteredSuggestions(destination)" 
                 (mousedown)="selectSuggestion('dest', s)" 
                 class="suggestion">
              🏁 {{s}}
            </div>
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

        <!-- Quick Time Buttons -->
        <div class="quick-btns">
          <button type="button" (click)="setNow()" [class.active]="isNow">⚡ Maintenant</button>
          <button type="button" (click)="setMorningPeak()" [class.active]="isMorningPeak">🌅 8h00</button>
          <button type="button" (click)="setEveningPeak()" [class.active]="isEveningPeak">🌆 18h00</button>
        </div>

        <!-- Submit -->
        <button type="submit" class="submit-btn" [disabled]="!origin || !destination || isLoading">
          <span *ngIf="!isLoading">🔍 Analyser</span>
          <span *ngIf="isLoading">⏳ Analyse...</span>
        </button>

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
        💡 Entrez n'importe quelle adresse au Maroc - TomTom calcule automatiquement la distance réelle.
      </div>

    </div>
  `,
  styles: [`
    .planner {
      padding: 20px;
      height: 100%;
      display: flex;
      flex-direction: column;
    }

    .planner-header h3 {
      margin: 0 0 20px 0;
      font-size: 18px;
      color: #1e293b;
      font-weight: 600;
    }

    .planner-form {
      flex: 1;
    }

    /* Fields */
    .field {
      margin-bottom: 16px;
      position: relative;
    }

    .field label {
      display: block;
      font-size: 12px;
      font-weight: 600;
      color: #475569;
      margin-bottom: 6px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .input-wrap {
      display: flex;
      align-items: center;
      background: #f8fafc;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      transition: border-color 0.2s;
    }

    .input-wrap:focus-within {
      border-color: #3b82f6;
    }

    .dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      margin-left: 12px;
      flex-shrink: 0;
    }

    .dot.green { background: #22c55e; }
    .dot.red { background: #ef4444; }

    input {
      flex: 1;
      border: none;
      background: none;
      padding: 12px;
      font-size: 14px;
      color: #1e293b;
      outline: none;
    }

    input::placeholder {
      color: #94a3b8;
    }

    /* Suggestions */
    .suggestions {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.12);
      z-index: 100;
      max-height: 180px;
      overflow-y: auto;
      margin-top: 4px;
    }

    .suggestion {
      padding: 12px 14px;
      cursor: pointer;
      font-size: 13px;
      color: #334155;
    }

    .suggestion:hover {
      background: #f1f5f9;
    }

    /* Row */
    .row {
      display: flex;
      gap: 12px;
    }

    .field.half {
      flex: 1;
    }

    .field.half input {
      width: 100%;
      background: #f8fafc;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      padding: 12px;
    }

    .field.half input:focus {
      border-color: #3b82f6;
    }

    /* Quick Buttons */
    .quick-btns {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
    }

    .quick-btns button {
      flex: 1;
      padding: 10px;
      background: #f1f5f9;
      border: 2px solid transparent;
      border-radius: 8px;
      font-size: 12px;
      cursor: pointer;
      transition: all 0.2s;
      color: #475569;
    }

    .quick-btns button:hover {
      background: #e2e8f0;
    }

    .quick-btns button.active {
      background: #dbeafe;
      border-color: #3b82f6;
      color: #1d4ed8;
    }

    /* Submit */
    .submit-btn {
      width: 100%;
      padding: 14px;
      background: #2563eb;
      border: none;
      border-radius: 10px;
      color: white;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    }

    .submit-btn:hover:not(:disabled) {
      background: #1d4ed8;
    }

    .submit-btn:disabled {
      background: #94a3b8;
      cursor: not-allowed;
    }

    /* Popular */
    .popular {
      margin-top: 20px;
      padding-top: 16px;
      border-top: 1px solid #e2e8f0;
    }

    .popular .label {
      font-size: 12px;
      color: #64748b;
      display: block;
      margin-bottom: 8px;
    }

    .chips {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
    }

    .chip {
      padding: 6px 12px;
      background: #f1f5f9;
      border: 1px solid #e2e8f0;
      border-radius: 20px;
      font-size: 11px;
      color: #475569;
      cursor: pointer;
      transition: all 0.2s;
    }

    .chip:hover {
      background: #e2e8f0;
    }

    /* Info Note */
    .info-note {
      margin-top: 16px;
      padding: 12px;
      background: #fef9c3;
      border-radius: 8px;
      font-size: 11px;
      color: #854d0e;
      line-height: 1.4;
    }
  `]
})
export class TripPlannerComponent {
  @Input() isLoading = false;
  @Output() planTrip = new EventEmitter<{ origin: string, destination: string, date: string, time: string }>();

  origin = '';
  destination = '';
  departureDate = new Date().toISOString().split('T')[0];
  departureTime = this.getCurrentTime();

  isNow = true;
  isMorningPeak = false;
  isEveningPeak = false;

  showOriginSuggestions = false;
  showDestSuggestions = false;

  popularRoutes = [
    { origin: 'Maarif', destination: 'Casa Port' },
    { origin: 'Maarif', destination: 'Rabat' },
    { origin: 'Casablanca', destination: 'Marrakech' },
    { origin: 'Technopark', destination: 'Centre Ville' }
  ];

  locations = [
    'Maarif', 'Casa Port', 'Technopark', 'Ain Diab', 'Anfa',
    'Centre Ville', 'Sidi Maarouf', 'Bourgogne', 'Hay Hassani',
    'Casablanca', 'Rabat', 'Marrakech', 'Tanger', 'Fes', 'Agadir',
    'El Jadida', 'Mohammedia', 'Kenitra', 'Meknes', 'Oujda'
  ];

  getCurrentTime(): string {
    const now = new Date();
    return now.getHours().toString().padStart(2, '0') + ':' +
      now.getMinutes().toString().padStart(2, '0');
  }

  getFilteredSuggestions(input: string): string[] {
    if (!input) return this.locations.slice(0, 5);
    const lower = input.toLowerCase();
    return this.locations.filter(l => l.toLowerCase().includes(lower)).slice(0, 6);
  }

  selectSuggestion(field: 'origin' | 'dest', value: string) {
    if (field === 'origin') { this.origin = value; this.showOriginSuggestions = false; }
    else { this.destination = value; this.showDestSuggestions = false; }
  }

  hideSuggestions(field: 'origin' | 'dest') {
    setTimeout(() => {
      if (field === 'origin') this.showOriginSuggestions = false;
      else this.showDestSuggestions = false;
    }, 150);
  }

  onSubmit() {
    if (this.origin && this.destination) {
      this.planTrip.emit({
        origin: this.origin,
        destination: this.destination,
        date: this.departureDate,
        time: this.departureTime
      });
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
