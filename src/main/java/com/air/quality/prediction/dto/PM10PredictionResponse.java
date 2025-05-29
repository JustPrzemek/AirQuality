package com.air.quality.prediction.dto;

public class PM10PredictionResponse {
    private String status;
    private String message;
    private String receivedAt;

    public PM10PredictionResponse() {}

    public PM10PredictionResponse(String status, String message, String receivedAt) {
        this.status = status;
        this.message = message;
        this.receivedAt = receivedAt;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getReceivedAt() { return receivedAt; }
    public void setReceivedAt(String receivedAt) { this.receivedAt = receivedAt; }
}
