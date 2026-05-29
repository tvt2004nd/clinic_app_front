package com.dermacare.clinic.doctor;

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

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.AppointmentResponse;
import com.dermacare.clinic.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DoctorDashboardFragment extends Fragment {

    private PendingAppointmentAdapter adapter;
    private RecyclerView rv;
    private TextView tvPendingCount, tvDoctorName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());

        tvDoctorName = view.findViewById(R.id.tvDoctorName);
        tvDoctorName.setText(session.getName());

        rv = view.findViewById(R.id.rvSchedule);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);

        adapter = new PendingAppointmentAdapter(new ArrayList<>(),
                new PendingAppointmentAdapter.ActionListener() {
                    @Override
                    public void onConfirm(AppointmentResponse appt) {
                        confirmAppointment(appt);
                    }

                    @Override
                    public void onExamine(AppointmentResponse appt) {
                        Intent intent = new Intent(requireContext(), ExamineActivity.class);
                        startActivity(intent);
                    }
                });
        rv.setAdapter(adapter);

        loadPendingAppointments();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPendingAppointments();
    }

    private void loadPendingAppointments() {
        ApiClient.getAppointmentService(requireContext())
                .getDoctorAppointments(null, null)
                .enqueue(new retrofit2.Callback<List<AppointmentResponse>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<AppointmentResponse>> call,
                                           retrofit2.Response<List<AppointmentResponse>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<AppointmentResponse> allAppts = response.body();
                            
                            // Lọc ra danh sách cần hiển thị: PENDING hoặc CONFIRMED
                            List<AppointmentResponse> displayAppts = new ArrayList<>();
                            int todayCount = 0;
                            int completedCount = 0;
                            
                            String todayStr = java.time.LocalDate.now().toString();
                            
                            for (AppointmentResponse a : allAppts) {
                                if ("PENDING".equals(a.status) || "CONFIRMED".equals(a.status)) {
                                    displayAppts.add(a);
                                }
                                if (todayStr.equals(a.date)) {
                                    todayCount++;
                                }
                                if ("COMPLETED".equals(a.status)) {
                                    completedCount++;
                                }
                            }
                            
                            adapter.setData(displayAppts);
                            
                            // Cập nhật số liệu thống kê
                            View view = getView();
                            if (view != null) {
                                TextView tvToday = view.findViewById(R.id.tvTodayCount);
                                TextView tvCompleted = view.findViewById(R.id.tvCompletedCount);
                                if (tvToday != null) tvToday.setText(String.valueOf(todayCount));
                                if (tvCompleted != null) tvCompleted.setText(String.valueOf(completedCount));
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<AppointmentResponse>> call, Throwable t) {
                        // Silently fail - keep showing empty/old list
                    }
                });
    }

    private void confirmAppointment(AppointmentResponse appt) {
        ApiClient.getAppointmentService(requireContext())
                .confirmAppointment(appt.appointmentId)
                .enqueue(new retrofit2.Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(retrofit2.Call<Map<String, Object>> call,
                                           retrofit2.Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "✅ Đã xác nhận lịch hẹn của " + appt.patientName,
                                    Toast.LENGTH_SHORT).show();
                            loadPendingAppointments();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
