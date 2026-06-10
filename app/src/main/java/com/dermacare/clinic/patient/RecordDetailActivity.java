package com.dermacare.clinic.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RecordDetailActivity extends AppCompatActivity {

    private Long recordId;
    private View layoutLoading, layoutContent, layoutError;
    private TextView tvRecordCode, tvDoctorName2, tvDoctorTitle, tvDoctorAvatar;
    private TextView tvDiagnosisTitle, tvDiagnosisDetail;
    private TextView tvTreatmentPlan, tvTreatmentPlanLabel;
    private TextView tvFollowUpDate, tvExaminedAt;
    private LinearLayout prescriptionList, examSectionContainer, photoGalleryDetail;
    private TextView tvNoPrescription, tvPhotoTitle;
    private LinearLayout followUpCard, examResultsCard;
    private View btnBack, btnRetry;
    private ImageView btnExportPdf, ivDoctorAvatar;
    private JsonObject currentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_record_detail);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tải layout: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recordId = getIntent().getLongExtra("recordId", -1);

        try {
            layoutLoading = findViewById(R.id.layoutLoadingDetail);
            layoutContent = findViewById(R.id.layoutContent);
            layoutError = findViewById(R.id.layoutErrorDetail);
            btnBack = findViewById(R.id.btnBack);
            btnRetry = findViewById(R.id.btnRetryDetail);
            btnExportPdf = findViewById(R.id.btnExportPdf);

            if (layoutLoading == null || layoutContent == null || layoutError == null) {
                throw new RuntimeException("Missing required layout views: loading=" + (layoutLoading != null) + 
                    ", content=" + (layoutContent != null) + ", error=" + (layoutError != null));
            }

            tvRecordCode = findViewById(R.id.tvRecordCode);
            tvDoctorName2 = findViewById(R.id.tvDoctorName2);
            tvDoctorTitle = findViewById(R.id.tvDoctorTitle);
            tvDoctorAvatar = findViewById(R.id.tvDoctorAvatar);
            ivDoctorAvatar = findViewById(R.id.ivDoctorAvatar);
            tvDiagnosisTitle = findViewById(R.id.tvDiagnosisTitle);
            tvDiagnosisDetail = findViewById(R.id.tvDiagnosisDetail);

            tvTreatmentPlan = findViewById(R.id.tvTreatmentPlan);
            tvTreatmentPlanLabel = findViewById(R.id.tvTreatmentPlanLabel);
            tvFollowUpDate = findViewById(R.id.tvFollowUpDate);
            tvExaminedAt = findViewById(R.id.tvExaminedAt);
            prescriptionList = findViewById(R.id.prescriptionList);
            tvNoPrescription = findViewById(R.id.tvNoPrescription);
            followUpCard = findViewById(R.id.followUpCard);
            examResultsCard = findViewById(R.id.examResultsCard);
            examSectionContainer = findViewById(R.id.examSectionContainer);
            photoGalleryDetail = findViewById(R.id.photoGalleryDetail);
            tvPhotoTitle = findViewById(R.id.tvPhotoTitle);

            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            if (btnExportPdf != null) {
                btnExportPdf.setOnClickListener(v -> exportPdf());
                btnExportPdf.setVisibility(View.GONE); // Ẩn khi chưa có dữ liệu
            }

            if (recordId != -1) {
                fetchDetail();
            } else {
                Toast.makeText(this, "Không tìm thấy hồ sơ", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (btnRetry != null) {
                btnRetry.setOnClickListener(v -> {
                    showLoading();
                    fetchDetail();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("RecordDetailActivity", "Initialization error", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void fetchDetail() {
        try {
            showLoading();
            ApiClient.getExaminationService(this).getMedicalRecord(recordId)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            try {
                                if (!isFinishing()) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        bindData(response.body());
                                    } else {
                                        showError();
                                        Toast.makeText(RecordDetailActivity.this,
                                                "Không thể tải dữ liệu (Lỗi " + response.code() + ")",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (!isFinishing()) {
                                    showError();
                                    Toast.makeText(RecordDetailActivity.this,
                                            "Lỗi xử lý phản hồi: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            if (!isFinishing()) {
                                showError();
                                Toast.makeText(RecordDetailActivity.this,
                                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showError();
            Toast.makeText(this, "Lỗi khi gửi yêu cầu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void bindData(JsonObject data) {
        try {
            currentData = data;
            if (btnExportPdf != null) btnExportPdf.setVisibility(View.VISIBLE);

            String recordCode = getString(data, "recordCode");
            if (tvRecordCode != null) {
                tvRecordCode.setText(recordCode != null ? recordCode : "HS-...");
            }

            JsonObject doctor = null;
            JsonElement doctorEl = data.get("doctor");
            if (doctorEl != null && !doctorEl.isJsonNull()) {
                doctor = doctorEl.getAsJsonObject();
            }
            if (doctor != null) {
                String name = getString(doctor, "fullName");
                String title = getString(doctor, "title");
                if (tvDoctorName2 != null) tvDoctorName2.setText(name != null ? name : "Bác sĩ");
                if (tvDoctorTitle != null) tvDoctorTitle.setText(title != null ? title : "");
                if (tvDoctorAvatar != null) {
                    String initials = name != null && name.length() > 0 ? name.substring(0, 1).toUpperCase() : "B";
                    tvDoctorAvatar.setText(initials);
                }
                String avatarUrl = getString(doctor, "avatarUrl");
                if (ivDoctorAvatar != null) {
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        ivDoctorAvatar.setVisibility(View.VISIBLE);
                        Glide.with(this).load(avatarUrl).into(ivDoctorAvatar);
                    } else {
                        ivDoctorAvatar.setVisibility(View.GONE);
                    }
                }
            }

            JsonObject finalDisease = null;
            JsonElement finalDiseaseEl = data.get("finalDisease");
            if (finalDiseaseEl != null && !finalDiseaseEl.isJsonNull()) {
                finalDisease = finalDiseaseEl.getAsJsonObject();
            }
            String diagnosis = getString(data, "finalDiagnosis");
            String diseaseName = finalDisease != null ? getString(finalDisease, "diseaseNameVi") : null;

            if (tvDiagnosisTitle != null) {
                if (diseaseName != null) {
                    tvDiagnosisTitle.setText(diseaseName);
                } else if (diagnosis != null && !diagnosis.isEmpty()) {
                    tvDiagnosisTitle.setText(diagnosis.length() > 50 ? diagnosis.substring(0, 50) + "..." : diagnosis);
                } else {
                    tvDiagnosisTitle.setText("Khám bệnh");
                }
            }
            if (tvDiagnosisDetail != null) {
                tvDiagnosisDetail.setText(diagnosis != null && !diagnosis.isEmpty() ? diagnosis : "Không có mô tả");
            }

            String symptoms = getString(data, "symptoms");
            if (symptoms != null && !symptoms.isEmpty()) {
                if (examResultsCard != null) examResultsCard.setVisibility(View.VISIBLE);
                populateExamSections(symptoms);
            } else {
                if (examResultsCard != null) examResultsCard.setVisibility(View.GONE);
            }

            loadExamPhotos(data);

            String treatmentPlan = getString(data, "treatmentPlan");
            if (treatmentPlan != null && !treatmentPlan.isEmpty()) {
                if (tvTreatmentPlanLabel != null) tvTreatmentPlanLabel.setVisibility(View.VISIBLE);
                if (tvTreatmentPlan != null) {
                    tvTreatmentPlan.setVisibility(View.VISIBLE);
                    tvTreatmentPlan.setText(treatmentPlan);
                }
            }

            JsonArray prescriptionItems = null;
            JsonElement prescriptionEl = data.get("prescriptionItems");
            if (prescriptionEl != null && !prescriptionEl.isJsonNull()) {
                prescriptionItems = prescriptionEl.getAsJsonArray();
            }
            if (prescriptionItems != null && prescriptionItems.size() > 0) {
                if (tvNoPrescription != null) tvNoPrescription.setVisibility(View.GONE);
                if (prescriptionList != null) {
                    prescriptionList.removeAllViews();
                    for (int i = 0; i < prescriptionItems.size(); i++) {
                        try {
                            JsonObject item = prescriptionItems.get(i).getAsJsonObject();
                            View itemView = getLayoutInflater().inflate(R.layout.item_prescription_simple, prescriptionList, false);
                            TextView tvMedName = itemView.findViewById(R.id.tvMedName);
                            TextView tvMedDosage = itemView.findViewById(R.id.tvMedDosage);
                            TextView tvMedPrice = itemView.findViewById(R.id.tvMedPrice);
                            ImageView ivMed = itemView.findViewById(R.id.ivMedImage);
                            
                            if (tvMedName != null) tvMedName.setText(getString(item, "medName"));
                            if (tvMedDosage != null) tvMedDosage.setText(formatDosage(item));
                            if (tvMedPrice != null) tvMedPrice.setText(formatPrice(item));
                            
                            String imageUrl = getString(item, "imageUrl");
                            if (ivMed != null) {
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Glide.with(this)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_nav_records)
                                            .centerCrop()
                                            .into(ivMed);
                                } else {
                                    ivMed.setImageResource(R.drawable.ic_nav_records);
                                    ivMed.setColorFilter(0xFFB45309);
                                    ivMed.setPadding(14, 14, 14, 14);
                                }
                            }
                            prescriptionList.addView(itemView);

                            if (i < prescriptionItems.size() - 1) {
                                View divider = new View(this);
                                divider.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                                divider.setBackgroundColor(getColor(R.color.border));
                                divider.setMinimumHeight(1);
                                prescriptionList.addView(divider);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                if (tvNoPrescription != null) tvNoPrescription.setVisibility(View.VISIBLE);
            }

            String followUpDate = getString(data, "followUpDate");
            if (followUpDate != null && !followUpDate.isEmpty()) {
                if (followUpCard != null) followUpCard.setVisibility(View.VISIBLE);
                if (tvFollowUpDate != null) tvFollowUpDate.setText("Hẹn tái khám: " + followUpDate);
            } else {
                if (followUpCard != null) followUpCard.setVisibility(View.GONE);
            }

            String examinedAt = getString(data, "examinedAt");
            if (examinedAt != null && examinedAt.length() >= 16) {
                if (tvExaminedAt != null) tvExaminedAt.setText("Khám lúc: " + examinedAt.substring(0, 16).replace("T", " "));
            } else {
                if (tvExaminedAt != null) tvExaminedAt.setVisibility(View.GONE);
            }

            showContent();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            showError();
        }
    }

    private String getString(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    private void loadExamPhotos(JsonObject data) {
        try {
            if (photoGalleryDetail != null) {
                photoGalleryDetail.removeAllViews();
            }
            JsonElement urlsEl = data.get("photoUrls");
            if (urlsEl == null || urlsEl.isJsonNull()) return;
            JsonArray urls = urlsEl.getAsJsonArray();
            if (urls.size() == 0) return;
            if (tvPhotoTitle != null) tvPhotoTitle.setVisibility(View.VISIBLE);
            if (photoGalleryDetail != null) photoGalleryDetail.setVisibility(View.VISIBLE);
            int size = (int) (100 * getResources().getDisplayMetrics().density);
            int margin = (int) (8 * getResources().getDisplayMetrics().density);
            for (int i = 0; i < urls.size(); i++) {
                try {
                    String url = urls.get(i).getAsString();
                    if (url == null || url.isEmpty()) continue;
                    ImageView iv = new ImageView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.rightMargin = margin;
                    iv.setLayoutParams(params);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    iv.setClipToOutline(true);
                    iv.setBackgroundResource(R.drawable.bg_card);
                    Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.ic_nav_records)
                            .centerCrop()
                            .into(iv);
                    if (photoGalleryDetail != null) photoGalleryDetail.addView(iv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final int[] SECTION_COLORS = {0xFF0D9488, 0xFF2563EB, 0xFF7C3AED, 0xFFEA580C};

    private void populateExamSections(String symptoms) {
        try {
            if (examSectionContainer != null) {
                examSectionContainer.removeAllViews();
            }
            if (symptoms == null || symptoms.isEmpty()) return;

            String[] rawSections = symptoms.split("\n(?=== )");
            int idx = 0;
            for (String raw : rawSections) {
                try {
                    String trimmed = raw.trim();
                    if (trimmed.isEmpty()) continue;
                    String title = "";
                    int titleEnd = trimmed.indexOf('\n');
                    String firstLine = titleEnd > 0 ? trimmed.substring(0, titleEnd).trim() : trimmed;
                    if (firstLine.startsWith("===")) {
                        title = firstLine.replaceAll("===", "").replaceAll("\\d+\\.\\s*", "").trim();
                        trimmed = titleEnd > 0 ? trimmed.substring(titleEnd + 1).trim() : "";
                    }
                    if (trimmed.isEmpty()) continue;
                    View sectionView = getLayoutInflater().inflate(R.layout.item_exam_section, examSectionContainer, false);
                    TextView tvTitle = sectionView.findViewById(R.id.tvSectionTitle);
                    LinearLayout content = sectionView.findViewById(R.id.sectionContent);
                    int color = SECTION_COLORS[idx % SECTION_COLORS.length];
                    if (tvTitle != null) {
                        tvTitle.setText(title);
                        tvTitle.setBackgroundColor(color);
                    }
                    String[] lines = trimmed.split("\n");
                    boolean hasFields = false;
                    for (String line : lines) {
                        String l = line.trim();
                        if (l.isEmpty()) continue;
                        int colon = l.indexOf(": ");
                        if (colon > 0) {
                            String label = l.substring(0, colon).trim();
                            String value = l.substring(colon + 2).trim();
                            if (value.isEmpty()) continue;
                            hasFields = true;
                            View fieldView = getLayoutInflater().inflate(R.layout.item_exam_field, content, false);
                            TextView tvLabel = fieldView.findViewById(R.id.tvFieldLabel);
                            TextView tvValue = fieldView.findViewById(R.id.tvFieldValue);
                            if (tvLabel != null) tvLabel.setText(label);
                            if (tvValue != null) tvValue.setText(value);
                            if (content != null) content.addView(fieldView);
                        }
                    }
                    if (!hasFields) continue;
                    if (title.contains("ẢNH")) continue;
                    if (examSectionContainer != null) examSectionContainer.addView(sectionView);
                    idx++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatDosage(JsonObject item) {
        String dosage = getString(item, "dosageInstruction");
        Integer qty = item.get("quantity") != null && !item.get("quantity").isJsonNull()
                ? item.get("quantity").getAsInt() : 0;
        String unit = getString(item, "unit");
        Integer days = item.get("durationDays") != null && !item.get("durationDays").isJsonNull()
                ? item.get("durationDays").getAsInt() : 0;

        StringBuilder sb = new StringBuilder();
        if (dosage != null && !dosage.isEmpty()) sb.append(dosage);
        if (qty > 0) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append("SL: ").append(qty);
            if (unit != null) sb.append(" ").append(unit);
        }
        if (days > 0) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(days).append(" ngày");
        }
        return sb.length() > 0 ? sb.toString() : "No dosage info";
    }

    private String formatPrice(JsonObject item) {
        if (item.get("totalPrice") != null && !item.get("totalPrice").isJsonNull()) {
            double price = item.get("totalPrice").getAsDouble();
            return String.format("%,.0f₫", price);
        }
        return "";
    }

    private void showLoading() {
        try {
            if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);
            if (layoutContent != null) layoutContent.setVisibility(View.GONE);
            if (layoutError != null) layoutError.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContent() {
        try {
            if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
            if (layoutContent != null) layoutContent.setVisibility(View.VISIBLE);
            if (layoutError != null) layoutError.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError() {
        try {
            if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
            if (layoutContent != null) layoutContent.setVisibility(View.GONE);
            if (layoutError != null) layoutError.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportPdf() {
        if (currentData == null) return;
        
        java.util.Map<String, String> pdfData = new java.util.HashMap<>();
        
        JsonObject patient = null;
        JsonElement patientEl = currentData.get("patient");
        if (patientEl != null && !patientEl.isJsonNull()) {
            patient = patientEl.getAsJsonObject();
        }
        
        if (patient != null) {
            pdfData.put("hoTen", getString(patient, "fullName") != null ? getString(patient, "fullName") : "");
            pdfData.put("ngaySinh", getString(patient, "birthDate") != null ? getString(patient, "birthDate") : "");
            pdfData.put("gioiTinh", getString(patient, "gender") != null ? getString(patient, "gender") : "");
            pdfData.put("diaChi", getString(patient, "address") != null ? getString(patient, "address") : "");
            pdfData.put("bhyt", getString(patient, "bhyt") != null ? getString(patient, "bhyt") : "");
        }
        
        pdfData.put("lyDo", getString(currentData, "symptoms") != null ? getString(currentData, "symptoms") : "");
        pdfData.put("diUng", getString(currentData, "allergy") != null ? getString(currentData, "allergy") : "");
        pdfData.put("tienSuGiaDinh", getString(currentData, "familyHistory") != null ? getString(currentData, "familyHistory") : "");
        pdfData.put("chanDoan", getString(currentData, "finalDiagnosis") != null ? getString(currentData, "finalDiagnosis") : "");
        
        JsonObject finalDisease = null;
        JsonElement finalDiseaseEl = currentData.get("finalDisease");
        if (finalDiseaseEl != null && !finalDiseaseEl.isJsonNull()) {
            finalDisease = finalDiseaseEl.getAsJsonObject();
        }
        pdfData.put("disease", finalDisease != null ? getString(finalDisease, "diseaseNameVi") : "");
        pdfData.put("dieuTri", getString(currentData, "treatmentPlan") != null ? getString(currentData, "treatmentPlan") : "");
        pdfData.put("followUp", getString(currentData, "followUpDate") != null ? getString(currentData, "followUpDate") : "");
        pdfData.put("recordCode", getString(currentData, "recordCode") != null ? getString(currentData, "recordCode") : "");
        
        JsonObject doctor = null;
        JsonElement doctorEl = currentData.get("doctor");
        if (doctorEl != null && !doctorEl.isJsonNull()) {
            doctor = doctorEl.getAsJsonObject();
        }
        String docName = doctor != null ? getString(doctor, "fullName") : "";
        pdfData.put("tenBacSi", "BS. " + docName);

        com.dermacare.clinic.util.PdfGenerator.exportMedicalRecordPdf(this, pdfData);
    }
}
