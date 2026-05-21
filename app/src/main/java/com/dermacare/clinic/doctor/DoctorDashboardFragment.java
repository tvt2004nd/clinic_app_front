package com.dermacare.clinic.doctor;

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
import com.dermacare.clinic.adapter.AppointmentAdapter;
import com.dermacare.clinic.data.MockData;
import com.dermacare.clinic.util.SessionManager;

public class DoctorDashboardFragment extends Fragment {
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
        TextView tvName = view.findViewById(R.id.tvDoctorName);
        tvName.setText(session.getName());

        RecyclerView rv = view.findViewById(R.id.rvSchedule);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Cập nhật listener đúng với interface OnExamineClick
        rv.setAdapter(new AppointmentAdapter(MockData.doctorSchedule(), position -> {
            String patientName = MockData.doctorSchedule().get(position)[1];
            Toast.makeText(requireContext(), "Đang chuẩn bị hồ sơ cho: " + patientName, Toast.LENGTH_SHORT).show();
        }));
        
        // Thêm hiệu ứng click cho các thẻ thống kê
        view.findViewById(R.id.rvSchedule).setNestedScrollingEnabled(false);
    }
}
