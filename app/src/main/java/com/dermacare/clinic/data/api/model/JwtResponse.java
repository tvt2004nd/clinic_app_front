package com.dermacare.clinic.data.api.model;
 
import java.util.List;
 
public class JwtResponse {
    private String token;
    private String type;
    private long userId;
    private String username;
    private String email;
    private List<String> roles;
 
    public String getToken() { return token; }
    public String getType() { return type; }
    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }
}
