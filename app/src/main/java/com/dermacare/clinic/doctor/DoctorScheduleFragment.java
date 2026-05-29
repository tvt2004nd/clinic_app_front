package com.dermacare.clinic.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.AppointmentAdapter;
import com.dermacare.clinic.data.MockData;

public class DoctorScheduleFragment extends Fragment {
    private com.dermacare.clinic.doctor.PendingAppointmentAdapter adapter;
    private View layoutEmpty;
    private RecyclerView rv;

    public static DoctorScheduleFragment newInstance() {
        return new DoctorScheduleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvDate = view.findViewById(R.id.tvCurrentDate);
        String today = java.time.LocalDate.now().toString();
        tvDate.setText("Hôm nay, " + today);

        rv = view.findViewById(R.id.recyclerView);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new com.dermacare.clinic.doctor.PendingAppointmentAdapter(new java.util.ArrayList<>(),
                new com.dermacare.clinic.doctor.PendingAppointmentAdapter.ActionListener() {
                    @Override
                    public void onConfirm(com.dermacare.clinic.data.api.model.AppointmentResponse appt) {
                        // Normally we handle confirm in Dashboard, but let's allow it here too
                        confirmAppointment(appt);
                    }

                    @Override
                    public void onExamine(com.dermacare.clinic.data.api.model.AppointmentResponse appt) {
                        android.content.Intent intent = new android.content.Intent(requireContext(), ExamineActivity.class);
                        startActivity(intent);
                    }
                });
        rv.setAdapter(adapter);

        loadSchedule();
    }

    private void loadSchedule() {
        com.dermacare.clinic.data.api.ApiClient.getAppointmentService(requireContext())
                .getDoctorAppointments(null, null)
                .enqueue(new retrofit2.Callback<java.util.List<com.dermacare.clinic.data.api.model.AppointmentResponse>>() {
                    @Override
                    public void onResponse(retrofit2.Call<java.util.List<com.dermacare.clinic.data.api.model.AppointmentResponse>> call,
                                           retrofit2.Response<java.util.List<com.dermacare.clinic.data.api.model.AppointmentResponse>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            java.util.List<com.dermacare.clinic.data.api.model.AppointmentResponse> list = response.body();
                            if (list.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rv.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rv.setVisibility(View.VISIBLE);
                                adapter.setData(list);
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<java.util.List<com.dermacare.clinic.data.api.model.AppointmentResponse>> call, Throwable t) {
                        if (!isAdded()) return;
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                });
    }

    private void confirmAppointment(com.dermacare.clinic.data.api.model.AppointmentResponse appt) {
        com.dermacare.clinic.data.api.ApiClient.getAppointmentService(requireContext())
                .confirmAppointment(appt.appointmentId)
                .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call,
                                           retrofit2.Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            loadSchedule();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                    }
                });
    }
}
