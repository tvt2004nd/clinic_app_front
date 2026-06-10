package com.dermacare.clinic;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.dermacare.clinic.databinding.ActivityMedicalRecordBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dermacare.clinic.util.PdfGenerator;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import java.util.ArrayList;
import java.util.List;

import com.dermacare.clinic.util.LocaleHelper;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.MedicalRecordService;
import com.dermacare.clinic.model.MedicalRecord;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicalRecordActivity extends AppCompatActivity {
    private ActivityMedicalRecordBinding binding;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "vi"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicalRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setupUI();
        setupToggleableRadioButtons();
    }

    private void setupToggleableRadioButtons() {
        // Giới tính
        makeExclusiveCheckBoxGroup((android.view.ViewGroup) binding.rgGender);
        // Màu sắc
        makeExclusiveCheckBoxGroup((android.view.ViewGroup) binding.rgColor);
        // Cơ địa
        makeExclusiveCheckBoxGroup((android.view.ViewGroup) binding.rgSkinType);
    }

    private void makeExclusiveCheckBoxGroup(android.view.ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            android.view.View view = group.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox cb = (CheckBox) view;
                cb.setOnClickListener(v -> {
                    boolean wasChecked = false;
                    if (cb.getTag() != null) {
                        wasChecked = (boolean) cb.getTag();
                    }

                    if (wasChecked) {
                        cb.setChecked(false);
                        cb.setTag(false);
                    } else {
                        // Reset tag và trạng thái của tất cả các CheckBox khác trong group
                        for (int j = 0; j < group.getChildCount(); j++) {
                            android.view.View other = group.getChildAt(j);
                            if (other instanceof CheckBox) {
                                CheckBox otherCb = (CheckBox) other;
                                otherCb.setChecked(false);
                                otherCb.setTag(false);
                            }
                        }
                        cb.setChecked(true);
                        cb.setTag(true);
                    }
                });
            }
        }
    }

    private void setupUI() {
        // Tự động điền tên bệnh nhân nếu được mở từ Lịch hẹn
        String patientName = getIntent().getStringExtra("PATIENT_NAME");
        if (patientName != null && !patientName.isEmpty()) {
            binding.etFullName.setText(patientName);
        }

        // Giúp cuộn mượt bằng chuột trên máy ảo
        binding.nestedScrollView.setFocusable(true);
        binding.nestedScrollView.setFocusableInTouchMode(true);
        binding.nestedScrollView.requestFocus();

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 1. Chọn ngày sinh
        binding.etBirthDate.setOnClickListener(v -> showDatePicker(binding.etBirthDate));

        // 2. Chọn ngày tái khám
        binding.etFollowUpDate.setOnClickListener(v -> showDatePicker(binding.etFollowUpDate));

        // 3. Thiết lập danh mục bệnh (ICD-10)
        String[] diseases = {"Viêm da cơ địa (L20)", "Vảy nến (L40)", "Mụn trứng cá (L70)", "Mề đay (L50)", "Nấm da (B35)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, diseases);
        binding.actvDisease.setAdapter(adapter);

        // 4. Xử lý nút LƯU & XUẤT BỆNH ÁN
        binding.btnExportPdf.setOnClickListener(v -> exportAndSave());
    }

    private void showDatePicker(android.widget.EditText editText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void exportAndSave() {
        // Thu thập dữ liệu
        MedicalRecord record = new MedicalRecord();
        
        // 1. Các trường bắt buộc cho Backend
        record.setRecordCode(binding.etRecordCode.getText().toString());
        record.setPatientId(1L); // Placeholder: Thực tế lấy từ Intent/Session
        record.setDoctorId(1L);  // Placeholder: Thực tế lấy từ Session
        record.setSymptoms("Triệu chứng: " + getSelectedSymptoms());
        record.setFinalDiagnosis(binding.etDiagnosis.getText().toString());
        
        // 2. Các trường chi tiết tổn thương
        record.setLesionDescription(binding.etSymptomsDetail.getText().toString());
        record.setLesionLocations(getSelectedLocations());
        record.setLesionFeatures(getSelectedSymptoms());
        
        // Màu sắc
        String color = "";
        if (binding.rbRed.isChecked()) color = "Đỏ";
        else if (binding.rbBrown.isChecked()) color = "Nâu";
        else if (binding.rbWhite.isChecked()) color = "Trắng";
        else if (binding.rbPink.isChecked()) color = "Hồng";
        record.setLesionColor(color);
        
        // Kích thước (Double)
        try {
            String sizeStr = binding.etSize.getText().toString();
            if (!sizeStr.isEmpty()) {
                record.setLesionSizeCm(Double.parseDouble(sizeStr));
            }
        } catch (NumberFormatException e) {
            record.setLesionSizeCm(0.0);
        }
        
        record.setLesionShape(binding.etShape.getText().toString());
        
        // 3. Thông tin điều trị & ICD-10
        record.setTreatmentPlan(binding.etTreatment.getText().toString());
        record.setFollowUpDate(formatDateForBackend(binding.etFollowUpDate.getText().toString()));
        record.setFinalDiseaseId(getDiseaseId(binding.actvDisease.getText().toString()));
        
        // 4. Flags
        record.setExplainedToPatient(binding.cbExplained.isChecked());
        record.setFollowupScheduled(binding.cbFollowUpScheduled.isChecked());

        // 5. Thông tin bổ sung cho UI/PDF
        record.setFullName(binding.etFullName.getText().toString());
        record.setBirthDate(binding.etBirthDate.getText().toString());
        
        if (binding.rbMale.isChecked()) record.setGender("Nam");
        else if (binding.rbFemale.isChecked()) record.setGender("Nữ");
        
        record.setAddress(binding.etAddress.getText().toString());
        record.setBhyt(binding.etBhyt.getText().toString());
        record.setAllergy(binding.etAllergy.getText().toString());
        record.setPastHistory(binding.etPastHistory.getText().toString());
        record.setFamilyHistory(binding.etFamilyHistory.getText().toString());
        record.setDiseaseName(binding.actvDisease.getText().toString());

        // Xuất PDF
        Map<String, String> pdfData = new HashMap<>();
        pdfData.put("hoTen", record.getFullName());
        pdfData.put("ngaySinh", record.getBirthDate());
        pdfData.put("gioiTinh", record.getGender());
        pdfData.put("diaChi", record.getAddress());
        pdfData.put("bhyt", record.getBhyt());
        pdfData.put("lyDo", record.getSymptoms());
        pdfData.put("diUng", record.getAllergy());
        pdfData.put("tienSuGiaDinh", record.getFamilyHistory());
        pdfData.put("chanDoan", record.getFinalDiagnosis());
        pdfData.put("disease", record.getDiseaseName());
        pdfData.put("dieuTri", record.getTreatmentPlan());
        pdfData.put("followUp", record.getFollowUpDate());
        pdfData.put("recordCode", record.getRecordCode());
        pdfData.put("tenBacSi", "BS. Trần Quang Nguyên");

        PdfGenerator.exportMedicalRecordPdf(this, pdfData);
        
        // 2. Gửi dữ liệu lên Backend
        saveToBackend(record);
    }

    private void saveToBackend(MedicalRecord record) {
        MedicalRecordService service = ApiClient.getMedicalRecordService(this);
        service.saveMedicalRecord(record).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MedicalRecordActivity.this, "Đã lưu hồ sơ bệnh án thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MedicalRecordActivity.this, "Lỗi lưu DB: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MedicalRecordActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedSymptoms() {
        List<String> selected = new ArrayList<>();
        if (binding.cbSymp1.isChecked()) selected.add("Mảng");
        if (binding.cbSymp2.isChecked()) selected.add("Sẩn");
        if (binding.cbSymp3.isChecked()) selected.add("Mụn nước");
        if (binding.cbSymp4.isChecked()) selected.add("Vảy nến");
        if (binding.cbSymp5.isChecked()) selected.add("Mề đay");
        if (binding.cbSymp6.isChecked()) selected.add("Mụn mủ");
        if (binding.cbSymp7.isChecked()) selected.add("Ngứa");
        if (binding.cbSymp8.isChecked()) selected.add("Đau rát");
        
        return String.join(", ", selected);
    }

    private String getSelectedLocations() {
        List<String> locations = new ArrayList<>();
        if (binding.chipFace.isChecked()) locations.add("Mặt");
        if (binding.chipArm.isChecked()) locations.add("Tay");
        if (binding.chipBody.isChecked()) locations.add("Thân");
        return String.join(", ", locations);
    }

    private String formatDateForBackend(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                // Input: DD/MM/YYYY -> Output: YYYY-MM-DD
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Integer getDiseaseId(String diseaseName) {
        if (diseaseName.contains("L20")) return 1;
        if (diseaseName.contains("L40")) return 2;
        if (diseaseName.contains("L70")) return 3;
        if (diseaseName.contains("L50")) return 4;
        if (diseaseName.contains("B35")) return 5;
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_medical_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            exportAndSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
