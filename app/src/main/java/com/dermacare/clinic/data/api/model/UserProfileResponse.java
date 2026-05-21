package com.dermacare.clinic.data.api.model;
 
public class UserProfileResponse {
    private long userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String gender;
    private String dateOfBirth; // Format "yyyy-MM-dd"
    private String address;
    private String avatarUrl;
    private String role;
 
    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }
}
