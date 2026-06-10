package com.dermacare.clinic.data.api.model;

public class AiTopKItem {
    private String class_name;
    private double probability;

    public String getClass_name() { return class_name; }
    public void setClass_name(String class_name) { this.class_name = class_name; }

    public double getProbability() { return probability; }
    public void setProbability(double probability) { this.probability = probability; }
}
