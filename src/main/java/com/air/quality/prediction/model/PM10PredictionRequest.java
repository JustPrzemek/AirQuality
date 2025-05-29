package com.air.quality.prediction.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PM10PredictionRequest {

    @JsonProperty("generatedAt")
    private String generatedAt;

    @JsonProperty("lastMeasuredPM10")
    private Double lastMeasuredPM10;

    @JsonProperty("lastMeasurementTime")
    private String lastMeasurementTime;

    @JsonProperty("predictionHorizonMinutes")
    private Integer predictionHorizonMinutes;

    @JsonProperty("predictions")
    private List<PredictionPoint> predictions;

    @JsonProperty("summary")
    private PredictionSummary summary;

    public PM10PredictionRequest() {}

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public Double getLastMeasuredPM10() { return lastMeasuredPM10; }
    public void setLastMeasuredPM10(Double lastMeasuredPM10) { this.lastMeasuredPM10 = lastMeasuredPM10; }

    public String getLastMeasurementTime() { return lastMeasurementTime; }
    public void setLastMeasurementTime(String lastMeasurementTime) { this.lastMeasurementTime = lastMeasurementTime; }

    public Integer getPredictionHorizonMinutes() { return predictionHorizonMinutes; }
    public void setPredictionHorizonMinutes(Integer predictionHorizonMinutes) { this.predictionHorizonMinutes = predictionHorizonMinutes; }

    public List<PredictionPoint> getPredictions() { return predictions; }
    public void setPredictions(List<PredictionPoint> predictions) { this.predictions = predictions; }

    public PredictionSummary getSummary() { return summary; }
    public void setSummary(PredictionSummary summary) { this.summary = summary; }

    public static class PredictionPoint {
        @JsonProperty("timestamp")
        private String timestamp;

        @JsonProperty("predictedPM10")
        private Double predictedPM10;

        public PredictionPoint() {}

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public Double getPredictedPM10() { return predictedPM10; }
        public void setPredictedPM10(Double predictedPM10) { this.predictedPM10 = predictedPM10; }
    }

    public static class PredictionSummary {
        @JsonProperty("avgPredictedPM10")
        private Double avgPredictedPM10;

        @JsonProperty("minPredictedPM10")
        private Double minPredictedPM10;

        @JsonProperty("maxPredictedPM10")
        private Double maxPredictedPM10;

        @JsonProperty("trend")
        private String trend;

        public PredictionSummary() {}

        public Double getAvgPredictedPM10() { return avgPredictedPM10; }
        public void setAvgPredictedPM10(Double avgPredictedPM10) { this.avgPredictedPM10 = avgPredictedPM10; }

        public Double getMinPredictedPM10() { return minPredictedPM10; }
        public void setMinPredictedPM10(Double minPredictedPM10) { this.minPredictedPM10 = minPredictedPM10; }

        public Double getMaxPredictedPM10() { return maxPredictedPM10; }
        public void setMaxPredictedPM10(Double maxPredictedPM10) { this.maxPredictedPM10 = maxPredictedPM10; }

        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }
}
