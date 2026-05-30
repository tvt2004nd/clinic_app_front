package com.dermacare.clinic.doctor;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExaminePrescriptionFragment extends Fragment implements ExamineStep {
    private TextInputEditText edtConsultationFee;
    private ListView lvPrescription;
    private List<PrescriptionItem> prescriptionItems = new ArrayList<>();
    private PrescriptionAdapter prescriptionAdapter;

    static class PrescriptionItem {
        int medicationId;
        String medName;
        String unit;
        double price;
        String dosageInstruction;
        int quantity;
        int durationDays;

        PrescriptionItem(int medicationId, String medName, String unit, double price,
                         String dosageInstruction, int quantity, int durationDays) {
            this.medicationId = medicationId;
            this.medName = medName;
            this.unit = unit;
            this.price = price;
            this.dosageInstruction = dosageInstruction;
            this.quantity = quantity;
            this.durationDays = durationDays;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examine_prescription, container, false);
        edtConsultationFee = view.findViewById(R.id.edtConsultationFee);
        MaterialButton btnAddMedicine = view.findViewById(R.id.btnAddMedicine);
        lvPrescription = view.findViewById(R.id.lvPrescription);

        prescriptionAdapter = new PrescriptionAdapter();
        lvPrescription.setAdapter(prescriptionAdapter);

        btnAddMedicine.setOnClickListener(v -> showAddMedicineDialog());

        return view;
    }

    private void showAddMedicineDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_medicine, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Thêm thuốc")
                .setView(dialogView)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                TextInputEditText etDosage = dialogView.findViewById(R.id.etDosageInstruction);
                TextInputEditText etQuantity = dialogView.findViewById(R.id.etQuantity);
                TextInputEditText etDuration = dialogView.findViewById(R.id.etDurationDays);
                TextView tvSelected = dialogView.findViewById(R.id.tvSelectedMed);

                Object tag = tvSelected.getTag();
                if (tag == null || !(tag instanceof JsonObject)) {
                    Toast.makeText(getContext(), "Vui lòng chọn thuốc", Toast.LENGTH_SHORT).show();
                    return;
                }

                String dosage = etDosage.getText().toString().trim();
                String qtyStr = etQuantity.getText().toString().trim();
                String durStr = etDuration.getText().toString().trim();

                if (dosage.isEmpty()) {
                    etDosage.setError("Vui lòng nhập liều dùng");
                    return;
                }
                if (qtyStr.isEmpty()) {
                    etQuantity.setError("Vui lòng nhập số lượng");
                    return;
                }
                if (durStr.isEmpty()) {
                    etDuration.setError("Vui lòng nhập số ngày");
                    return;
                }

                JsonObject med = (JsonObject) tag;
                int medicationId = med.get("medicationId").getAsInt();
                String medName = med.get("medName").getAsString();
                String unit = med.has("unit") && !med.get("unit").isJsonNull() ? med.get("unit").getAsString() : "";
                double price = med.has("price") && !med.get("price").isJsonNull() ? med.get("price").getAsDouble() : 0;
                int quantity = Integer.parseInt(qtyStr);
                int durationDays = Integer.parseInt(durStr);

                prescriptionItems.add(new PrescriptionItem(medicationId, medName, unit, price,
                        dosage, quantity, durationDays));
                prescriptionAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });
        });

        setupMedicationSearch(dialogView, dialog);
        dialog.show();
    }

    private void setupMedicationSearch(View dialogView, AlertDialog dialog) {
        TextInputEditText etSearch = dialogView.findViewById(R.id.etSearchMedication);
        ListView lvResults = dialogView.findViewById(R.id.lvSearchResults);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 2) {
                    searchMedications(keyword, lvResults, dialogView, dialog);
                } else {
                    lvResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchMedications(String keyword, ListView lvResults, View dialogView, AlertDialog dialog) {
        ApiClient.getExaminationService(requireContext())
                .searchMedications(keyword)
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<JsonObject> results = response.body();
                            lvResults.setAdapter(new BaseAdapter() {
                                @Override
                                public int getCount() { return results.size(); }

                                @Override
                                public Object getItem(int position) { return results.get(position); }

                                @Override
                                public long getItemId(int position) { return position; }

                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    if (convertView == null) {
                                        convertView = LayoutInflater.from(getContext())
                                                .inflate(R.layout.item_medication_search, parent, false);
                                    }
                                    JsonObject med = results.get(position);
                                    TextView tvName = convertView.findViewById(R.id.tvMedName);
                                    TextView tvDetail = convertView.findViewById(R.id.tvMedDetail);
                                    TextView tvPrice = convertView.findViewById(R.id.tvMedPrice);

                                    tvName.setText(med.has("medName") ? med.get("medName").getAsString() : "");
                                    String detail = "";
                                    if (med.has("activeIngredient") && !med.get("activeIngredient").isJsonNull())
                                        detail += med.get("activeIngredient").getAsString();
                                    if (med.has("dosageForm") && !med.get("dosageForm").isJsonNull())
                                        detail += " - " + med.get("dosageForm").getAsString();
                                    tvDetail.setText(detail);
                                    if (med.has("price") && !med.get("price").isJsonNull())
                                        tvPrice.setText(String.format("%,.0f₫", med.get("price").getAsDouble()));
                                    else
                                        tvPrice.setText("");

                                    convertView.setOnClickListener(v -> {
                                        selectMedication(med, dialogView, lvResults);
                                    });

                                    return convertView;
                                }
                            });
                            lvResults.setVisibility(View.VISIBLE);
                        } else {
                            lvResults.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        lvResults.setVisibility(View.GONE);
                    }
                });
    }

    private void selectMedication(JsonObject med, View dialogView, ListView lvResults) {
        lvResults.setVisibility(View.GONE);
        TextView tvSelected = dialogView.findViewById(R.id.tvSelectedMed);
        String medName = med.has("medName") ? med.get("medName").getAsString() : "";
        if (med.has("unit") && !med.get("unit").isJsonNull()) {
            medName += " (" + med.get("unit").getAsString() + ")";
        }
        tvSelected.setText("Đã chọn: " + medName);
        tvSelected.setVisibility(View.VISIBLE);
        tvSelected.setTag(med);

        dialogView.findViewById(R.id.etSearchMedication).setVisibility(View.GONE);
        View parent = (View) dialogView.findViewById(R.id.etSearchMedication).getParent();
        if (parent != null) parent.setVisibility(View.GONE);
    }

    @Override
    public boolean isValid() {
        return edtConsultationFee != null && edtConsultationFee.getText().toString().trim().length() > 0;
    }

    public String getConsultationFee() {
        return edtConsultationFee != null ? edtConsultationFee.getText().toString().trim() : "";
    }

    public JsonArray getPrescriptionItemsJson() {
        JsonArray items = new JsonArray();
        for (PrescriptionItem item : prescriptionItems) {
            JsonObject obj = new JsonObject();
            obj.addProperty("medicationId", item.medicationId);
            obj.addProperty("dosageInstruction", item.dosageInstruction);
            obj.addProperty("quantity", item.quantity);
            obj.addProperty("durationDays", item.durationDays);
            items.add(obj);
        }
        return items;
    }

    private class PrescriptionAdapter extends BaseAdapter {
        @Override
        public int getCount() { return prescriptionItems.size(); }

        @Override
        public Object getItem(int position) { return prescriptionItems.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_prescription_simple, parent, false);
            }
            PrescriptionItem item = prescriptionItems.get(position);
            ((TextView) convertView.findViewById(R.id.tvMedName)).setText(item.medName);
            String dosage = item.dosageInstruction;
            dosage += " · SL: " + item.quantity + (item.unit.isEmpty() ? "" : " " + item.unit);
            dosage += " · " + item.durationDays + " ngày";
            ((TextView) convertView.findViewById(R.id.tvMedDosage)).setText(dosage);
            ((TextView) convertView.findViewById(R.id.tvMedPrice))
                    .setText(String.format("%,.0f₫", item.price * item.quantity));

            convertView.setOnLongClickListener(v -> {
                prescriptionItems.remove(position);
                notifyDataSetChanged();
                return true;
            });

            return convertView;
        }
    }
}
