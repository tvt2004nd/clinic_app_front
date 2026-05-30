package com.dermacare.clinic.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.google.gson.JsonObject;



import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamineAdmissionFragment extends Fragment implements ExamineStep {

    private TextView tvPatientName, tvPatientInfo, tvReason, tvPatientPhone;
    private TextView tvPatientAddress, tvPatientInsurance, tvPatientBloodType;
    private TextView tvAvatarInitials;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examine_admission, container, false);

        tvPatientName = view.findViewById(R.id.tvPatientName);
        tvPatientInfo = view.findViewById(R.id.tvPatientInfo);
        tvReason = view.findViewById(R.id.tvReason);
        tvPatientPhone = view.findViewById(R.id.tvPatientPhone);
        tvPatientAddress = view.findViewById(R.id.tvPatientAddress);
        tvPatientInsurance = view.findViewById(R.id.tvPatientInsurance);
        tvPatientBloodType = view.findViewById(R.id.tvPatientBloodType);
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);

        long recordId = getArguments() != null ? getArguments().getLong("recordId", -1L) : -1L;
        if (recordId != -1L) {
            loadPatientData(recordId);
        }

        return view;
    }

    private void loadPatientData(long recordId) {
        ApiClient.getExaminationService(requireContext())
                .getMedicalRecord(recordId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject data = response.body();
                            displayPatientData(data);
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        // Keep default empty text fields on error
                    }
                });
    }

    private void displayPatientData(JsonObject data) {
        // Patient info
        if (data.has("patient") && !data.get("patient").isJsonNull()) {
            JsonObject patient = data.getAsJsonObject("patient");

            String fullName = getJsonString(patient, "fullName");
            String phone = getJsonString(patient, "phone");
            String dob = getJsonString(patient, "dateOfBirth");
            String gender = getJsonString(patient, "gender");
            String address = getJsonString(patient, "address");
            String bloodType = getJsonString(patient, "bloodType");
            String insurance = getJsonString(patient, "insuranceNumber");

            tvPatientName.setText(fullName);

            // Set avatar initials from patient name
            if (fullName != null && !fullName.isEmpty()) {
                String[] nameParts = fullName.trim().split("\\s+");
                if (nameParts.length >= 2) {
                    tvAvatarInitials.setText(
                        String.valueOf(nameParts[0].charAt(0)) +
                        String.valueOf(nameParts[nameParts.length - 1].charAt(0))
                    );
                } else {
                    tvAvatarInitials.setText(String.valueOf(fullName.charAt(0)));
                }
            }

            // Format DOB (yyyy-MM-dd -> dd/MM/yyyy) and gender
            String info = "";
            if (dob != null && !dob.isEmpty()) {
                String[] parts = dob.split("-");
                if (parts.length == 3) {
                    info = parts[2] + "/" + parts[1] + "/" + parts[0];
                } else {
                    info = dob;
                }
            }
            if (gender != null && !gender.isEmpty()) {
                String genderVi = "MALE".equals(gender) ? "Nam" : "FEMALE".equals(gender) ? "Nữ" : gender;
                info = info.isEmpty() ? genderVi : info + " · " + genderVi;
            }
            tvPatientInfo.setText(info);

            tvPatientPhone.setText(phone != null ? phone : "");
            tvPatientAddress.setText(address != null ? address : "");
            tvPatientInsurance.setText(insurance != null ? insurance : "");
            tvPatientBloodType.setText(bloodType != null ? bloodType : "");
        }

        // Appointment reason
        if (data.has("appointment") && !data.get("appointment").isJsonNull()) {
            JsonObject appointment = data.getAsJsonObject("appointment");
            String reason = getJsonString(appointment, "reason");
            tvReason.setText(reason != null ? reason : "");
        }
    }

    private String getJsonString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "";
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
