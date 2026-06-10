package com.dermacare.clinic.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamineAdmissionFragment extends Fragment implements ExamineStep {

    private TextView tvPatientName, tvPatientInfo, tvReason, tvPatientPhone;
    private TextView tvPatientAddress, tvPatientInsurance, tvPatientBloodType;
    private TextView tvAvatarInitials, tvNoHistory;
    private LinearLayout historyList;
    private ProgressBar progressHistory;
    private Long patientId = null;
    private Long currentRecordId = -1L;

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
        historyList = view.findViewById(R.id.historyList);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);
        progressHistory = view.findViewById(R.id.progressHistory);

        currentRecordId = getArguments() != null ? getArguments().getLong("recordId", -1L) : -1L;
        if (currentRecordId != -1L) {
            loadPatientData(currentRecordId);
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
                    }
                });
    }

    private void displayPatientData(JsonObject data) {
        if (data.has("patient") && !data.get("patient").isJsonNull()) {
            JsonObject patient = data.getAsJsonObject("patient");

            if (patient.has("patientId") && !patient.get("patientId").isJsonNull()) {
                patientId = patient.get("patientId").getAsLong();
            }

            String fullName = getJsonString(patient, "fullName");
            String phone = getJsonString(patient, "phone");
            String dob = getJsonString(patient, "dateOfBirth");
            String gender = getJsonString(patient, "gender");
            String address = getJsonString(patient, "address");
            String bloodType = getJsonString(patient, "bloodType");
            String insurance = getJsonString(patient, "insuranceNumber");

            tvPatientName.setText(fullName);

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

        if (data.has("appointment") && !data.get("appointment").isJsonNull()) {
            JsonObject appointment = data.getAsJsonObject("appointment");
            String reason = getJsonString(appointment, "reason");
            tvReason.setText(reason != null ? reason : "");
        }

        if (patientId != null) {
            loadPastHistory();
        } else {
            tvNoHistory.setVisibility(View.VISIBLE);
        }
    }

    private void loadPastHistory() {
        progressHistory.setVisibility(View.VISIBLE);
        tvNoHistory.setVisibility(View.GONE);

        ApiClient.getExaminationService(requireContext()).getDoctorHistory()
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        progressHistory.setVisibility(View.GONE);
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<DoctorHistoryFragment.HistoryItem>>() {}.getType();
                            List<DoctorHistoryFragment.HistoryItem> allRecords = gson.fromJson(
                                    gson.toJsonTree(response.body()), listType);

                            boolean found = false;
                            for (DoctorHistoryFragment.HistoryItem item : allRecords) {
                                if (item.patientId != null && item.patientId.equals(patientId)
                                        && item.recordId != null && !item.recordId.equals(currentRecordId)) {
                                    addHistoryCard(item);
                                    found = true;
                                }
                            }
                            if (!found) {
                                tvNoHistory.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvNoHistory.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        progressHistory.setVisibility(View.GONE);
                        if (!isAdded()) return;
                        tvNoHistory.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void addHistoryCard(DoctorHistoryFragment.HistoryItem item) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_doctor_history, historyList, false);

        TextView tvName = card.findViewById(R.id.tvPatientName);
        TextView tvDiagnosis = card.findViewById(R.id.tvDiagnosis);
        TextView tvDate = card.findViewById(R.id.tvExaminedDate);
        TextView tvStatus = card.findViewById(R.id.tvInvoiceStatus);
        TextView tvAvatar = card.findViewById(R.id.tvAvatarText);
        View cardAvatar = card.findViewById(R.id.cardAvatar);

        tvName.setVisibility(View.GONE);
        if (cardAvatar != null) {
            cardAvatar.setVisibility(View.GONE);
        }

        String diag = item.diagnosis != null && !item.diagnosis.isEmpty()
                ? item.diagnosis : (item.diseaseName != null ? item.diseaseName : "Khám bệnh");
        tvDiagnosis.setText(diag);

        String date = item.examinedAt != null && item.examinedAt.length() >= 10
                ? item.examinedAt.substring(0, 10) : "--";
        tvDate.setText("Đã khám: " + date);

        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> {
            if (getActivity() != null && item.recordId != null) {
                android.content.Intent intent = new android.content.Intent(getActivity(),
                        com.dermacare.clinic.patient.RecordDetailActivity.class);
                intent.putExtra("recordId", item.recordId.longValue());
                getActivity().startActivity(intent);
            }
        });

        historyList.addView(card);
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
