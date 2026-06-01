package com.dermacare.clinic.data.api.model;

import java.util.List;

public class HealthProfileRequest {
    public String bloodType;
    public String medicalHistory;
    public String insuranceNumber;
    public String emergencyContact;
    public String emergencyPhone;
    public List<AllergyDTO> allergies;

    public HealthProfileRequest(String bloodType, String medicalHistory, String insuranceNumber, String emergencyContact, String emergencyPhone, List<AllergyDTO> allergies) {
        this.bloodType = bloodType;
        this.medicalHistory = medicalHistory;
        this.insuranceNumber = insuranceNumber;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
        this.allergies = allergies;
    }
}
