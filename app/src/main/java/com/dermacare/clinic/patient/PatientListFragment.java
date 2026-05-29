package com.dermacare.clinic.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;

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
        Bundle args = getArguments();
        String type = args != null ? args.getString(ARG_TYPE, TYPE_APPOINTMENTS) : TYPE_APPOINTMENTS;

        if (TYPE_RECORDS.equals(type)) {
            return inflater.inflate(R.layout.fragment_patient_records, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_patient_appointments, container, false);
        }
    }
}
