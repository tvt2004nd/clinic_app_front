package com.dermacare.clinic.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.SimpleTextAdapter;
import com.dermacare.clinic.data.MockData;
import com.dermacare.clinic.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class DoctorPatientsFragment extends Fragment {
    private SimpleTextAdapter adapter;
    private RecyclerView rv;

    public static DoctorPatientsFragment newInstance() {
        return new DoctorPatientsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_simple, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Bệnh nhân");

        rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        refreshList();

        // Nút Thêm bệnh nhân (FAB)
        view.findViewById(R.id.btnAddPatient).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddPatientActivity.class));
        });

        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void refreshList() {
        List<String> lines = new ArrayList<>();
        for (Patient p : MockData.doctorPatients()) {
            lines.add(p.toString());
        }
        adapter = new SimpleTextAdapter(lines);
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách khi quay lại từ màn hình Thêm bệnh nhân
        refreshList();
    }
}
