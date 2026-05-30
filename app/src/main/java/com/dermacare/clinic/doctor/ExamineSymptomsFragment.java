package com.dermacare.clinic.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;

public class ExamineSymptomsFragment extends Fragment implements ExamineStep {
    private EditText edtSymptoms;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examine_symptoms, container, false);
        edtSymptoms = view.findViewById(R.id.edtSymptoms);
        return view;
    }

    @Override
    public boolean isValid() {
        return edtSymptoms != null && edtSymptoms.getText().toString().trim().length() > 0;
    }

    public String getSymptomsText() {
        return edtSymptoms != null ? edtSymptoms.getText().toString().trim() : "";
    }
}
