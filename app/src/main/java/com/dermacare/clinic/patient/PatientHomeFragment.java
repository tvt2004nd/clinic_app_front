package com.dermacare.clinic.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.CalendarDayAdapter;
import com.dermacare.clinic.adapter.DoctorAdapter;
import com.dermacare.clinic.adapter.SpecialtyAdapter;
import com.dermacare.clinic.data.MockData;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.AppointmentResponse;
import com.dermacare.clinic.data.api.model.DoctorResponse;
import com.dermacare.clinic.model.Doctor;
import com.dermacare.clinic.util.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientHomeFragment extends Fragment {

    private SessionManager session;
    private TextView tvUserName; // Đã thêm khai báo toàn cục
    private ShapeableImageView ivUserAvatar; // Đã thêm khai báo toàn cục

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());

        // Ánh xạ View cho User
        tvUserName = view.findViewById(R.id.tvUserName);
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);

        // ==========================================
        // 1. SETUP LỊCH & CHUYÊN KHOA (Mock Data)
        // ==========================================
        RecyclerView rvCalendar = view.findViewById(R.id.rvCalendar);
        rvCalendar.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        List<CalendarDayAdapter.Day> weekDays = Arrays.asList(
                new CalendarDayAdapter.Day("T2", "19", true),
                new CalendarDayAdapter.Day("T3", "20", false),
                new CalendarDayAdapter.Day("T4", "21", false),
                new CalendarDayAdapter.Day("T5", "22", false),
                new CalendarDayAdapter.Day("T6", "23", false),
                new CalendarDayAdapter.Day("T7", "24", false),
                new CalendarDayAdapter.Day("CN", "25", false)
        );
        rvCalendar.setAdapter(new CalendarDayAdapter(weekDays));

        RecyclerView rvSpecialties = view.findViewById(R.id.rvSpecialties);
        rvSpecialties.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSpecialties.setAdapter(new SpecialtyAdapter(MockData.specialties(), MockData.specialtyIconResIds()));

        // ==========================================
        // 2. LOAD DANH SÁCH BÁC SĨ (API) - LẤY TOP 4
        // ==========================================
        RecyclerView rvDoctors = view.findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
        DoctorAdapter doctorAdapter = new DoctorAdapter(new ArrayList<>(), doctor -> {
            fetchAndShowSchedules(doctor);
        });
        rvDoctors.setAdapter(doctorAdapter);

        ApiClient.getPublicService(requireContext()).getDoctors()
                .enqueue(new Callback<List<DoctorResponse>>() {
                    @Override
                    public void onResponse(Call<List<DoctorResponse>> call, Response<List<DoctorResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            List<DoctorResponse> allDoctors = response.body();

                            // SẮP XẾP BÁC SĨ THEO RATING GIẢM DẦN
                            java.util.Collections.sort(allDoctors, (d1, d2) -> {
                                double r1 = d1.rating != null ? d1.rating : 0.0;
                                double r2 = d2.rating != null ? d2.rating : 0.0;
                                return Double.compare(r2, r1);
                            });

                            List<Doctor> mappedDoctors = new ArrayList<>();
                            int limit = Math.min(allDoctors.size(), 4);

                            for (int i = 0; i < limit; i++) {
                                DoctorResponse dr = allDoctors.get(i);

                                String finalAvatarUrl = "";
                                if (dr.avatarUrl != null && !dr.avatarUrl.trim().isEmpty() && !dr.avatarUrl.equals("null")) {
                                    if (dr.avatarUrl.startsWith("http")) {
                                        finalAvatarUrl = dr.avatarUrl;
                                    } else if (dr.avatarUrl.startsWith("/")) {
                                        finalAvatarUrl = ApiClient.BASE_URL + dr.avatarUrl.substring(1);
                                    } else {
                                        finalAvatarUrl = ApiClient.BASE_URL + dr.avatarUrl;
                                    }
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
                            doctorAdapter.setDoctors(mappedDoctors);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DoctorResponse>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi tải danh sách bác sĩ", Toast.LENGTH_SHORT).show();
                    }
                });

        // ==========================================
        // 3. CÁC NÚT BẤM KHÁC
        // ==========================================
        TextView tvSeeAllDoctors = view.findViewById(R.id.tvSeeAllDoctors);
        if (tvSeeAllDoctors != null) {
            tvSeeAllDoctors.setOnClickListener(v -> {
                // CHUYỂN SANG MÀN HÌNH DANH SÁCH BÁC SĨ (THAY VÌ HIỆN TOAST NHƯ TRƯỚC)
                Intent intent = new Intent(requireContext(), AllDoctorsActivity.class);
                startActivity(intent);
            });
        }

        view.findViewById(R.id.btnNotification).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.toast_notifications_upcoming, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.searchBar).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tìm kiếm bác sĩ...", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnQuickAppointments).setOnClickListener(v ->
                navigateToTab(R.id.nav_appointments));

        view.findViewById(R.id.btnQuickInvoices).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_patient, new PatientInvoicesFragment())
                        .addToBackStack("invoices")
                        .commit();
            }
        });

        view.findViewById(R.id.btnQuickAI).setOnClickListener(v -> navigateToTab(R.id.nav_ai));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPatientInfo();
        if (getView() != null) {
            loadUpcomingAppointment(getView());
        }
    }

    private void loadPatientInfo() {
        if (tvUserName == null || ivUserAvatar == null) return;

        String userName = session.getName() != null ? session.getName() : "Khách";
        tvUserName.setText(userName);

        String avatarUrl = session.getAvatar();

        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            String fullUrl = avatarUrl;
            if (avatarUrl.startsWith("/")) {
                fullUrl = ApiClient.BASE_URL + avatarUrl.substring(1);
            } else if (!avatarUrl.startsWith("http")) {
                fullUrl = ApiClient.BASE_URL + avatarUrl;
            }

            Glide.with(this)
                    .load(fullUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .into(ivUserAvatar);
        } else {
            String userInitial = userName.substring(0, 1).toUpperCase();
            String defaultAvatarUrl = "https://ui-avatars.com/api/?name=" + userInitial + "&background=0D8B8B&color=fff";

            Glide.with(this)
                    .load(defaultAvatarUrl)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .into(ivUserAvatar);
        }
    }

    private void loadUpcomingAppointment(View view) {
        View upcomingHeader = view.findViewById(R.id.upcomingHeader);
        View upcomingCard = view.findViewById(R.id.upcomingCard);
        ShapeableImageView ivDoctorAvatar = view.findViewById(R.id.ivDoctorAvatar);
        TextView tvUpcomingDoctorName = view.findViewById(R.id.tvUpcomingDoctorName);
        TextView tvUpcomingSpecialty = view.findViewById(R.id.tvUpcomingSpecialty);

        ApiClient.getAppointmentService(requireContext()).getMyAppointments()
                .enqueue(new Callback<List<AppointmentResponse>>() {
                    @Override
                    public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                            AppointmentResponse closestUpcoming = null;

                            for (AppointmentResponse appt : response.body()) {
                                if (("PENDING".equals(appt.status) || "CONFIRMED".equals(appt.status))
                                        && appt.date != null && appt.date.compareTo(today) >= 0) {
                                    closestUpcoming = appt;
                                    break;
                                }
                            }

                            if (closestUpcoming != null && closestUpcoming.doctorName != null) {
                                upcomingHeader.setVisibility(View.VISIBLE);
                                upcomingCard.setVisibility(View.VISIBLE);

                                tvUpcomingDoctorName.setText("BS. " + closestUpcoming.doctorName);
                                tvUpcomingSpecialty.setText("Da liễu tổng quát");

                                String safeInit = closestUpcoming.doctorName.substring(0, 1).toUpperCase();
                                String docAvatarUrl = "https://ui-avatars.com/api/?name=" + safeInit + "&background=random&color=fff";

                                Glide.with(requireContext())
                                        .load(docAvatarUrl)
                                        .placeholder(R.drawable.ic_nav_profile)
                                        .error(R.drawable.ic_nav_profile)
                                        .into(ivDoctorAvatar);

                                view.findViewById(R.id.btnBook).setOnClickListener(v -> navigateToTab(R.id.nav_appointments));
                            } else {
                                upcomingHeader.setVisibility(View.GONE);
                                upcomingCard.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                        upcomingHeader.setVisibility(View.GONE);
                        upcomingCard.setVisibility(View.GONE);
                    }
                });
    }

    private void navigateToTab(int tabId) {
        if (getActivity() instanceof PatientMainActivity) {
            ((PatientMainActivity) getActivity()).selectTab(tabId);
        }
    }

    private void fetchAndShowSchedules(Doctor doctor) {
        if (doctor.doctorId == null) {
            Toast.makeText(requireContext(), "Bác sĩ này chưa có lịch hẹn", Toast.LENGTH_SHORT).show();
            return;
        }

        String initials = "";
        if (doctor.name != null && !doctor.name.isEmpty()) {
            String[] parts = doctor.name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = String.valueOf(parts[parts.length - 2].charAt(0))
                        + parts[parts.length - 1].charAt(0);
            } else {
                initials = doctor.name.substring(0, Math.min(2, doctor.name.length()));
            }
        }

        Intent intent = new Intent(requireContext(), DoctorProfileActivity.class);
        intent.putExtra(BookingActivity.EXTRA_DOCTOR_ID, doctor.doctorId);
        intent.putExtra(BookingActivity.EXTRA_DOCTOR_NAME, doctor.name);
        intent.putExtra(BookingActivity.EXTRA_SPECIALTY, doctor.specialty);
        intent.putExtra(BookingActivity.EXTRA_AVATAR_URL, doctor.avatarUrl);
        intent.putExtra(BookingActivity.EXTRA_INITIALS, initials.toUpperCase());
        intent.putExtra(BookingActivity.EXTRA_FEE, doctor.fee);
        startActivity(intent);
    }
}