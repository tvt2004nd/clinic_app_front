package com.dermacare.clinic.data.api.model;

public class AppointmentRequest {
    public Long doctorId;
    public Long scheduleId;
    public String reason;
    public String patientName;
    public String phone;

    public AppointmentRequest(Long doctorId, Long scheduleId, String reason, String patientName, String phone) {
        this.doctorId = doctorId;
        this.scheduleId = scheduleId;
        this.reason = reason;
        this.patientName = patientName;
        this.phone = phone;
    }
}
