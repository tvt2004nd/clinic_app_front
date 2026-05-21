package com.dermacare.clinic.patient;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.UserService;
import com.dermacare.clinic.data.api.model.ChangePasswordRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText editOldPassword, editNewPassword, editConfirmPassword;
    private MaterialButton btnSavePassword;
    private ImageView btnBack;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userService = ApiClient.getUserService(this);

        // Bind Views
        btnBack = findViewById(R.id.btnBack);
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());

        // Save password click listener
        btnSavePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String oldPassword = editOldPassword.getText() != null ? editOldPassword.getText().toString() : "";
        String newPassword = editNewPassword.getText() != null ? editNewPassword.getText().toString() : "";
        String confirmPassword = editConfirmPassword.getText() != null ? editConfirmPassword.getText().toString() : "";

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tất cả các trường", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải chứa ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.equals(oldPassword)) {
            Toast.makeText(this, "Mật khẩu mới không được trùng với mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Xác nhận mật khẩu mới không trùng khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSavePassword.setEnabled(false);
        btnSavePassword.setText("Đang đổi mật khẩu...");

        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
        userService.changePassword(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSavePassword.setEnabled(true);
                btnSavePassword.setText("Đổi mật khẩu");

                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Đổi mật khẩu thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ChangePasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSavePassword.setEnabled(true);
                btnSavePassword.setText("Đổi mật khẩu");
                Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
