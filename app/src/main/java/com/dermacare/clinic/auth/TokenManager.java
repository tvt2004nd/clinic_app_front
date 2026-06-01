package com.dermacare.clinic.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {
    private static final String PREF_NAME = "DermaCareAuth";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUserId(Long id) {
        prefs.edit().putLong(KEY_USER_ID, id).apply();
    }

    public Long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
