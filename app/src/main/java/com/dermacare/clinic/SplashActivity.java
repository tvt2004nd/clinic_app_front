package com.dermacare.clinic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.dermacare.clinic.auth.LoginActivity;
import com.dermacare.clinic.auth.OnboardingActivity;
import com.dermacare.clinic.doctor.DoctorMainActivity;
import com.dermacare.clinic.patient.PatientMainActivity;
import com.dermacare.clinic.util.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MS = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager session = new SessionManager(this);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (!session.isOnboarded()) {
                intent = new Intent(this, OnboardingActivity.class);
            } else if (session.isLoggedIn()) {
                if (SessionManager.ROLE_DOCTOR.equals(session.getRole())) {
                    intent = new Intent(this, DoctorMainActivity.class);
                } else {
                    intent = new Intent(this, PatientMainActivity.class);
                }
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
