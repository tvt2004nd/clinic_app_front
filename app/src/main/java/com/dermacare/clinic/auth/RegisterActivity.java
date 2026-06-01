package com.dermacare.clinic.auth;
 
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
 
import androidx.appcompat.app.AppCompatActivity;
 
import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.AuthService;
import com.dermacare.clinic.data.api.model.RegisterRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
 
import java.io.IOException;
 
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
 
public class RegisterActivity extends AppCompatActivity {
 
    private TextInputEditText inputUsername, inputFullName, inputEmail, inputPhone, inputPassword, inputConfirmPassword;
    private MaterialButton btnRegisterSubmit;
    private AuthService authService;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
 
        authService = ApiClient.getAuthService(this);
 
        inputUsername = findViewById(R.id.inputUsername);
        inputFullName = findViewById(R.id.inputFullName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
 
        btnRegisterSubmit.setOnClickListener(v -> handleRegistration());
 
        findViewById(R.id.btnLoginLink).setOnClickListener(v -> finish());
    }
 
    private void handleRegistration() {
        String username = inputUsername.getText() != null ? inputUsername.getText().toString().trim() : "";
        String fullName = inputFullName.getText() != null ? inputFullName.getText().toString().trim() : "";
        String email = inputEmail.getText() != null ? inputEmail.getText().toString().trim() : "";
        String phone = inputPhone.getText() != null ? inputPhone.getText().toString().trim() : "";
        String password = inputPassword.getText() != null ? inputPassword.getText().toString().trim() : "";
        String confirmPassword = inputConfirmPassword.getText() != null ? inputConfirmPassword.getText().toString().trim() : "";
 
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tất cả các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }
 
        if (username.length() < 3) {
            Toast.makeText(this, "Tên đăng nhập phải chứa ít nhất 3 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
 
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
 
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải chứa ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
 
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp", Toast.LENGTH_SHORT).show();
            return;
        }
 
        btnRegisterSubmit.setEnabled(false);
        btnRegisterSubmit.setText("Đang đăng ký...");
 
        RegisterRequest registerRequest = new RegisterRequest(
                username,
                email,
                password,
                fullName,
                phone,
                "PATIENT" // Registrations default to patient as per doctor policy
        );
 
        authService.register(registerRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnRegisterSubmit.setEnabled(true);
                btnRegisterSubmit.setText(R.string.register);
 
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "Đăng ký thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnRegisterSubmit.setEnabled(true);
                btnRegisterSubmit.setText(R.string.register);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
