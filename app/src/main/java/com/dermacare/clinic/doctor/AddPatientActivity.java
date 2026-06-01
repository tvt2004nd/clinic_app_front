package com.dermacare.clinic.doctor;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dermacare.clinic.data.MockData;
import com.dermacare.clinic.databinding.ActivityAddPatientBinding;
import com.dermacare.clinic.model.Patient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddPatientActivity extends AppCompatActivity {
    private ActivityAddPatientBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPatientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSavePatient.setOnClickListener(v -> {
            String name = binding.etNewName.getText().toString().trim();
            String detail = binding.etNewDetail.getText().toString().trim();

            if (name.isEmpty() || detail.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            Patient newPatient = new Patient(name, detail, today);
            
            MockData.addPatient(newPatient);
            
            Toast.makeText(this, "Đã thêm bệnh nhân thành công", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
