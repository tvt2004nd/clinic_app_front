package com.dermacare.clinic.data.api.model;

public class AllergyDTO {
    public Long allergyId;
    public String allergen;
    public String reaction;
    public String severity;
    public String note;

    public AllergyDTO() {
    }

    public AllergyDTO(String allergen, String reaction, String severity, String note) {
        this.allergen = allergen;
        this.reaction = reaction;
        this.severity = severity;
        this.note = note;
    }
}
