package com.air.quality.prediction.controller;


import com.air.quality.prediction.dto.PM10PredictionResponse;
import com.air.quality.prediction.model.PM10PredictionRequest;
import com.air.quality.prediction.service.PM10PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pm10")
@CrossOrigin(origins = "*")
public class PM10PredictionController {

    @Autowired
    private PM10PredictionService predictionService;

    @PostMapping("/predictions")
    public ResponseEntity<PM10PredictionResponse> receivePredictions(
            @RequestBody PM10PredictionRequest request) {

        System.out.println("Otrzymano nowe predykcje PM10 o: " + request.getGeneratedAt());
        System.out.println("Ostatni pomiar PM10: " + request.getLastMeasuredPM10());
        System.out.println("Liczba predykcji: " + request.getPredictions().size());
        System.out.println("Trend: " + request.getSummary().getTrend());

        PM10PredictionResponse response = predictionService.processPredictions(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/predictions/latest")
    public ResponseEntity<PM10PredictionRequest> getLatestPredictions() {
        PM10PredictionRequest latest = predictionService.getLatestPredictions();
        if (latest != null) {
            return ResponseEntity.ok(latest);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("PM10 Prediction Service is running");
    }
}
