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
        tvTitle.setText(R.string.tab_patients);

        List<String> lines = new ArrayList<>();
        for (String[] row : MockData.doctorPatients()) {
            lines.add(row[0] + "\n" + row[1] + "\n" + "Khám gần nhất: " + row[2]);
        }

        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new SimpleTextAdapter(lines));
    }
}
