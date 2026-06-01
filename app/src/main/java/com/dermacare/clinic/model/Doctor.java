package com.dermacare.clinic.model;

public class Doctor {
    public final String name;
    public final String specialty;
    public final String rating;
    public final boolean availableToday;
    public final String avatarUrl;

    public Doctor(String name, String specialty, String rating, boolean availableToday, String avatarUrl) {
        this.name = name;
        this.specialty = specialty;
        this.rating = rating;
        this.availableToday = availableToday;
        this.avatarUrl = avatarUrl;
    }
}
