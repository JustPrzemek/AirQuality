package com.air.quality.prediction.controller;

import com.air.quality.prediction.dto.DailyStatsDTO;
import com.air.quality.prediction.model.AirQualityData;
import com.air.quality.prediction.service.AirQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/air-quality")
public class AirQualityController {

    private final AirQualityService airQualityService;

    @Autowired
    public AirQualityController(AirQualityService airQualityService) {
        this.airQualityService = airQualityService;
    }

    @GetMapping("/data")
    public ResponseEntity<List<AirQualityData>> getDataByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AirQualityData> data = airQualityService.getDataByDate(date);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/data/range")
    public ResponseEntity<List<AirQualityData>> getDataByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AirQualityData> data = airQualityService.getDataByDateRange(startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<DailyStatsDTO> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyStatsDTO stats = airQualityService.getDailyStats(date);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates() {
        List<LocalDate> dates = airQualityService.getAvailableDates();
        return ResponseEntity.ok(dates);
    }

    @PostMapping("/process-file")
    public ResponseEntity<String> triggerProcessFile(@RequestParam String filename) {
        java.io.File file = new java.io.File(filename);
        if (file.exists() && file.isFile()) {
            airQualityService.processCsvFile(file);
            return ResponseEntity.ok("File processing started for: " + filename);
        } else {
            return ResponseEntity.badRequest().body("File not found: " + filename);
        }
    }
}
