package com.dermacare.clinic.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.dermacare.clinic.R;
import com.google.android.material.button.MaterialButton;

public class ExamineFollowUpFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examine_follow_up, container, false);
        
        MaterialButton btnFinishAndBill = view.findViewById(R.id.btnFinishAndBill);
        btnFinishAndBill.setOnClickListener(v -> {
            // Move to Invoice Activity
            Intent intent = new Intent(getActivity(), InvoiceActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        
        return view;
    }
}
