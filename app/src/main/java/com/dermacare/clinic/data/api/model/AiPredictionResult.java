package com.dermacare.clinic.data.api.model;

import java.util.List;

public class AiPredictionResult {
    private String predicted_class;
    private double confidence;
    private List<AiTopKItem> top_k;

    public String getPredicted_class() { return predicted_class; }
    public void setPredicted_class(String predicted_class) { this.predicted_class = predicted_class; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public List<AiTopKItem> getTop_k() { return top_k; }
    public void setTop_k(List<AiTopKItem> top_k) { this.top_k = top_k; }
}
