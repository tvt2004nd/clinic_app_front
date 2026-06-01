package com.dermacare.clinic.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MedicalRecord {
    @SerializedName("recordCode")
    private String recordCode;
    
    @SerializedName("patientId")
    private Long patientId; // Bắt buộc
    
    @SerializedName("doctorId")
    private Long doctorId; // Bắt buộc
    
    @SerializedName("appointmentId")
    private Long appointmentId;

    @SerializedName("symptoms")
    private String symptoms; // Triệu chứng chung (Bắt buộc)
    
    @SerializedName("finalDiagnosis")
    private String finalDiagnosis; // Chẩn đoán của bác sĩ (Bắt buộc)

    @SerializedName("finalDiseaseId")
    private Integer finalDiseaseId; // ID từ danh mục ICD-10
    
    @SerializedName("treatmentPlan")
    private String treatmentPlan;
    
    @SerializedName("followUpDate")
    private String followUpDate; // Định dạng YYYY-MM-DD

    // Chi tiết tổn thương da (Lesion details)
    @SerializedName("lesionDescription")
    private String lesionDescription;
    
    @SerializedName("lesionLocations")
    private String lesionLocations; // JSON hoặc CSV vị trí
    
    @SerializedName("lesionFeatures")
    private String lesionFeatures; // Các đặc điểm (Sẩn, mảng...)
    
    @SerializedName("lesionColor")
    private String lesionColor;
    
    @SerializedName("lesionSizeCm")
    private Double lesionSizeCm;
    
    @SerializedName("lesionShape")
    private String lesionShape;

    // Flags
    @SerializedName("explainedToPatient")
    private Boolean explainedToPatient;
    
    @SerializedName("followupScheduled")
    private Boolean followupScheduled;

    // Thông tin bổ sung cho UI/PDF (có thể không lưu DB nếu Backend ko có trường này)
    private String fullName;
    private String birthDate;
    private String gender;
    private String address;
    private String bhyt;
    private String allergy;
    private String pastHistory;
    private String familyHistory;
    private String diseaseName;

    // Getters and Setters
    public String getRecordCode() { return recordCode; }
    public void setRecordCode(String recordCode) { this.recordCode = recordCode; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getFinalDiagnosis() { return finalDiagnosis; }
    public void setFinalDiagnosis(String finalDiagnosis) { this.finalDiagnosis = finalDiagnosis; }

    public Integer getFinalDiseaseId() { return finalDiseaseId; }
    public void setFinalDiseaseId(Integer finalDiseaseId) { this.finalDiseaseId = finalDiseaseId; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public String getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }

    public String getLesionDescription() { return lesionDescription; }
    public void setLesionDescription(String lesionDescription) { this.lesionDescription = lesionDescription; }

    public String getLesionLocations() { return lesionLocations; }
    public void setLesionLocations(String lesionLocations) { this.lesionLocations = lesionLocations; }

    public String getLesionFeatures() { return lesionFeatures; }
    public void setLesionFeatures(String lesionFeatures) { this.lesionFeatures = lesionFeatures; }

    public String getLesionColor() { return lesionColor; }
    public void setLesionColor(String lesionColor) { this.lesionColor = lesionColor; }

    public Double getLesionSizeCm() { return lesionSizeCm; }
    public void setLesionSizeCm(Double lesionSizeCm) { this.lesionSizeCm = lesionSizeCm; }

    public String getLesionShape() { return lesionShape; }
    public void setLesionShape(String lesionShape) { this.lesionShape = lesionShape; }

    public Boolean getExplainedToPatient() { return explainedToPatient; }
    public void setExplainedToPatient(Boolean explainedToPatient) { this.explainedToPatient = explainedToPatient; }

    public Boolean getFollowupScheduled() { return followupScheduled; }
    public void setFollowupScheduled(Boolean followupScheduled) { this.followupScheduled = followupScheduled; }

    // Bổ sung cho PDF
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBhyt() { return bhyt; }
    public void setBhyt(String bhyt) { this.bhyt = bhyt; }
    public String getAllergy() { return allergy; }
    public void setAllergy(String allergy) { this.allergy = allergy; }
    public String getPastHistory() { return pastHistory; }
    public void setPastHistory(String pastHistory) { this.pastHistory = pastHistory; }
    public String getFamilyHistory() { return familyHistory; }
    public void setFamilyHistory(String familyHistory) { this.familyHistory = familyHistory; }
    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
}
