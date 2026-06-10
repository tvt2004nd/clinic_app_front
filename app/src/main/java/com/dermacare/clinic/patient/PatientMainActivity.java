package com.dermacare.clinic.patient;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.dermacare.clinic.chat.ConversationListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PatientMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavPatient);
        FloatingActionButton fabChat = findViewById(R.id.fabChat);

        if (savedInstanceState == null) {
            showFragment(new PatientHomeFragment());
        }

        // Bấm vào FAB chuyển sang màn hình ChatActivity (đang ở cùng thư mục patient)
        fabChat.setOnClickListener(v -> {
            Intent intent = new Intent(PatientMainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

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
            } else if (id == R.id.nav_chat) {
                fragment = ConversationListFragment.newInstance(false);
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

    public void selectTab(int tabId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavPatient);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(tabId);
        }
    }
}