package com.dermacare.clinic.auth;
 
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
 
import androidx.appcompat.app.AppCompatActivity;
 
import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.AuthService;
import com.dermacare.clinic.data.api.model.ForgotPasswordRequest;
import com.dermacare.clinic.data.api.model.ResetPasswordRequest;

import com.google.android.material.textfield.TextInputLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
 
import java.io.IOException;
 
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
 
public class ForgotPasswordActivity extends AppCompatActivity {
 
    private TextView tvTitle;
    private LinearLayout layoutStep1, layoutStep2;
    private TextInputEditText inputForgotEmail, inputOtp, inputNewPassword, inputConfirmNewPassword;


    private TextInputLayout tilForgotEmail, tilOtp, tilNewPassword, tilConfirmNewPassword;

    private MaterialButton btnSendOtp, btnResetPassword;
    private AuthService authService;
    private String savedEmail = "";
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
 
        authService = ApiClient.getAuthService(this);
 
        tvTitle = findViewById(R.id.tvTitle);
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
 
        inputForgotEmail = findViewById(R.id.inputForgotEmail);
        inputOtp = findViewById(R.id.inputOtp);
        inputNewPassword = findViewById(R.id.inputNewPassword);
        inputConfirmNewPassword = findViewById(R.id.inputConfirmNewPassword);
 

        tilForgotEmail = findViewById(R.id.tilForgotEmail);
        tilOtp = findViewById(R.id.tilOtp);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmNewPassword = findViewById(R.id.tilConfirmNewPassword);
 

        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnResetPassword = findViewById(R.id.btnResetPassword);
 
        btnSendOtp.setOnClickListener(v -> sendOtpCode());
        btnResetPassword.setOnClickListener(v -> resetPasswordWithOtp());
 
        findViewById(R.id.btnBackToLogin).setOnClickListener(v -> finish());

    }
 
    private void sendOtpCode() {
        String email = inputForgotEmail.getText() != null ? inputForgotEmail.getText().toString().trim() : "";
 
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email của bạn", Toast.LENGTH_SHORT).show();


        setupTextWatcher(inputForgotEmail, tilForgotEmail);
        setupTextWatcher(inputOtp, tilOtp);
        setupTextWatcher(inputNewPassword, tilNewPassword);
        setupTextWatcher(inputConfirmNewPassword, tilConfirmNewPassword);
    }
 
    private void setupTextWatcher(TextInputEditText editText, TextInputLayout inputLayout) {
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLayout.setError(null);
            }
 
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
 
    private void sendOtpCode() {
        tilForgotEmail.setError(null);
        String email = inputForgotEmail.getText() != null ? inputForgotEmail.getText().toString().trim() : "";
 
        if (email.isEmpty()) {
            tilForgotEmail.setError("Vui lòng nhập email của bạn");

            return;
        }
 
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            Toast.makeText(this, "Địa chỉ email không hợp lệ", Toast.LENGTH_SHORT).show();

            tilForgotEmail.setError("Địa chỉ email không hợp lệ");

            return;
        }
 
        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Đang gửi...");
 
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        authService.forgotPassword(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSendOtp.setEnabled(true);
                btnSendOtp.setText("Gửi mã OTP");
 
                if (response.isSuccessful()) {
                    savedEmail = email;
                    Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi thành công đến email của bạn! Vui lòng kiểm tra hộp thư (và thư rác).", Toast.LENGTH_LONG).show();
                    
                    // Switch to Step 2
                    tvTitle.setText("Đặt lại mật khẩu");
                    layoutStep1.setVisibility(View.GONE);
                    layoutStep2.setVisibility(View.VISIBLE);
                } else {

                    Toast.makeText(ForgotPasswordActivity.this, "Gửi OTP thất bại. Vui lòng kiểm tra lại email.", Toast.LENGTH_LONG).show();

                    String errorMsg = "Gửi OTP thất bại. Vui lòng kiểm tra lại email.";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                            if (errorMsg.startsWith("Error: ")) {
                                errorMsg = errorMsg.substring(7);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tilForgotEmail.setError(errorMsg);
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSendOtp.setEnabled(true);
                btnSendOtp.setText("Gửi mã OTP");
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
 
    private void resetPasswordWithOtp() {


        tilOtp.setError(null);
        tilNewPassword.setError(null);
        tilConfirmNewPassword.setError(null);
 

        String otp = inputOtp.getText() != null ? inputOtp.getText().toString().trim() : "";
        String newPassword = inputNewPassword.getText() != null ? inputNewPassword.getText().toString().trim() : "";
        String confirmPassword = inputConfirmNewPassword.getText() != null ? inputConfirmNewPassword.getText().toString().trim() : "";
 

        if (otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
 
        if (otp.length() != 6) {
            Toast.makeText(this, "Mã OTP phải chứa 6 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }
 
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải chứa ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
 
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu mới không trùng khớp", Toast.LENGTH_SHORT).show();

        boolean hasError = false;
 
        if (otp.isEmpty()) {
            tilOtp.setError("Vui lòng nhập mã OTP");
            hasError = true;
        } else if (otp.length() != 6) {
            tilOtp.setError("Mã OTP phải chứa 6 chữ số");
            hasError = true;
        }
 
        if (newPassword.isEmpty()) {
            tilNewPassword.setError("Vui lòng nhập mật khẩu mới");
            hasError = true;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError("Mật khẩu phải chứa ít nhất 6 ký tự");
            hasError = true;
        }
 
        if (confirmPassword.isEmpty()) {
            tilConfirmNewPassword.setError("Vui lòng xác nhận mật khẩu mới");
            hasError = true;
        } else if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            tilConfirmNewPassword.setError("Mật khẩu mới không trùng khớp");
            hasError = true;
        }
 
        if (hasError) {

            return;
        }
 
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Đang thực hiện...");
 
        ResetPasswordRequest request = new ResetPasswordRequest(savedEmail, otp, newPassword);
        authService.resetPassword(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Đặt lại mật khẩu");
 
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "Đặt lại mật khẩu thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();

                            if (errorMsg.startsWith("Error: ")) {
                                errorMsg = errorMsg.substring(7);
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Đặt lại mật khẩu");
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
 
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
