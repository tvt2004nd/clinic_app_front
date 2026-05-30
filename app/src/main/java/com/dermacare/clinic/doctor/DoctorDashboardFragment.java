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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoctorDashboardFragment extends Fragment {

    private PendingAppointmentAdapter adapter;
    private RecyclerView rv;
    private TextView tvTodayCount, tvCompletedCount, tvPendingCount, tvDoctorName, tvGreeting, tvCurrentDate, tvApptCount;
    private View layoutEmpty;
    private List<AppointmentResponse> allAppointments = new ArrayList<>();

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

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvGreeting.setText(getGreeting());

        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi", "VN"));
        String formatted = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        tvCurrentDate.setText(dayOfWeek + ", " + formatted);

        tvTodayCount = view.findViewById(R.id.tvTodayCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvApptCount = view.findViewById(R.id.tvApptCount);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

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
                        intent.putExtra("appointmentId", appt.appointmentId);
                        startActivity(intent);
                    }
                });
        rv.setAdapter(adapter);

        // Wire up quick action buttons
        view.findViewById(R.id.btnQuickNewExam).setOnClickListener(v -> {
            showNewPatients();
        });

        view.findViewById(R.id.btnQuickHistory).setOnClickListener(v -> {
            showHistory();
        });

        view.findViewById(R.id.btnQuickInvoice).setOnClickListener(v -> {
            showInvoices();
        });

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Chào buổi sáng,";
        if (hour < 18) return "Chào buổi chiều,";
        return "Chào buổi tối,";
    }

    private void showNewPatients() {
        // Navigate to DoctorPatientsFragment or show bottom sheet with pending appointments
        // For simplicity, navigate to the patients tab in DoctorMainActivity
        if (getActivity() instanceof DoctorMainActivity) {
            DoctorMainActivity activity = (DoctorMainActivity) getActivity();
            activity.navigateToTab(R.id.nav_patients);
            Toast.makeText(requireContext(), "Xem danh sách bệnh nhân chờ xác nhận",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showHistory() {
        // Replace current fragment with DoctorHistoryFragment
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_doctor, DoctorHistoryFragment.newInstance())
                    .addToBackStack("history")
                    .commit();
        }
    }

    private void showInvoices() {
        // Replace current fragment with DoctorInvoicesFragment
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_doctor, DoctorInvoicesFragment.newInstance())
                    .addToBackStack("invoices")
                    .commit();
        }
    }

    private void loadData() {
        ApiClient.getAppointmentService(requireContext())
                .getDoctorAppointments(null, null)
                .enqueue(new retrofit2.Callback<List<AppointmentResponse>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<AppointmentResponse>> call,
                                           retrofit2.Response<List<AppointmentResponse>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            allAppointments = response.body();

                            // Only show CONFIRMED appointments in "Lịch khám hôm nay"
                            List<AppointmentResponse> confirmedAppts = new ArrayList<>();
                            int todayCount = 0;
                            int pendingCount = 0;
                            int completedCount = 0;

                            String todayStr = LocalDate.now().toString();

                            for (AppointmentResponse a : allAppointments) {
                                if ("CONFIRMED".equals(a.status)) {
                                    confirmedAppts.add(a);
                                }
                                if (todayStr.equals(a.date)) {
                                    todayCount++;
                                }
                                if ("PENDING".equals(a.status)) {
                                    pendingCount++;
                                }
                                if ("COMPLETED".equals(a.status)) {
                                    completedCount++;
                                }
                            }

                            adapter.setData(confirmedAppts);

                            if (tvTodayCount != null) tvTodayCount.setText(String.valueOf(todayCount));
                            if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(pendingCount));
                            if (tvCompletedCount != null) tvCompletedCount.setText(String.valueOf(completedCount));
                            if (tvApptCount != null) tvApptCount.setText(confirmedAppts.size() + " lịch hẹn");

                            if (layoutEmpty != null) {
                                layoutEmpty.setVisibility(confirmedAppts.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<AppointmentResponse>> call, Throwable t) {
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
                                    "Đã xác nhận lịch hẹn của " + appt.patientName,
                                    Toast.LENGTH_SHORT).show();
                            loadData();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
