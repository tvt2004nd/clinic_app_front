package com.dermacare.clinic.data.api.model;
 
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
 
    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
 
    public String getOldPassword() { return oldPassword; }
    public String getNewPassword() { return newPassword; }
}
