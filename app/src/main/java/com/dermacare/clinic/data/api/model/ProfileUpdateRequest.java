package com.dermacare.clinic.data.api.model;
 
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String gender;
    private String dateOfBirth; // Format "yyyy-MM-dd"
    private String address;
    private String avatarUrl;
 
    public ProfileUpdateRequest(String fullName, String phone, String gender, String dateOfBirth, String address, String avatarUrl) {
        this.fullName = fullName;
        this.phone = phone;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.avatarUrl = avatarUrl;
    }
 
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getAvatarUrl() { return avatarUrl; }
}
