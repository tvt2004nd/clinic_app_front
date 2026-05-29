package com.dermacare.clinic.doctor;

import android.content.Intent;
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
        
        // Mở màn hình khám bệnh khi nhấn nút "Khám"
        rv.setAdapter(new AppointmentAdapter(MockData.doctorSchedule(), position -> {
            Intent intent = new Intent(requireContext(), ExamineActivity.class);
            startActivity(intent);
        }));
        
        // Thêm hiệu ứng click cho các thẻ thống kê
        view.findViewById(R.id.rvSchedule).setNestedScrollingEnabled(false);
    }
}
