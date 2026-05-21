package com.dermacare.clinic.patient;

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

public class PatientListFragment extends Fragment {
    public static final String TYPE_APPOINTMENTS = "appointments";
    public static final String TYPE_RECORDS = "records";
    private static final String ARG_TITLE = "title";
    private static final String ARG_TYPE = "type";

    public static PatientListFragment newInstance(String title, String type) {
        PatientListFragment f = new PatientListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_TYPE, type);
        f.setArguments(args);
        return f;
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
        Bundle args = getArguments();
        String title = args != null ? args.getString(ARG_TITLE, "") : "";
        String type = args != null ? args.getString(ARG_TYPE, TYPE_APPOINTMENTS) : TYPE_APPOINTMENTS;

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        List<String[]> items = TYPE_RECORDS.equals(type)
                ? MockData.medicalRecords()
                : MockData.appointments();

        List<String> lines = new ArrayList<>();
        for (String[] row : items) {
            lines.add(row[0] + "\n" + row[1] + "\n" + row[2]);
        }

        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new SimpleTextAdapter(lines));
    }
}
