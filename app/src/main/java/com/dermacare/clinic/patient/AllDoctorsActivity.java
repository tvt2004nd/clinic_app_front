package com.dermacare.clinic.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.DoctorAdapter;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.DoctorResponse;
import com.dermacare.clinic.model.Doctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllDoctorsActivity extends AppCompatActivity {

    private DoctorAdapter doctorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_doctors);

        // Setup nút Back trên thanh tiêu đề
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        RecyclerView rvAllDoctors = findViewById(R.id.rvAllDoctors);
        rvAllDoctors.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Adapter và bắt sự kiện khi click vào bác sĩ (Chuyển sang trang Profile Bác sĩ)
        doctorAdapter = new DoctorAdapter(new ArrayList<>(), this::navigateToDoctorProfile);
        rvAllDoctors.setAdapter(doctorAdapter);

        // Gọi API lấy TẤT CẢ bác sĩ
        fetchAllDoctors();
    }

    private void fetchAllDoctors() {
        ApiClient.getPublicService(this).getDoctors()
                .enqueue(new Callback<List<DoctorResponse>>() {
                    @Override
                    public void onResponse(Call<List<DoctorResponse>> call, Response<List<DoctorResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<DoctorResponse> allDoctors = response.body();

                            // Sắp xếp bác sĩ theo Rating giảm dần (Tùy chọn)
                            Collections.sort(allDoctors, (d1, d2) -> {
                                double r1 = d1.rating != null ? d1.rating : 0.0;
                                double r2 = d2.rating != null ? d2.rating : 0.0;
                                return Double.compare(r2, r1);
                            });

                            List<Doctor> mappedDoctors = new ArrayList<>();

                            // KHÔNG LIMIT - DUYỆT QUA TẤT CẢ DANH SÁCH
                            for (DoctorResponse dr : allDoctors) {
                                String finalAvatarUrl = "";
                                if (dr.avatarUrl != null && !dr.avatarUrl.trim().isEmpty()) {
                                    if (dr.avatarUrl.startsWith("http")) {
                                        finalAvatarUrl = dr.avatarUrl;
                                    } else if (dr.avatarUrl.startsWith("/")) {
                                        finalAvatarUrl = ApiClient.BASE_URL + dr.avatarUrl.substring(1);
                                    } else {
                                        finalAvatarUrl = ApiClient.BASE_URL + dr.avatarUrl;
                                    }
                                } else {
                                    String safeName = dr.fullName != null && !dr.fullName.isEmpty() ? dr.fullName.substring(0, 1) : "D";
                                    finalAvatarUrl = "https://ui-avatars.com/api/?name=" + safeName + "&background=random&color=fff";
                                }

                                mappedDoctors.add(new Doctor(
                                        dr.doctorId,
                                        dr.fullName,
                                        dr.specialty,
                                        dr.rating != null ? String.valueOf(dr.rating) : "5.0",
                                        true,
                                        finalAvatarUrl,
                                        dr.fee != null ? dr.fee : 150000
                                ));
                            }

                            // Cập nhật danh sách lên giao diện
                            doctorAdapter.setDoctors(mappedDoctors);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DoctorResponse>> call, Throwable t) {
                        Toast.makeText(AllDoctorsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm chuyển sang trang chi tiết bác sĩ
    private void navigateToDoctorProfile(Doctor doctor) {
        if (doctor.doctorId == null) return;

        String initials = "";
        if (doctor.name != null && !doctor.name.isEmpty()) {
            String[] parts = doctor.name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = String.valueOf(parts[parts.length - 2].charAt(0)) + parts[parts.length - 1].charAt(0);
            } else {
                initials = doctor.name.substring(0, Math.min(2, doctor.name.length()));
            }
        }

        Intent intent = new Intent(this, DoctorProfileActivity.class);
        intent.putExtra(BookingActivity.EXTRA_DOCTOR_ID, doctor.doctorId);
        intent.putExtra(BookingActivity.EXTRA_DOCTOR_NAME, doctor.name);
        intent.putExtra(BookingActivity.EXTRA_SPECIALTY, doctor.specialty);
        intent.putExtra(BookingActivity.EXTRA_AVATAR_URL, doctor.avatarUrl);
        intent.putExtra(BookingActivity.EXTRA_INITIALS, initials.toUpperCase());
        intent.putExtra(BookingActivity.EXTRA_FEE, doctor.fee);
        startActivity(intent);
    }
}