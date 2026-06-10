package com.dermacare.clinic.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.dermacare.clinic.R;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.Random;

public class DoctorProfileActivity extends AppCompatActivity {

    private Long doctorId;
    private String doctorName, specialty, avatarUrl, initials;
    private double fee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        doctorId = intent.getLongExtra(BookingActivity.EXTRA_DOCTOR_ID, -1);
        doctorName = intent.getStringExtra(BookingActivity.EXTRA_DOCTOR_NAME);
        specialty = intent.getStringExtra(BookingActivity.EXTRA_SPECIALTY);
        avatarUrl = intent.getStringExtra(BookingActivity.EXTRA_AVATAR_URL);
        initials = intent.getStringExtra(BookingActivity.EXTRA_INITIALS);
        fee = intent.getDoubleExtra(BookingActivity.EXTRA_FEE, 150000);

        // Setup Toolbar back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ View
        ImageView ivDocAvatar = findViewById(R.id.ivDocAvatar);
        TextView tvDocName = findViewById(R.id.tvDocName);
        TextView tvDocSpecialty = findViewById(R.id.tvDocSpecialty);
        TextView tvPatients = findViewById(R.id.tvPatients);
        TextView tvExperience = findViewById(R.id.tvExperience);
        TextView tvRating = findViewById(R.id.tvRating);
        TextView tvBiography = findViewById(R.id.tvBiography);
        MaterialButton btnBookAppointment = findViewById(R.id.btnBookAppointment);

        // Đổ dữ liệu thật
        tvDocName.setText((doctorName != null && !doctorName.contains("BS.")) ? "BS. " + doctorName : doctorName);
        tvDocSpecialty.setText(specialty != null ? specialty : "Da liễu tổng quát");

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_nav_profile).into(ivDocAvatar);
        } else {
            String defaultAvatar = "https://ui-avatars.com/api/?name=" + initials + "&background=random&color=fff";
            Glide.with(this).load(defaultAvatar).into(ivDocAvatar);
        }

        // ==========================================
        // HARDCODE THÔNG TIN CHO SINH ĐỘNG
        // ==========================================
        Random random = new Random();

        // Random số lượng bệnh nhân từ 800 đến 3500+
        int randomPatients = 800 + random.nextInt(2700);
        tvPatients.setText(randomPatients + "+");

        // Random kinh nghiệm từ 5 đến 15 năm
        int randomExp = 5 + random.nextInt(11);
        tvExperience.setText(randomExp + " năm");

        // Random rating từ 4.5 đến 5.0
        double randomRate = 4.5 + (random.nextDouble() * 0.5);
        tvRating.setText(new DecimalFormat("#.1").format(randomRate));

        // Đoạn văn giới thiệu sinh động (Nếu API ko có tiểu sử)
        String bio = "Bác sĩ " + doctorName + " là một chuyên gia hàng đầu trong lĩnh vực " +
                (specialty != null ? specialty : "Da liễu") + ". Với hơn " + randomExp +
                " năm kinh nghiệm cống hiến, bác sĩ đã điều trị thành công cho hàng nghìn bệnh nhân mắc các vấn đề về da.\n\n" +
                "Bác sĩ từng tu nghiệp tại các bệnh viện da liễu uy tín và luôn cập nhật các phương pháp điều trị tiên tiến nhất (như Laser thế hệ mới, tế bào gốc). Phương châm làm việc: \"Làn da khỏe mạnh của bạn là sứ mệnh của tôi\".";
        tvBiography.setText(bio);

        // Xử lý nút Đặt lịch (Chuyển tiếp sang BookingActivity)
        btnBookAppointment.setOnClickListener(v -> {
            Intent bookingIntent = new Intent(this, BookingActivity.class);
            bookingIntent.putExtra(BookingActivity.EXTRA_DOCTOR_ID, doctorId);
            bookingIntent.putExtra(BookingActivity.EXTRA_DOCTOR_NAME, doctorName);
            bookingIntent.putExtra(BookingActivity.EXTRA_SPECIALTY, specialty);
            bookingIntent.putExtra(BookingActivity.EXTRA_AVATAR_URL, avatarUrl);
            bookingIntent.putExtra(BookingActivity.EXTRA_INITIALS, initials);
            bookingIntent.putExtra(BookingActivity.EXTRA_FEE, fee);
            startActivity(bookingIntent);
        });
    }
}