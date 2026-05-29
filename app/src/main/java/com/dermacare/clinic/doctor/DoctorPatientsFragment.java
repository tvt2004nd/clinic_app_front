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
import com.dermacare.clinic.adapter.SimpleTextAdapter;
import com.dermacare.clinic.data.MockData;

import java.util.ArrayList;
import java.util.List;

public class DoctorPatientsFragment extends Fragment {
    private PatientAdapter adapter;
    private View layoutEmpty;
    private RecyclerView rv;
    private TextView tvPatientCount;

    public static DoctorPatientsFragment newInstance() {
        return new DoctorPatientsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_patients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPatientCount = view.findViewById(R.id.tvPatientCount);
        rv = view.findViewById(R.id.recyclerView);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PatientAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        loadPatients();
    }

    private void loadPatients() {
        com.dermacare.clinic.data.api.ApiClient.getAppointmentService(requireContext())
                .getDoctorAppointments(null, null)
                .enqueue(new retrofit2.Callback<List<com.dermacare.clinic.data.api.model.AppointmentResponse>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<com.dermacare.clinic.data.api.model.AppointmentResponse>> call,
                                           retrofit2.Response<List<com.dermacare.clinic.data.api.model.AppointmentResponse>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            java.util.Map<String, com.dermacare.clinic.data.api.model.AppointmentResponse> patients = new java.util.HashMap<>();
                            for (com.dermacare.clinic.data.api.model.AppointmentResponse appt : response.body()) {
                                if (appt.patientName != null && !patients.containsKey(appt.patientName)) {
                                    patients.put(appt.patientName, appt);
                                }
                            }
                            
                            List<com.dermacare.clinic.data.api.model.AppointmentResponse> uniquePatients = new ArrayList<>(patients.values());
                            
                            if (uniquePatients.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rv.setVisibility(View.GONE);
                                tvPatientCount.setText("0 bệnh nhân");
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rv.setVisibility(View.VISIBLE);
                                adapter.setData(uniquePatients);
                                tvPatientCount.setText(uniquePatients.size() + " bệnh nhân");
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<com.dermacare.clinic.data.api.model.AppointmentResponse>> call, Throwable t) {
                        if (!isAdded()) return;
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                        tvPatientCount.setText("Lỗi kết nối");
                    }
                });
    }
}
