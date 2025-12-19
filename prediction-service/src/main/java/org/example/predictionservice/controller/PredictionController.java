package org.example.predictionservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.predictionservice.model.EnrichedPrediction;
import org.example.predictionservice.model.Prediction;
import org.example.predictionservice.model.PredictionRequest;
import org.example.predictionservice.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping("/status")
    public String status() {
        return "Prediction Service is running with dynamic prediction capabilities.";
    }

    /**
     * Legacy endpoint for basic prediction (backward compatibility)
     */
    @PostMapping("/analyze")
    public ResponseEntity<Prediction> analyzeTrip(@RequestBody PredictionRequest request) {
        Prediction prediction = predictionService.analyzeTrip(
                request.getOrigin(),
                request.getDestination());
        return ResponseEntity.ok(prediction);
    }

    /**
     * New enriched prediction endpoint with full analysis
     */
    @PostMapping("/analyze/enriched")
    public ResponseEntity<EnrichedPrediction> analyzeEnrichedTrip(@RequestBody PredictionRequest request) {
        EnrichedPrediction prediction = predictionService.analyzeEnrichedTrip(
                request.getOrigin(),
                request.getDestination(),
                request.getDepartureDate(),
                request.getDepartureTime());
        return ResponseEntity.ok(prediction);
    }

    /**
     * GET endpoint for quick predictions (testing)
     */
    @GetMapping("/quick")
    public ResponseEntity<EnrichedPrediction> quickPrediction(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) String time) {

        EnrichedPrediction prediction = predictionService.analyzeEnrichedTrip(
                origin, destination, null, time);
        return ResponseEntity.ok(prediction);
    }
}
