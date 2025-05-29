package com.air.quality.prediction.dto;

import java.time.LocalDate;
import java.util.Map;

public class DailyStatsDTO {
    private LocalDate date;
    private Map<String, Double> averages;
    private Map<String, Double> minimums;
    private Map<String, Double> maximums;

    public DailyStatsDTO() {
    }

    public DailyStatsDTO(LocalDate date, Map<String, Double> averages,
                         Map<String, Double> minimums, Map<String, Double> maximums) {
        this.date = date;
        this.averages = averages;
        this.minimums = minimums;
        this.maximums = maximums;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<String, Double> getAverages() {
        return averages;
    }

    public void setAverages(Map<String, Double> averages) {
        this.averages = averages;
    }

    public Map<String, Double> getMinimums() {
        return minimums;
    }

    public void setMinimums(Map<String, Double> minimums) {
        this.minimums = minimums;
    }

    public Map<String, Double> getMaximums() {
        return maximums;
    }

    public void setMaximums(Map<String, Double> maximums) {
        this.maximums = maximums;
    }
}
