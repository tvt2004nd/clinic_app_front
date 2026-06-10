package com.dermacare.clinic.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.ScheduleAdapter;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.ScheduleResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    public static final String EXTRA_DOCTOR_ID = "doctor_id";
    public static final String EXTRA_DOCTOR_NAME = "doctor_name";
    public static final String EXTRA_SPECIALTY = "specialty";
    public static final String EXTRA_AVATAR_URL = "avatar_url";
    public static final String EXTRA_INITIALS = "initials";
    public static final String EXTRA_FEE = "fee";
    public static final String EXTRA_APPOINTMENT_ID = "appointment_id";

    private ViewFlipper viewFlipper;
    private ScheduleAdapter scheduleAdapter;
    private ScheduleResponse selectedSchedule;

    private TextInputEditText etName, etPhone, etBirthYear, etSymptoms;
    private TextView tvSelectedSlot;
    private MaterialCardView stepCircle2;
    private View stepLine;
    private com.google.android.material.button.MaterialButton btnNext, btnPrev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Setup views first
        viewFlipper = findViewById(R.id.viewFlipper);
        stepCircle2 = findViewById(R.id.stepCircle2);
        stepLine = findViewById(R.id.stepLine);
        btnNext = findViewById(R.id.btnNextStep);
        btnPrev = findViewById(R.id.btnPrevStep);
        tvSelectedSlot = findViewById(R.id.tvSelectedSlot);
        etName = findViewById(R.id.etPatientName);
        etPhone = findViewById(R.id.etPhone);
        etBirthYear = findViewById(R.id.etBirthYear);
        etSymptoms = findViewById(R.id.etSymptoms);

        // Get intent extras
        long doctorId = getIntent().getLongExtra(EXTRA_DOCTOR_ID, -1);
        String doctorName = getIntent().getStringExtra(EXTRA_DOCTOR_NAME);
        String specialty = getIntent().getStringExtra(EXTRA_SPECIALTY);
        String avatarUrl = getIntent().getStringExtra(EXTRA_AVATAR_URL);
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        double fee = getIntent().getDoubleExtra(EXTRA_FEE, 150000);

        // Setup header
        TextView tvName = findViewById(R.id.tvBookingDoctorName);
        TextView tvSpec = findViewById(R.id.tvBookingSpecialty);
        TextView tvFee = findViewById(R.id.tvBookingFee);
        TextView tvInitials = findViewById(R.id.tvDoctorHeaderInitials);
        ShapeableImageView imgHeader = findViewById(R.id.imgDoctorHeader);
        tvName.setText(doctorName);
        tvSpec.setText(specialty);
        tvFee.setText(String.format(Locale.getDefault(), "Phí khám: %,.0f₫", fee));
        tvInitials.setText(initials);

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgHeader);
            tvInitials.setVisibility(View.GONE);
        }

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // If launched for reschedule
        long existingApptId = getIntent().getLongExtra(EXTRA_APPOINTMENT_ID, -1);
        // Remove immediate text change here, let goToStep1/2 handle it

        // Pre-fill profile from session
        com.dermacare.clinic.util.SessionManager session = new com.dermacare.clinic.util.SessionManager(this);
        if (etName.getText() != null && etName.getText().toString().isEmpty()) {
            etName.setText(session.getName());
        }
        if (etPhone.getText() != null && etPhone.getText().toString().isEmpty()) {
            etPhone.setText(session.getPhone());
        }
        if (etBirthYear.getText() != null && etBirthYear.getText().toString().isEmpty()) {
            String dob = session.getDob(); // Assume yyyy-MM-dd
            if (dob != null && dob.length() >= 4) {
                etBirthYear.setText(dob.substring(0, 4));
            }
        }

        // Setup schedule RecyclerView
        RecyclerView rv = findViewById(R.id.rvSchedules);
        rv.setLayoutManager(new LinearLayoutManager(this));
        scheduleAdapter = new ScheduleAdapter(new java.util.ArrayList<>(), schedule -> {
            selectedSchedule = schedule;
        });
        rv.setAdapter(scheduleAdapter);

        // Load schedules
        if (doctorId != -1) {
            ApiClient.getPublicService(this).getSchedules(doctorId)
                    .enqueue(new retrofit2.Callback<List<ScheduleResponse>>() {
                        @Override
                        public void onResponse(retrofit2.Call<List<ScheduleResponse>> call,
                                retrofit2.Response<List<ScheduleResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                // Filter available schedules
                                List<ScheduleResponse> available = new java.util.ArrayList<>();
                                for (ScheduleResponse s : response.body()) {
                                    if ("AVAILABLE".equals(s.status) && !s.isFull) {
                                        available.add(s);
                                    }
                                }
                                scheduleAdapter = new ScheduleAdapter(available, schedule -> {
                                    selectedSchedule = schedule;
                                });
                                rv.setAdapter(scheduleAdapter);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<List<ScheduleResponse>> call, Throwable t) {
                            Toast.makeText(BookingActivity.this, "Lỗi tải lịch", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Next / Prev buttons
        btnNext.setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() == 0) {
                if (selectedSchedule == null) {
                    Toast.makeText(this, "Vui lòng chọn một ca khám", Toast.LENGTH_SHORT).show();
                    return;
                }
                goToStep2();
            } else {
                submitBooking();
            }
        });

        btnPrev.setOnClickListener(v -> goToStep1());
    }

    private void goToStep2() {
        viewFlipper.setDisplayedChild(1);
        long existingApptId = getIntent().getLongExtra(EXTRA_APPOINTMENT_ID, -1);
        if (existingApptId > 0) {
            btnNext.setText("Xác nhận đổi lịch");
        } else {
            btnNext.setText("Xác nhận đặt lịch");
        }
        btnPrev.setVisibility(View.VISIBLE);

        // Update step indicator
        stepCircle2.setCardBackgroundColor(getColor(R.color.primary));
        TextView step2Text = stepCircle2.getChildAt(0) instanceof TextView
                ? (TextView) stepCircle2.getChildAt(0)
                : null;
        if (step2Text != null)
            step2Text.setTextColor(getColor(R.color.white));
        stepLine.setBackgroundColor(getColor(R.color.primary));

        // Show selected slot
        if (selectedSchedule != null) {
            tvSelectedSlot.setText("Ngày " + selectedSchedule.date
                    + " · " + selectedSchedule.startTime.substring(0, 5)
                    + " – " + selectedSchedule.endTime.substring(0, 5));
        }
    }

    private void goToStep1() {
        viewFlipper.setDisplayedChild(0);
        btnNext.setText("Tiếp theo →");
        btnPrev.setVisibility(View.GONE);
        stepCircle2.setCardBackgroundColor(getColor(R.color.surface_variant));
        stepLine.setBackgroundColor(getColor(R.color.border));
    }

    private void submitBooking() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String symptoms = etSymptoms.getText() != null ? etSymptoms.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }

        long existingApptId = getIntent().getLongExtra(EXTRA_APPOINTMENT_ID, -1);
        String title = existingApptId > 0 ? "Xác nhận đổi lịch" : "Xác nhận đặt lịch";
        String message = existingApptId > 0 ? "Bạn có chắc chắn muốn đổi sang lịch khám mới này không?" : "Bạn có chắc chắn muốn đặt lịch khám này không?";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    performSubmit(name, phone, symptoms);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performSubmit(String name, String phone, String symptoms) {
        btnNext.setEnabled(false);
        btnNext.setText("Đang gửi...");

        long existingApptId = getIntent().getLongExtra(EXTRA_APPOINTMENT_ID, -1);
        if (existingApptId > 0) {
            // Reschedule flow
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("scheduleId", selectedSchedule.scheduleId);
            com.dermacare.clinic.data.api.ApiClient.getAppointmentService(this)
                    .rescheduleAppointment(existingApptId, body)
                    .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                        @Override
                        public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call,
                                retrofit2.Response<java.util.Map<String, Object>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                new androidx.appcompat.app.AlertDialog.Builder(BookingActivity.this)
                                        .setTitle("✅ Đổi lịch thành công")
                                        .setMessage("Yêu cầu đổi lịch đã được gửi. Bác sĩ sẽ xác nhận ca mới.")
                                        .setPositiveButton("OK", (d, w) -> finish())
                                        .setCancelable(false)
                                        .show();
                            } else {
                                btnNext.setEnabled(true);
                                btnNext.setText("Xác nhận đổi lịch");
                                String msg = "Đổi lịch thất bại";
                                try {
                                    if (response.errorBody() != null) {
                                        String errBody = response.errorBody().string();
                                        com.google.gson.JsonObject err = new com.google.gson.Gson().fromJson(errBody,
                                                com.google.gson.JsonObject.class);
                                        if (err != null && err.has("message")) {
                                            msg = err.get("message").getAsString();
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                                Toast.makeText(BookingActivity.this, msg, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                            btnNext.setEnabled(true);
                            btnNext.setText("Xác nhận đổi lịch");
                            Toast.makeText(BookingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            return;
        }

        com.dermacare.clinic.data.api.model.AppointmentRequest req = new com.dermacare.clinic.data.api.model.AppointmentRequest(
                getIntent().getLongExtra(EXTRA_DOCTOR_ID, -1),
                selectedSchedule.scheduleId,
                symptoms,
                name,
                phone);

        com.dermacare.clinic.data.api.ApiClient.getAppointmentService(this)
                .bookAppointment(req)
                .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call,
                            retrofit2.Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Save profile to session if successful
                            com.dermacare.clinic.util.SessionManager session = new com.dermacare.clinic.util.SessionManager(BookingActivity.this);
                            String dob = (etBirthYear.getText() != null ? etBirthYear.getText().toString() : "1990") + "-01-01";
                            session.saveProfile(name, phone, session.getGender(), dob, session.getAddress(), session.getAvatar());

                            new androidx.appcompat.app.AlertDialog.Builder(BookingActivity.this)
                                    .setTitle("🎉 Đặt lịch thành công!")
                                    .setMessage("Lịch hẹn của bạn đang chờ bác sĩ xác nhận.\n\n" +
                                            "Mã lịch hẹn: " + response.body().get("appointmentCode") + "\n\n" +
                                            "Chúng tôi sẽ thông báo khi bác sĩ xác nhận.")
                                    .setPositiveButton("OK", (d, w) -> finish())
                                    .setCancelable(false)
                                    .show();
                        } else {
                            btnNext.setEnabled(true);
                            btnNext.setText("Xác nhận đặt lịch");
                            String msg = "Đặt lịch thất bại";
                            try {
                                if (response.errorBody() != null) {
                                    String errBody = response.errorBody().string();
                                    com.google.gson.JsonObject err = new com.google.gson.Gson().fromJson(errBody,
                                            com.google.gson.JsonObject.class);
                                    if (err != null && err.has("message")) {
                                        msg = err.get("message").getAsString();
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                            Toast.makeText(BookingActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                        btnNext.setEnabled(true);
                        btnNext.setText("Xác nhận đặt lịch");
                        Toast.makeText(BookingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}
