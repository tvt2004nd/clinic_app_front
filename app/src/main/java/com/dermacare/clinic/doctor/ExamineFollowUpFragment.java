package com.dermacare.clinic.doctor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class ExamineFollowUpFragment extends Fragment implements ExamineStep {
    private TextInputEditText edtFollowUpDate;
    private TextInputEditText edtFollowUpReason;
    private ExamineActivity examineActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examine_follow_up, container, false);

        edtFollowUpDate = view.findViewById(R.id.edtFollowUpDate);
        edtFollowUpReason = view.findViewById(R.id.edtFollowUpReason);

        edtFollowUpDate.setOnClickListener(v -> showDatePicker());

        MaterialButton btnFinish = view.findViewById(R.id.btnFinishAndBill);
        btnFinish.setOnClickListener(v -> {
            if (!isValid()) {
                edtFollowUpDate.setError("Vui lòng chọn ngày tái khám");
                return;
            }
            if (examineActivity != null) {
                examineActivity.completeExamination();
            }
        });

        return view;
    }

    public void setExamineActivity(ExamineActivity activity) {
        this.examineActivity = activity;
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    edtFollowUpDate.setText(date);
                    edtFollowUpDate.setError(null);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    @Override
    public boolean isValid() {
        return edtFollowUpDate != null && edtFollowUpDate.getText().toString().trim().length() > 0;
    }

    public String getFollowUpDate() {
        return edtFollowUpDate != null ? edtFollowUpDate.getText().toString().trim() : "";
    }

    public String getFollowUpReason() {
        return edtFollowUpReason != null ? edtFollowUpReason.getText().toString().trim() : "";
    }
}
