/// <reference types="google.maps" />
import { Component, ElementRef, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MeteoService } from '../../services/meteo.service';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit {


  map!: google.maps.Map;
  directionsService!: google.maps.DirectionsService;
  directionsRenderer!: google.maps.DirectionsRenderer;
  trafficLayer!: google.maps.TrafficLayer;

  showTraffic = false;
  weatherData: any = null;

  // Default center: Morocco (approximate center)
  // Morocco Bounds: Latitude: 27.666667, Longitude: -9.7
  // Casablanca: 33.5731, -7.5898
  defaultCenter = { lat: 33.5731, lng: -7.5898 }; 

  constructor(private meteoService: MeteoService) { }

  ngOnInit(): void {
    this.meteoService.getLiveWeather(this.defaultCenter.lat, this.defaultCenter.lng).subscribe({
      next: (data) => {
        this.weatherData = {
          city: 'Casablanca', // Default or derived from reverse geocoding
          temp: data.current.temperature,
          condition: data.current.condition
        };
      },
      error: (err) => console.error('Error fetching weather:', err)
    });
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  initMap(): void {
    const mapOptions: google.maps.MapOptions = {
      center: this.defaultCenter,
      zoom: 13,
      mapTypeControl: false,
      streetViewControl: false,
      fullscreenControl: false,
      zoomControl: true,
      styles: [
        {
          featureType: "poi",
          elementType: "labels",
          stylers: [{ visibility: "off" }]
        }
      ]
    };

    this.map = new google.maps.Map(document.getElementById('map') as HTMLElement, mapOptions);
    
    this.directionsService = new google.maps.DirectionsService();
    this.directionsRenderer = new google.maps.DirectionsRenderer({
      map: this.map,
      suppressMarkers: false,
      polylineOptions: {
        strokeColor: "#4285f4",
        strokeWeight: 5
      }
    });

    this.trafficLayer = new google.maps.TrafficLayer();


    this.getUserLocation();
  }



  public calculateRoute(originCoords: {lat: number, lng: number}, destination: string, transportMode: string = 'driving'): Observable<google.maps.DirectionsResult | null> {
    const mode = this.getTravelMode(transportMode);
    const resultSubject = new Subject<google.maps.DirectionsResult | null>();
    
    // Fetch weather for the origin
    this.updateWeather(originCoords.lat, originCoords.lng, "Départ");

    this.directionsService.route(
      {
        origin: originCoords,
        destination: destination,
        travelMode: mode,
        provideRouteAlternatives: true,
        drivingOptions: mode === google.maps.TravelMode.DRIVING ? {
             departureTime: new Date(), // Important for duration_in_traffic
             trafficModel: google.maps.TrafficModel.BEST_GUESS
        } : undefined
      },
      (response, status) => {
          this.handleRouteResponse(response, status);
          if (status === "OK") {
              resultSubject.next(response);
          } else {
              resultSubject.next(null);
          }
          resultSubject.complete();
      }
    );

    return resultSubject.asObservable();
  }

  public calculateRouteFromStrings(origin: string, destination: string, transportMode: string = 'driving'): Observable<google.maps.DirectionsResult | null> {
    const mode = this.getTravelMode(transportMode);
    const resultSubject = new Subject<google.maps.DirectionsResult | null>();

    // Fetch weather for the origin
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ address: origin }, (results, status) => {
      if (status === "OK" && results && results[0]) {
        const lat = results[0].geometry.location.lat();
        const lng = results[0].geometry.location.lng();
        this.updateWeather(lat, lng, origin);
      }
    });

    this.directionsService.route(
      {
        origin: origin,
        destination: destination,
        travelMode: mode,
        provideRouteAlternatives: true,
        drivingOptions: mode === google.maps.TravelMode.DRIVING ? {
             departureTime: new Date(),
             trafficModel: google.maps.TrafficModel.BEST_GUESS
        } : undefined
      },
      (response, status) => {
          this.handleRouteResponse(response, status);
           if (status === "OK") {
              resultSubject.next(response);
          } else {
              resultSubject.next(null);
          }
           resultSubject.complete();
      }
    );
    
    return resultSubject.asObservable();
  }

  private handleRouteResponse(response: google.maps.DirectionsResult | null, status: google.maps.DirectionsStatus): void {
    if (status === "OK" && response) {
      this.directionsRenderer.setDirections(response);
      const bounds = response.routes[0].bounds;
      this.map.fitBounds(bounds);
    } else {
      console.error("Directions request failed due to " + status);
    }
  }

  private getTravelMode(mode: string): google.maps.TravelMode {
    switch (mode.toLowerCase()) {
      case 'walking': return google.maps.TravelMode.WALKING;
      case 'transit': return google.maps.TravelMode.TRANSIT;
      default: return google.maps.TravelMode.DRIVING;
    }
  }

  getUserLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const pos = {
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          };

          // Add a marker for user location
          new google.maps.Marker({
            position: pos,
            map: this.map,
            title: "Votre position",
            icon: {
                path: google.maps.SymbolPath.CIRCLE,
                scale: 7,
                fillColor: "#4285F4",
                fillOpacity: 1,
                strokeColor: "white",
                strokeWeight: 2,
            },
          });

          this.map.setCenter(pos);
          

          
        },
        () => {
          console.log("Error: The Geolocation service failed.");
        }
      );
    } else {
      console.log("Error: Your browser doesn't support geolocation.");
    }
  }

  toggleTraffic(): void {
    this.showTraffic = !this.showTraffic;
    if (this.showTraffic) {
      this.trafficLayer.setMap(this.map);
    } else {
      this.trafficLayer.setMap(null);
    }
  }

  updateWeather(lat: number, lng: number, cityName?: string): void {
    this.meteoService.getLiveWeather(lat, lng).subscribe({
      next: (data) => {
        this.weatherData = {
          city: cityName || 'Localisation actuelle', 
          temp: data.current.temperature,
          condition: data.current.condition
        };
      },
      error: (err) => console.error('Error fetching weather:', err)
    });
  }
}
