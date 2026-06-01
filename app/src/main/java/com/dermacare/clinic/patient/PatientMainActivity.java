package com.dermacare.clinic.patient;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PatientMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavPatient);
        if (savedInstanceState == null) {
            showFragment(new PatientHomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                fragment = new PatientHomeFragment();
            } else if (id == R.id.nav_appointments) {
                fragment = PatientListFragment.newInstance("Lịch hẹn của tôi", PatientListFragment.TYPE_APPOINTMENTS);
            } else if (id == R.id.nav_records) {
                fragment = PatientListFragment.newInstance("Hồ sơ bệnh án", PatientListFragment.TYPE_RECORDS);
            } else if (id == R.id.nav_ai) {
                fragment = new PatientAiFragment();
            } else {
                fragment = ProfileFragment.newInstance(false);
            }
            showFragment(fragment);
            return true;
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_patient, fragment)
                .commit();
    }
}
