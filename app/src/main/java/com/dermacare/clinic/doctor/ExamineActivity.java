package com.dermacare.clinic.doctor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamineActivity extends AppCompatActivity {
    private int currentStep = 0;
    private MaterialButton btnNext, btnBack;
    private LinearLayout stepIndicator;
    private TextView tvStepTitle;
    private Long recordId = -1L;
    private long appointmentId = -1L;
    private boolean isLoading = true;
    private long consultationFeeAmount = 0L;

    private final String[] titles = {
        "Tiếp nhận bệnh nhân",
        "Khám tổn thương da",
        "Chẩn đoán cuối cùng",
        "Kê đơn thuốc",
        "Lịch tái khám"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examine);

        btnNext = findViewById(R.id.btnNextExamine);
        btnBack = findViewById(R.id.btnBack);
        stepIndicator = findViewById(R.id.stepIndicator);
        tvStepTitle = findViewById(R.id.tvStepTitle);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Quy trình khám");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        buildStepIndicator();

        btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateStep();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isLoading) return;

            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.examine_container);
            if (currentFragment instanceof ExamineStep && !((ExamineStep) currentFragment).isValid()) {
                String msg = "Vui lòng điền đầy đủ thông tin";
                if (currentStep == 3 && consultationFeeAmount <= 0) {
                    msg = "Không lấy được phí khám của bác sĩ. Vui lòng thử lại hoặc liên hệ quản trị.";
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                return;
            }

            // Save step data before advancing
            if (recordId != -1L) {
                saveStepData(currentStep, currentFragment);
            }

            if (currentStep == titles.length - 1) {
                completeExamination();
            } else {
                currentStep++;
                updateStep();
            }
        });

        appointmentId = getIntent().getLongExtra("appointmentId", -1L);
        if (appointmentId == -1L) {
            Toast.makeText(this, "Lỗi: không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        callIntake(appointmentId);
    }

    private void callIntake(long appointmentId) {
        btnNext.setEnabled(false);
        btnNext.setText("Đang tải...");

        JsonObject body = new JsonObject();
        body.addProperty("appointmentId", appointmentId);

        ApiClient.getExaminationService(this).intakePatient(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    recordId = data.get("recordId").getAsLong();
                    if (data.has("doctor") && data.get("doctor").isJsonObject()) {
                        com.google.gson.JsonObject doctor = data.getAsJsonObject("doctor");
                        if (doctor.has("consultationFee") && !doctor.get("consultationFee").isJsonNull()) {
                            consultationFeeAmount = (long) doctor.get("consultationFee").getAsDouble();
                        }
                    }
                    isLoading = false;
                    btnNext.setEnabled(true);
                    btnNext.setText("Tiếp tục");
                    updateStep();
                } else {
                    btnNext.setEnabled(true);
                    btnNext.setText("Tiếp tục");
                    Toast.makeText(ExamineActivity.this, "Không thể tạo hồ sơ khám", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnNext.setEnabled(true);
                btnNext.setText("Tiếp tục");
                Toast.makeText(ExamineActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveStepData(int step, Fragment fragment) {
        if (recordId == -1L) return;

        JsonObject body = new JsonObject();
        Call<JsonObject> call = null;

        switch (step) {
            case 1: // Skin lesion examination
                if (fragment instanceof ExamineSkinLesionFragment) {
                    body.addProperty("symptoms", ((ExamineSkinLesionFragment) fragment).getCombinedExaminationData());
                    call = ApiClient.getExaminationService(this).updateSymptoms(recordId, body);
                    saveExamPhotos(((ExamineSkinLesionFragment) fragment).getPhotoBitmaps());
                }
                break;
            case 2: // Diagnosis
                if (fragment instanceof ExamineDiagnosisFragment) {
                    ExamineDiagnosisFragment df = (ExamineDiagnosisFragment) fragment;
                    body.addProperty("finalDiagnosis", df.getFinalDiagnosisText());
                    String plan = df.getTreatmentPlan();
                    if (plan != null && !plan.isEmpty()) {
                        body.addProperty("treatmentPlan", plan);
                    }
                    call = ApiClient.getExaminationService(this).updateFinalDiagnosis(recordId, body);
                }
                break;
            case 3: // Prescription
                if (fragment instanceof ExaminePrescriptionFragment) {
                    ExaminePrescriptionFragment pf = (ExaminePrescriptionFragment) fragment;
                    if (pf.getConsultationFee() != null && !pf.getConsultationFee().isEmpty()) {
                        try {
                            consultationFeeAmount = Long.parseLong(pf.getConsultationFee().replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    JsonArray items = pf.getPrescriptionItemsJson();
                    body.add("items", items);
                    call = ApiClient.getExaminationService(this).updatePrescription(recordId, body);
                }
                break;
        }

        if (call != null) {
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        // Log error but don't block the flow
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // Log error but don't block the flow
                }
            });
        }
    }

    private void saveExamPhotos(List<Bitmap> bitmaps) {
        if (bitmaps == null || bitmaps.isEmpty()) return;
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (int i = 0; i < bitmaps.size(); i++) {
            Bitmap bm = bitmaps.get(i);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), baos.toByteArray());
            parts.add(MultipartBody.Part.createFormData("files", "photo_" + recordId + "_" + i + ".jpg", fileBody));
        }
        ApiClient.getExaminationService(this).uploadPhotos(recordId, parts).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    String errMsg = "Upload ảnh thất bại: " + response.code();
                    try {
                        if (response.errorBody() != null) errMsg += " - " + response.errorBody().string();
                    } catch (Exception ignored) {}
                    Toast.makeText(ExamineActivity.this, errMsg, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ExamineActivity.this, "Lỗi upload ảnh: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void completeExamination() {
        if (recordId == -1L) {
            Toast.makeText(this, "Lỗi: không tìm thấy hồ sơ khám", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false);
        btnNext.setText("Đang xử lý...");

        // First save follow-up data (step 4)
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.examine_container);
        if (currentFragment instanceof ExamineFollowUpFragment) {
            String followUpDate = ((ExamineFollowUpFragment) currentFragment).getFollowUpDate();
            String followUpReason = ((ExamineFollowUpFragment) currentFragment).getFollowUpReason();
            saveFollowUpThenComplete(followUpDate, followUpReason);
        } else {
            callCompleteVisit();
        }
    }

    private void saveFollowUpThenComplete(String dateStr, String reason) {
        if (dateStr.isEmpty()) {
            callCompleteVisit();
            return;
        }

        // Parse dd/MM/yyyy to yyyy-MM-dd
        String[] parts = dateStr.split("/");
        String isoDate;
        if (parts.length == 3) {
            isoDate = parts[2] + "-" + parts[1] + "-" + parts[0];
        } else {
            isoDate = dateStr;
        }

        JsonObject body = new JsonObject();
        body.addProperty("followUpDate", isoDate);
        body.addProperty("createAppointment", false);
        if (!reason.isEmpty()) {
            body.addProperty("reason", reason);
        }

        ApiClient.getExaminationService(this).scheduleFollowUp(recordId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                callCompleteVisit();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Still try to complete even if follow-up save fails
                callCompleteVisit();
            }
        });
    }

    private void callCompleteVisit() {
        ApiClient.getExaminationService(this).completeVisit(recordId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    // Mark appointment as completed so it disappears from doctor's schedule
                    markAppointmentCompleted();
                    // After completing the visit, create the invoice automatically
                    createInvoiceAfterComplete();
                } else {
                    btnNext.setEnabled(true);
                    btnNext.setText("Hoàn thành");
                    String errorMsg = "Không thể hoàn tất khám bệnh";
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        if (errorBody.contains("message")) {
                            JsonObject err = new Gson().fromJson(errorBody, JsonObject.class);
                            if (err.has("message")) errorMsg = err.get("message").getAsString();
                        } else {
                            errorMsg = errorBody;
                        }
                    } catch (Exception ignored) {}
                    new android.app.AlertDialog.Builder(ExamineActivity.this)
                            .setTitle("Lỗi")
                            .setMessage(errorMsg)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnNext.setEnabled(true);
                btnNext.setText("Hoàn thành");
                new android.app.AlertDialog.Builder(ExamineActivity.this)
                        .setTitle("Lỗi kết nối")
                        .setMessage("Không thể kết nối máy chủ: " + t.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void createInvoiceAfterComplete() {
        JsonObject body = new JsonObject();
        body.addProperty("recordId", recordId);
        if (consultationFeeAmount > 0) {
            body.addProperty("consultationFee", consultationFeeAmount);
        } else {
            body.addProperty("consultationFee", 150000);
        }

        ApiClient.getInvoiceService(this).createInvoice(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                btnNext.setEnabled(true);
                btnNext.setText("Hoàn thành");

                boolean invoiceCreated = response.isSuccessful();
                String msg = invoiceCreated
                        ? "Đã hoàn tất khám bệnh. Hệ thống đã tạo hóa đơn, bệnh nhân sẽ thanh toán tại mục Hóa đơn trong ứng dụng."
                        : "Đã hoàn tất khám bệnh. Không thể tạo hóa đơn tự động, vui lòng tạo thủ công.";

                new android.app.AlertDialog.Builder(ExamineActivity.this)
                        .setTitle("Hoàn tất khám bệnh")
                        .setMessage(msg)
                        .setPositiveButton("Về trang chủ", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnNext.setEnabled(true);
                btnNext.setText("Hoàn thành");

                new android.app.AlertDialog.Builder(ExamineActivity.this)
                        .setTitle("Hoàn tất khám bệnh")
                        .setMessage("Đã hoàn tất khám bệnh. Không thể tạo hóa đơn do lỗi kết nối, vui lòng tạo thủ công.")
                        .setPositiveButton("Về trang chủ", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        });
    }

    private void markAppointmentCompleted() {
        if (appointmentId == -1L) return;
        ApiClient.getAppointmentService(this).completeAppointment(appointmentId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
            }
        });
    }

    private int dp2px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void buildStepIndicator() {
        stepIndicator.removeAllViews();
        int circleSize = dp2px(40);
        for (int i = 0; i < titles.length; i++) {
            TextView circle = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(circleSize, circleSize);
            params.gravity = android.view.Gravity.CENTER_VERTICAL;
            circle.setLayoutParams(params);
            circle.setText(String.valueOf(i + 1));
            circle.setGravity(android.view.Gravity.CENTER);
            circle.setTextSize(16);
            circle.setTypeface(null, android.graphics.Typeface.BOLD);
            circle.setTextColor(ContextCompat.getColor(this, R.color.white));
            circle.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_inactive));
            circle.setElevation(dp2px(2));

            stepIndicator.addView(circle);

            if (i < titles.length - 1) {
                View line = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp2px(3), 1);
                lp.setMargins(dp2px(6), 0, dp2px(6), 0);
                lp.gravity = android.view.Gravity.CENTER_VERTICAL;
                line.setLayoutParams(lp);
                android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                gd.setCornerRadius(dp2px(2));
                gd.setColor(ContextCompat.getColor(this, R.color.white));
                gd.setAlpha((int) (0.25f * 255));
                line.setBackground(gd);
                stepIndicator.addView(line);
            }
        }
    }

    private void updateStep() {
        boolean isLastStep = currentStep == titles.length - 1;
        btnNext.setText(isLastStep ? "Hoàn thành" : "Tiếp tục");

        tvStepTitle.setText("Bước " + (currentStep + 1) + ": " + titles[currentStep]);

        Fragment fragment;
        switch (currentStep) {
            case 0:
                fragment = new ExamineAdmissionFragment();
                Bundle args = new Bundle();
                args.putLong("recordId", recordId);
                fragment.setArguments(args);
                break;
            case 1: fragment = new ExamineSkinLesionFragment(); break;
            case 2: fragment = new ExamineDiagnosisFragment(); break;
            case 3:
                fragment = new ExaminePrescriptionFragment();
                break;
            case 4: fragment = new ExamineFollowUpFragment(); break;
            default: fragment = new ExamineAdmissionFragment();
        }

        if (isLastStep && fragment instanceof ExamineFollowUpFragment) {
            ((ExamineFollowUpFragment) fragment).setExamineActivity(this);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.examine_container, fragment)
                .commit();

        updateStepIndicator();
    }

    public long getConsultationFeeAmount() {
        return consultationFeeAmount;
    }

    private void updateStepIndicator() {
        for (int i = 0; i < stepIndicator.getChildCount(); i++) {
            View child = stepIndicator.getChildAt(i);
            if (child instanceof TextView) {
                TextView circle = (TextView) child;
                int stepIndex = i / 2;
                if (stepIndex < currentStep) {
                    circle.setText("✓");
                    circle.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_active));
                    circle.setTextColor(ContextCompat.getColor(this, R.color.primary));
                    circle.setElevation(dp2px(3));
                } else if (stepIndex == currentStep) {
                    circle.setText(String.valueOf(stepIndex + 1));
                    circle.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_active));
                    circle.setTextColor(ContextCompat.getColor(this, R.color.primary));
                    circle.setElevation(dp2px(3));
                } else {
                    circle.setText(String.valueOf(stepIndex + 1));
                    circle.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_inactive));
                    circle.setTextColor(ContextCompat.getColor(this, R.color.white));
                    circle.setElevation(0);
                }
            } else if (child instanceof View && child != stepIndicator) {
                View line = child;
                int lineIndex = -1;
                for (int j = 0; j < stepIndicator.getChildCount(); j++) {
                    if (stepIndicator.getChildAt(j) == line) {
                        lineIndex = j;
                        break;
                    }
                }
                if (lineIndex != -1) {
                    int stepBeforeLine = lineIndex / 2;
                    android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                    gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    gd.setCornerRadius(dp2px(2));
                    if (stepBeforeLine < currentStep) {
                        gd.setColor(ContextCompat.getColor(this, R.color.primary));
                        gd.setAlpha(255);
                    } else {
                        gd.setColor(ContextCompat.getColor(this, R.color.white));
                        gd.setAlpha((int) (0.25f * 255));
                    }
                    line.setBackground(gd);
                }
            }
        }
    }
}
