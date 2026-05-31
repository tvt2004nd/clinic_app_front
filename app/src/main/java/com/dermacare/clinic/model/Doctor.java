package com.dermacare.clinic.model;

public class Doctor {
    public final Long doctorId;
    public final String name;
    public final String specialty;
    public final String rating;
    public final boolean availableToday;
    public final String avatarUrl;
    public final double fee;

    public Doctor(Long doctorId, String name, String specialty, String rating, boolean availableToday, String avatarUrl, double fee) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
        this.rating = rating;
        this.availableToday = availableToday;
        this.avatarUrl = avatarUrl;
        this.fee = fee;
    }
}
