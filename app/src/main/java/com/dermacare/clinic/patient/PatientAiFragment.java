package com.dermacare.clinic.patient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.AiDiagnosisService;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.AiPredictResponse;
import com.dermacare.clinic.data.api.model.AiTopKItem;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientAiFragment extends Fragment {
    private ImageView ivSelectedImage;
    private MaterialButton btnUploadImage;
    private ProgressBar pbLoading;
    private LinearLayout llResultContainer;
    private TextView tvPrediction;
    
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        Glide.with(this).load(selectedImageUri).into(ivSelectedImage);
                        btnUploadImage.setText("Phân tích ảnh này");
                        llResultContainer.setVisibility(View.GONE);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        ivSelectedImage = view.findViewById(R.id.ivSelectedImage);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        pbLoading = view.findViewById(R.id.pbLoading);
        llResultContainer = view.findViewById(R.id.llResultContainer);
        tvPrediction = view.findViewById(R.id.tvPrediction);

        btnUploadImage.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                // Open gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            } else {
                // Upload and predict
                uploadImageForPrediction();
            }
        });
    }

    private void uploadImageForPrediction() {
        if (selectedImageUri == null) return;
        
        btnUploadImage.setEnabled(false);
        pbLoading.setVisibility(View.VISIBLE);
        llResultContainer.setVisibility(View.GONE);
        
        try {
            // Get file from URI
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
            File tempFile = new File(requireContext().getCacheDir(), "temp_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);
            
            AiDiagnosisService service = ApiClient.getAiDiagnosisService(requireContext());
            service.predictDisease(body, 5).enqueue(new Callback<AiPredictResponse>() {
                @Override
                public void onResponse(Call<AiPredictResponse> call, Response<AiPredictResponse> response) {
                    btnUploadImage.setEnabled(true);
                    pbLoading.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        AiPredictResponse result = response.body();
                        if (result.isSuccess() && result.getPrediction() != null) {
                            displayResult(result);
                        } else {
                            Toast.makeText(requireContext(), "Lỗi: " + result.getError(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Lỗi server: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                    
                    // Reset to allow new image picking
                    selectedImageUri = null;
                    btnUploadImage.setText("Chọn ảnh khác");
                }

                @Override
                public void onFailure(Call<AiPredictResponse> call, Throwable t) {
                    btnUploadImage.setEnabled(true);
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Không thể kết nối mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("AiFragment", "Upload error", t);
                    
                    selectedImageUri = null;
                    btnUploadImage.setText("Thử lại");
                }
            });
            
        } catch (Exception e) {
            btnUploadImage.setEnabled(true);
            pbLoading.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void displayResult(AiPredictResponse result) {
        llResultContainer.setVisibility(View.VISIBLE);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Phân tích từ ảnh:\n");
        
        // Calculate sum of Top K probabilities for normalization
        double sumProb = result.getPrediction().getConfidence();
        for (AiTopKItem item : result.getPrediction().getTop_k()) {
            if (!item.getClass_name().equals(result.getPrediction().getPredicted_class())) {
                sumProb += item.getProbability();
            }
        }
        if (sumProb <= 0) sumProb = 1.0;

        String mainDisease = DiseaseTranslator.translate(result.getPrediction().getPredicted_class());
        int mainConf = (int) Math.round((result.getPrediction().getConfidence() / sumProb) * 100);
        
        sb.append("➤ ").append(mainDisease)
          .append(" (Tỷ lệ: ").append(mainConf).append("%)\n\n");
          
        sb.append("Các dự đoán khác:\n");
        for (AiTopKItem item : result.getPrediction().getTop_k()) {
            if (!item.getClass_name().equals(result.getPrediction().getPredicted_class())) {
                String subDisease = DiseaseTranslator.translate(item.getClass_name());
                int subConf = (int) Math.round((item.getProbability() / sumProb) * 100);
                if (subConf > 0) {
                    sb.append("   • ").append(subDisease)
                      .append(": ").append(subConf).append("%\n");
                }
            }
        }
        
        sb.append("\n⚠️ LƯU Ý QUAN TRỌNG:\n");
        sb.append("Kết quả trên chỉ mang tính chất tham khảo sơ bộ từ Trợ lý AI và KHÔNG thay thế cho chẩn đoán y khoa. ");
        sb.append("Vui lòng đến gặp Bác sĩ da liễu để được thăm khám và điều trị chính xác nhất.");
        
        tvPrediction.setText(sb.toString());
    }
}
