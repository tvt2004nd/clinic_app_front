package com.dermacare.clinic.util;
 
import android.content.Context;
import android.content.SharedPreferences;
 
public class SessionManager {
    private static final String PREFS = "dermacare_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ONBOARDED = "onboarded";
    private static final String KEY_ROLE = "role";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DOB = "date_of_birth";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_AVATAR = "avatar_url";
 
    public static final String ROLE_PATIENT = "patient";
    public static final String ROLE_DOCTOR = "doctor";
 
    private final SharedPreferences prefs;
 
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
 
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }
 
    public boolean isOnboarded() {
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }
 
    public String getRole() {
        return prefs.getString(KEY_ROLE, ROLE_PATIENT);
    }
 
    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }
 
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }
 
    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }
 
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }
 
    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }
 
    public String getGender() {
        return prefs.getString(KEY_GENDER, "OTHER");
    }
 
    public String getDob() {
        return prefs.getString(KEY_DOB, "");
    }
 
    public String getAddress() {
        return prefs.getString(KEY_ADDRESS, "");
    }
 
    public String getAvatar() {
        return prefs.getString(KEY_AVATAR, "");
    }
 
    public void setOnboarded(boolean value) {
        prefs.edit().putBoolean(KEY_ONBOARDED, value).apply();
    }
 
    public void login(String token, long userId, String name, String email, String role) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }
 
    public void saveProfile(String name, String phone, String gender, String dob, String address, String avatar) {
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_PHONE, phone)
                .putString(KEY_GENDER, gender)
                .putString(KEY_DOB, dob)
                .putString(KEY_ADDRESS, address)
                .putString(KEY_AVATAR, avatar)
                .apply();
    }
 
    public void logout() {
        prefs.edit().clear().apply();
    }
}
