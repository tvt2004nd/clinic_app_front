package com.dermacare.clinic.patient;

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

import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.CalendarDayAdapter;
import com.dermacare.clinic.adapter.DoctorAdapter;
import com.dermacare.clinic.adapter.SpecialtyAdapter;
import com.dermacare.clinic.data.MockData;
import com.dermacare.clinic.util.SessionManager;

import java.util.Arrays;
import java.util.List;

public class PatientHomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        tvUserName.setText(session.getName());

        RecyclerView rvCalendar = view.findViewById(R.id.rvCalendar);
        rvCalendar.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
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
        rvSpecialties.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSpecialties.setAdapter(new SpecialtyAdapter(MockData.specialties(), MockData.specialtyIconResIds()));

        RecyclerView rvDoctors = view.findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
        DoctorAdapter doctorAdapter = new DoctorAdapter(new java.util.ArrayList<>(), doctor -> {
            fetchAndShowSchedules(doctor);
        });
        rvDoctors.setAdapter(doctorAdapter);

        // Fetch real doctors
        com.dermacare.clinic.data.api.ApiClient.getPublicService(requireContext()).getDoctors()
                .enqueue(new retrofit2.Callback<List<com.dermacare.clinic.data.api.model.DoctorResponse>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<com.dermacare.clinic.data.api.model.DoctorResponse>> call,
                                           retrofit2.Response<List<com.dermacare.clinic.data.api.model.DoctorResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<com.dermacare.clinic.model.Doctor> mappedDoctors = new java.util.ArrayList<>();
                            for (com.dermacare.clinic.data.api.model.DoctorResponse dr : response.body()) {
                                String safeName = dr.fullName != null && !dr.fullName.isEmpty() ? dr.fullName.substring(0, 1) : "D";
                                mappedDoctors.add(new com.dermacare.clinic.model.Doctor(
                                        dr.doctorId,
                                        dr.fullName,
                                        dr.specialty,
                                        dr.rating != null ? String.valueOf(dr.rating) : "5.0",
                                        true,
                                        "https://ui-avatars.com/api/?name=" + safeName + "&background=random&color=fff",
                                        dr.fee != null ? dr.fee : 150000
                                ));
                            }
                            doctorAdapter.setDoctors(mappedDoctors);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<com.dermacare.clinic.data.api.model.DoctorResponse>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi tải danh sách bác sĩ", Toast.LENGTH_SHORT).show();
                    }
                });

        view.findViewById(R.id.btnBook).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Vui lòng chọn một bác sĩ từ danh sách", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnNotification).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.toast_notifications_upcoming, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.searchBar).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tìm kiếm bác sĩ...", Toast.LENGTH_SHORT).show());
    }

    private void fetchAndShowSchedules(com.dermacare.clinic.model.Doctor doctor) {
        if (doctor.doctorId == null) {
            Toast.makeText(requireContext(), "Bác sĩ này chưa có lịch hẹn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build initials
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

        android.content.Intent intent = new android.content.Intent(requireContext(), BookingActivity.class);
        intent.putExtra(BookingActivity.EXTRA_DOCTOR_ID, doctor.doctorId);
        intent.putExtra(BookingActivity.EXTRA_DOCTOR_NAME, doctor.name);
        intent.putExtra(BookingActivity.EXTRA_SPECIALTY, doctor.specialty);
        intent.putExtra(BookingActivity.EXTRA_AVATAR_URL, doctor.avatarUrl);
        intent.putExtra(BookingActivity.EXTRA_INITIALS, initials.toUpperCase());
        intent.putExtra(BookingActivity.EXTRA_FEE, doctor.fee);
        startActivity(intent);
    }
}

