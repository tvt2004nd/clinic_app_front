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
        rvDoctors.setAdapter(new DoctorAdapter(MockData.doctors()));

        view.findViewById(R.id.btnBook).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.toast_book_upcoming, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnNotification).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.toast_notifications_upcoming, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.searchBar).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tìm kiếm bác sĩ...", Toast.LENGTH_SHORT).show());
    }
}
