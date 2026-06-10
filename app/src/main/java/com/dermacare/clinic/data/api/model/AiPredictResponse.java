package com.dermacare.clinic.data.api.model;

public class AiPredictResponse {
    private boolean success;
    private AiPredictionResult prediction;
    private String error;
    private Double inference_time_ms;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public AiPredictionResult getPrediction() { return prediction; }
    public void setPrediction(AiPredictionResult prediction) { this.prediction = prediction; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Double getInference_time_ms() { return inference_time_ms; }
    public void setInference_time_ms(Double inference_time_ms) { this.inference_time_ms = inference_time_ms; }
}
