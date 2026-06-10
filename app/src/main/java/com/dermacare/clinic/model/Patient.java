package com.dermacare.clinic.model;

import java.io.Serializable;

public class Patient implements Serializable {
    private String name;
    private String ageAndSymptom;
    private String lastExamDate;

    public Patient(String name, String ageAndSymptom, String lastExamDate) {
        this.name = name;
        this.ageAndSymptom = ageAndSymptom;
        this.lastExamDate = lastExamDate;
    }

    public String getName() { return name; }
    public String getAgeAndSymptom() { return ageAndSymptom; }
    public String getLastExamDate() { return lastExamDate; }
    
    @Override
    public String toString() {
        return name + "\n" + ageAndSymptom + "\nKhám gần nhất: " + lastExamDate;
    }
}
