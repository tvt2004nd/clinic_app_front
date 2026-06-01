package com.dermacare.clinic.data.api.model;

import java.util.List;

public class HealthProfileResponse {
    public String patientCode;
    public String bloodType;
    public String medicalHistory;
    public String insuranceNumber;
    public String emergencyContact;
    public String emergencyPhone;
    public List<AllergyDTO> allergies;
}
