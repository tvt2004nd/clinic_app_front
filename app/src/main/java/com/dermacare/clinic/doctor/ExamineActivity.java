package com.dermacare.clinic.doctor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.dermacare.clinic.R;
import com.google.android.material.button.MaterialButton;

public class ExamineActivity extends AppCompatActivity {
    private int currentStep = 0;
    private MaterialButton btnNext, btnBack;

    private final String[] titles = {
        "Tiếp nhận bệnh nhân",
        "Ghi nhận triệu chứng",
        "Tham chiếu kết quả AI",
        "Chẩn đoán cuối cùng",
        "Kê đơn thuốc",
        "Lịch tái khám"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examine);

        btnNext = findViewById(R.id.btnNextExamine);
        btnBack = findViewById(R.id.btnBack);

        updateStep();

        btnNext.setOnClickListener(v -> {
            if (currentStep < 5) {
                currentStep++;
                updateStep();
            } else {
                finish();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateStep();
            } else {
                finish();
            }
        });
    }

    private void updateStep() {
        btnNext.setText(currentStep == 5 ? "Hoàn thành" : "Tiếp tục");
        
        Fragment fragment;
        switch (currentStep) {
            case 0: fragment = new ExamineAdmissionFragment(); break;
            case 1: fragment = new ExamineSymptomsFragment(); break;
            case 2: fragment = new ExamineAiResultFragment(); break;
            case 3: fragment = new ExamineDiagnosisFragment(); break;
            case 4: fragment = new ExaminePrescriptionFragment(); break;
            case 5: fragment = new ExamineFollowUpFragment(); break;
            default: fragment = new ExamineAdmissionFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.examine_container, fragment)
                .commit();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titles[currentStep]);
        }
    }
}
