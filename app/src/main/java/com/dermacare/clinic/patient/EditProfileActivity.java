package com.dermacare.clinic.patient;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.UserService;
import com.dermacare.clinic.data.api.model.ProfileUpdateRequest;
import com.dermacare.clinic.data.api.model.UserProfileResponse;
import com.dermacare.clinic.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ShapeableImageView imgEditAvatar;
    private LinearLayout layoutChangeAvatar;
    private TextInputEditText editFullName, editPhone, editDob, editAddress;
    private RadioGroup radioGroupGender;
    private RadioButton radioMale, radioFemale, radioOther;
    private MaterialButton btnSaveProfile;
    private ImageView btnBack;

    private SessionManager sessionManager;
    private UserService userService;
    private Calendar calendar;

    private String currentAvatarUrl = "";
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);
        userService = ApiClient.getUserService(this);
        calendar = Calendar.getInstance();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAvatarFile(uri);
                    }
                }
        );

        btnBack = findViewById(R.id.btnBack);
        imgEditAvatar = findViewById(R.id.imgEditAvatar);
        layoutChangeAvatar = findViewById(R.id.layoutChangeAvatar);
        editFullName = findViewById(R.id.editFullName);
        editPhone = findViewById(R.id.editPhone);
        editDob = findViewById(R.id.editDob);
        editAddress = findViewById(R.id.editAddress);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioOther = findViewById(R.id.radioOther);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnBack.setOnClickListener(v -> finish());
        editDob.setOnClickListener(v -> showDatePicker());
        layoutChangeAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        loadProfileFromSession();
        fetchProfileFromServer();

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadProfileFromSession() {
        editFullName.setText(sessionManager.getName());
        editPhone.setText(sessionManager.getPhone());
        editDob.setText(sessionManager.getDob());
        editAddress.setText(sessionManager.getAddress());
        currentAvatarUrl = sessionManager.getAvatar();

        String gender = sessionManager.getGender();
        if ("MALE".equalsIgnoreCase(gender)) {
            radioMale.setChecked(true);
        } else if ("FEMALE".equalsIgnoreCase(gender)) {
            radioFemale.setChecked(true);
        } else {
            radioOther.setChecked(true);
        }

        updateAvatarPreview(currentAvatarUrl);
    }

    private void updateAvatarPreview(String url) {
        if (url != null && !url.trim().isEmpty()) {
            String fullUrl = url;
            if (url.startsWith("/")) {
                fullUrl = ApiClient.BASE_URL + url.substring(1);
            } else if (!url.startsWith("http")) {
                fullUrl = ApiClient.BASE_URL + url;
            }
            Glide.with(this)
                    .load(fullUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .into(imgEditAvatar);
        } else {
            imgEditAvatar.setImageResource(R.drawable.ic_nav_profile);
        }
    }

    private void uploadAvatarFile(Uri uri) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("avatar_upload", ".jpg", getCacheDir());

            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                if (inputStream == null) {
                    Toast.makeText(this, "Không thể mở ảnh đã chọn", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

            Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

            File finalTempFile = tempFile;
            userService.uploadAvatar(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    deleteTempFile(finalTempFile);
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            currentAvatarUrl = json.getString("avatarUrl");
                            sessionManager.saveProfile(
                                    sessionManager.getName(),
                                    sessionManager.getPhone(),
                                    sessionManager.getGender(),
                                    sessionManager.getDob(),
                                    sessionManager.getAddress(),
                                    currentAvatarUrl
                            );
                            updateAvatarPreview(currentAvatarUrl);
                            Toast.makeText(EditProfileActivity.this, "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Lỗi đọc phản hồi upload", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Tải ảnh lên thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    deleteTempFile(finalTempFile);
                    Toast.makeText(EditProfileActivity.this, "Lỗi kết nối upload: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            deleteTempFile(tempFile);
            Toast.makeText(this, "Không thể mở hoặc xử lý tệp tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String currentDob = editDob.getText() != null ? editDob.getText().toString() : "";
        if (!currentDob.isEmpty()) {
            try {
                String[] parts = currentDob.split("-");
                if (parts.length == 3) {
                    year = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]) - 1;
                    day = Integer.parseInt(parts[2]);
                }
            } catch (Exception ignored) {
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    editDob.setText(sdf.format(calendar.getTime()));
                }, year, month, day);

        datePickerDialog.show();
    }

    private void fetchProfileFromServer() {
        userService.getProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse profile = response.body();
                    sessionManager.saveProfile(
                            profile.getFullName(),
                            profile.getPhone(),
                            profile.getGender(),
                            profile.getDateOfBirth(),
                            profile.getAddress(),
                            profile.getAvatarUrl()
                    );
                    loadProfileFromSession();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
            }
        });
    }

    private void saveProfileChanges() {
        String fullName = editFullName.getText() != null ? editFullName.getText().toString().trim() : "";
        String phone = editPhone.getText() != null ? editPhone.getText().toString().trim() : "";
        String dob = editDob.getText() != null ? editDob.getText().toString().trim() : "";
        String address = editAddress.getText() != null ? editAddress.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Họ và tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = "OTHER";
        int checkedId = radioGroupGender.getCheckedRadioButtonId();
        if (checkedId == R.id.radioMale) {
            gender = "MALE";
        } else if (checkedId == R.id.radioFemale) {
            gender = "FEMALE";
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Đang lưu...");

        ProfileUpdateRequest request = new ProfileUpdateRequest(fullName, phone, gender, dob, address, currentAvatarUrl);
        userService.updateProfile(request).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Lưu");

                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse profile = response.body();
                    sessionManager.saveProfile(
                            profile.getFullName(),
                            profile.getPhone(),
                            profile.getGender(),
                            profile.getDateOfBirth(),
                            profile.getAddress(),
                            profile.getAvatarUrl()
                    );

                    Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Cập nhật thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException ignored) {
                    }
                    Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Lưu");
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
