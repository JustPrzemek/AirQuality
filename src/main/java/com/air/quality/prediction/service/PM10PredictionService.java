package com.air.quality.prediction.service;

import com.air.quality.prediction.dto.PM10PredictionResponse;
import com.air.quality.prediction.model.PM10PredictionRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PM10PredictionService {

    private PM10PredictionRequest latestPredictions;

    public PM10PredictionResponse processPredictions(PM10PredictionRequest request) {

        this.latestPredictions = request;

        System.out.println("=== NOWE PREDYKCJE PM10 ===");
        System.out.println("Czas generacji: " + request.getGeneratedAt());
        System.out.println("Ostatni pomiar: " + request.getLastMeasuredPM10() + " μg/m³");
        System.out.println("Horyzont predykcji: " + request.getPredictionHorizonMinutes() + " minut");

        if (request.getSummary() != null) {
            System.out.println("Średnia predykcja: " + request.getSummary().getAvgPredictedPM10() + " μg/m³");
            System.out.println("Min predykcja: " + request.getSummary().getMinPredictedPM10() + " μg/m³");
            System.out.println("Max predykcja: " + request.getSummary().getMaxPredictedPM10() + " μg/m³");
            System.out.println("Trend: " + request.getSummary().getTrend());
        }

        System.out.println("Liczba punktów predykcji: " + request.getPredictions().size());
        System.out.println("========================");

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return new PM10PredictionResponse(
                "SUCCESS",
                "Predykcje PM10 zostały pomyślnie przetworzone",
                currentTime
        );
    }

    public PM10PredictionRequest getLatestPredictions() {
        return latestPredictions;
    }
}