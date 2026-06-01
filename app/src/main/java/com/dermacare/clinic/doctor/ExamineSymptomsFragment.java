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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExamineSymptomsFragment extends Fragment implements ExamineStep {

    private TextInputEditText edtSymptoms, edtLesionLocation, edtDuration, edtTriggers, edtPreviousTreatment;
    private MaterialAutoCompleteTextView actSeverity, actOnsetType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examine_symptoms, container, false);
        edtSymptoms = view.findViewById(R.id.edtSymptoms);
        edtLesionLocation = view.findViewById(R.id.edtLesionLocation);
        edtDuration = view.findViewById(R.id.edtDuration);
        edtTriggers = view.findViewById(R.id.edtTriggers);
        edtPreviousTreatment = view.findViewById(R.id.edtPreviousTreatment);
        actSeverity = view.findViewById(R.id.actSeverity);
        actOnsetType = view.findViewById(R.id.actOnsetType);

        setupDropdown(actSeverity, R.array.symptom_severity_levels);
        setupDropdown(actOnsetType, R.array.symptom_onset_types);
        return view;
    }

    private void setupDropdown(MaterialAutoCompleteTextView view, int arrayRes) {
        String[] items = getResources().getStringArray(arrayRes);
        view.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, items));
    }

    @Override
    public boolean isValid() {
        return edtSymptoms != null && edtSymptoms.getText() != null
                && edtSymptoms.getText().toString().trim().length() > 0;
    }

    public String getCombinedSymptoms() {
        List<String> parts = new ArrayList<>();
        addPart(parts, "Triệu chứng chính", textOf(edtSymptoms));
        addPart(parts, "Kiểu khởi phát", textOf(actOnsetType));
        addPart(parts, "Vị trí tổn thương", textOf(edtLesionLocation));
        addPart(parts, "Thời gian mắc", textOf(edtDuration));
        addPart(parts, "Mức độ", textOf(actSeverity));
        addPart(parts, "Yếu tố kích thích", textOf(edtTriggers));
        addPart(parts, "Điều trị trước đây", textOf(edtPreviousTreatment));
        return TextUtils.join("\n", parts);
    }

    private static void addPart(List<String> parts, String label, String value) {
        if (!TextUtils.isEmpty(value)) {
            parts.add(String.format(Locale.ROOT, "%s: %s", label, value));
        }
    }

    private static String textOf(android.widget.EditText field) {
        if (field == null || field.getText() == null) return "";
        return field.getText().toString().trim();
    }
}
