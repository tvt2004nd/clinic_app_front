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
    private MaterialCardView followUpCard, examResultsCard;
    private View btnBack, btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        recordId = getIntent().getLongExtra("recordId", -1);

        layoutLoading = findViewById(R.id.layoutLoadingDetail);
        layoutContent = findViewById(R.id.layoutContent);
        layoutError = findViewById(R.id.layoutErrorDetail);
        btnBack = findViewById(R.id.btnBack);
        btnRetry = findViewById(R.id.btnRetryDetail);

        tvRecordCode = findViewById(R.id.tvRecordCode);
        tvDoctorName2 = findViewById(R.id.tvDoctorName2);
        tvDoctorTitle = findViewById(R.id.tvDoctorTitle);
        tvDoctorAvatar = findViewById(R.id.tvDoctorAvatar);
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

        btnBack.setOnClickListener(v -> finish());

        if (recordId != -1) {
            fetchDetail();
        } else {
            Toast.makeText(this, "Không tìm thấy hồ sơ", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnRetry.setOnClickListener(v -> {
            showLoading();
            fetchDetail();
        });
    }

    private void fetchDetail() {
        ApiClient.getExaminationService(this).getMedicalRecord(recordId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isFinishing() && response.isSuccessful() && response.body() != null) {
                            bindData(response.body());
                        } else {
                            showError();
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
    }

    private void bindData(JsonObject data) {
        String recordCode = getString(data, "recordCode");
        tvRecordCode.setText(recordCode != null ? recordCode : "HS-...");

        JsonObject doctor = null;
        JsonElement doctorEl = data.get("doctor");
        if (doctorEl != null && !doctorEl.isJsonNull()) {
            doctor = doctorEl.getAsJsonObject();
        }
        if (doctor != null) {
            String name = getString(doctor, "fullName");
            String title = getString(doctor, "title");
            tvDoctorName2.setText(name != null ? name : "Bác sĩ");
            tvDoctorTitle.setText(title != null ? title : "");
            String initials = name != null && name.length() > 0 ? name.substring(0, 1).toUpperCase() : "B";
            tvDoctorAvatar.setText(initials);
        }

        JsonObject finalDisease = null;
        JsonElement finalDiseaseEl = data.get("finalDisease");
        if (finalDiseaseEl != null && !finalDiseaseEl.isJsonNull()) {
            finalDisease = finalDiseaseEl.getAsJsonObject();
        }
        String diagnosis = getString(data, "finalDiagnosis");
        String diseaseName = finalDisease != null ? getString(finalDisease, "diseaseNameVi") : null;

        if (diseaseName != null) {
            tvDiagnosisTitle.setText(diseaseName);
        } else if (diagnosis != null && !diagnosis.isEmpty()) {
            tvDiagnosisTitle.setText(diagnosis.length() > 50 ? diagnosis.substring(0, 50) + "..." : diagnosis);
        } else {
            tvDiagnosisTitle.setText("Khám bệnh");
        }
        tvDiagnosisDetail.setText(diagnosis != null && !diagnosis.isEmpty() ? diagnosis : "Không có mô tả");

        String symptoms = getString(data, "symptoms");
        if (symptoms != null && !symptoms.isEmpty()) {
            examResultsCard.setVisibility(View.VISIBLE);
            populateExamSections(symptoms);
        } else {
            examResultsCard.setVisibility(View.GONE);
        }

        loadExamPhotos(data);

        String treatmentPlan = getString(data, "treatmentPlan");
        if (treatmentPlan != null && !treatmentPlan.isEmpty()) {
            tvTreatmentPlanLabel.setVisibility(View.VISIBLE);
            tvTreatmentPlan.setVisibility(View.VISIBLE);
            tvTreatmentPlan.setText(treatmentPlan);
        }

        JsonArray prescriptionItems = null;
        JsonElement prescriptionEl = data.get("prescriptionItems");
        if (prescriptionEl != null && !prescriptionEl.isJsonNull()) {
            prescriptionItems = prescriptionEl.getAsJsonArray();
        }
        if (prescriptionItems != null && prescriptionItems.size() > 0) {
            tvNoPrescription.setVisibility(View.GONE);
            prescriptionList.removeAllViews();
            for (int i = 0; i < prescriptionItems.size(); i++) {
                JsonObject item = prescriptionItems.get(i).getAsJsonObject();
                View itemView = getLayoutInflater().inflate(R.layout.item_prescription_simple, prescriptionList, false);
                ((TextView) itemView.findViewById(R.id.tvMedName))
                        .setText(getString(item, "medName"));
                ((TextView) itemView.findViewById(R.id.tvMedDosage))
                        .setText(formatDosage(item));
                ((TextView) itemView.findViewById(R.id.tvMedPrice))
                        .setText(formatPrice(item));
                ImageView ivMed = itemView.findViewById(R.id.ivMedImage);
                String imageUrl = getString(item, "imageUrl");
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
                prescriptionList.addView(itemView);

                if (i < prescriptionItems.size() - 1) {
                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(getColor(R.color.border));
                    divider.setMinimumHeight(1);
                    prescriptionList.addView(divider);
                }
            }
        } else {
            tvNoPrescription.setVisibility(View.VISIBLE);
        }

        String followUpDate = getString(data, "followUpDate");
        if (followUpDate != null && !followUpDate.isEmpty()) {
            followUpCard.setVisibility(View.VISIBLE);
            tvFollowUpDate.setText("Hẹn tái khám: " + followUpDate);
        } else {
            followUpCard.setVisibility(View.GONE);
        }

        String examinedAt = getString(data, "examinedAt");
        if (examinedAt != null && examinedAt.length() >= 16) {
            tvExaminedAt.setText("Khám lúc: " + examinedAt.substring(0, 16).replace("T", " "));
        } else {
            tvExaminedAt.setVisibility(View.GONE);
        }

        showContent();
    }

    private String getString(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    private void loadExamPhotos(JsonObject data) {
        photoGalleryDetail.removeAllViews();
        JsonElement urlsEl = data.get("photoUrls");
        if (urlsEl == null || urlsEl.isJsonNull()) return;
        JsonArray urls = urlsEl.getAsJsonArray();
        if (urls.size() == 0) return;
        tvPhotoTitle.setVisibility(View.VISIBLE);
        photoGalleryDetail.setVisibility(View.VISIBLE);
        int size = (int) (100 * getResources().getDisplayMetrics().density);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < urls.size(); i++) {
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
            photoGalleryDetail.addView(iv);
        }
    }

    private final int[] SECTION_COLORS = {0xFF0D9488, 0xFF2563EB, 0xFF7C3AED, 0xFFEA580C};

    private void populateExamSections(String symptoms) {
        examSectionContainer.removeAllViews();
        String[] rawSections = symptoms.split("\n(?=== )");
        int idx = 0;
        for (String raw : rawSections) {
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
            tvTitle.setText(title);
            tvTitle.setBackgroundColor(color);
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
                    ((TextView) fieldView.findViewById(R.id.tvFieldLabel)).setText(label);
                    ((TextView) fieldView.findViewById(R.id.tvFieldValue)).setText(value);
                    content.addView(fieldView);
                }
            }
            if (!hasFields) continue;
            if (title.contains("ẢNH")) continue;
            examSectionContainer.addView(sectionView);
            idx++;
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
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void showContent() {
        layoutLoading.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
    }

    private void showError() {
        layoutLoading.setVisibility(View.GONE);
        layoutContent.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
    }
}
