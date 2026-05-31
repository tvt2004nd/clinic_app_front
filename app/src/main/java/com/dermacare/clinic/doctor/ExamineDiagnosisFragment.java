package com.dermacare.clinic.doctor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;


public class ExamineDiagnosisFragment extends Fragment implements ExamineStep {

    private TextInputEditText edtDiseaseName, edtTreatmentPlan, edtDiagnosisNote;
    private MaterialAutoCompleteTextView actDiagnosisType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examine_diagnosis, container, false);
        edtDiseaseName = view.findViewById(R.id.edtDiseaseName);
        edtTreatmentPlan = view.findViewById(R.id.edtTreatmentPlan);
        edtDiagnosisNote = view.findViewById(R.id.edtDiagnosisNote);
        actDiagnosisType = view.findViewById(R.id.actDiagnosisType);

        actDiagnosisType.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.diagnosis_types)));
        return view;
    }

    @Override
    public boolean isValid() {
        return edtDiseaseName != null && edtDiseaseName.getText() != null
                && edtDiseaseName.getText().toString().trim().length() > 0;
    }

    public String getFinalDiagnosisText() {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, textOf(edtDiseaseName));
        String type = textOf(actDiagnosisType);
        if (!TextUtils.isEmpty(type)) {
            sb.append("\nLoại: ").append(type);
        }
        String note = textOf(edtDiagnosisNote);
        if (!TextUtils.isEmpty(note)) {
            sb.append("\nGhi chú: ").append(note);
        }
        return sb.toString().trim();
    }

    public String getTreatmentPlan() {
        return textOf(edtTreatmentPlan);
    }

    private static void appendLine(StringBuilder sb, String line) {
        if (!TextUtils.isEmpty(line)) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(line);
        }
    }

    private static String textOf(android.widget.EditText field) {
        if (field == null || field.getText() == null) return "";
        return field.getText().toString().trim();
    }
}
