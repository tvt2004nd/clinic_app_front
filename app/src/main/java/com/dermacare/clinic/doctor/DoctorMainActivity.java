package com.dermacare.clinic.doctor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.dermacare.clinic.chat.ConversationListFragment;
import com.dermacare.clinic.patient.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DoctorMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavDoctor);
        if (savedInstanceState == null) {
            showFragment(new DoctorDashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                fragment = new DoctorDashboardFragment();
            } else if (id == R.id.nav_schedule) {
                fragment = DoctorScheduleFragment.newInstance();
            } else if (id == R.id.nav_patients) {
                fragment = DoctorPatientsFragment.newInstance();
            } else if (id == R.id.nav_chat) {
                fragment = ConversationListFragment.newInstance(true);
            } else {
                fragment = ProfileFragment.newInstance(true);
            }
            showFragment(fragment);
            return true;
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_doctor, fragment)
                .commit();
    }
}
