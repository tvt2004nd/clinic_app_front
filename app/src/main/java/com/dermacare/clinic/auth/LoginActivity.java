package com.dermacare.clinic.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.AuthService;
import com.dermacare.clinic.data.api.UserService;
import com.dermacare.clinic.data.api.model.GoogleLoginRequest;
import com.dermacare.clinic.data.api.model.JwtResponse;
import com.dermacare.clinic.data.api.model.LoginRequest;
import com.dermacare.clinic.data.api.model.UserProfileResponse;
import com.dermacare.clinic.doctor.DoctorMainActivity;
import com.dermacare.clinic.patient.PatientMainActivity;
import com.dermacare.clinic.util.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private static final String GOOGLE_CLIENT_ID = "981369855230-obdjqtioa4t401orm0t91s0ubn4d0fiq.apps.googleusercontent.com";

    private AuthService authService;
    private UserService userService;
    private SessionManager sessionManager;
    private GoogleSignInClient googleSignInClient;

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = ApiClient.getAuthService(this);
        userService = ApiClient.getUserService(this);
        sessionManager = new SessionManager(this);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> handleLogin());

        findViewById(R.id.btnRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.btnForgot).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        findViewById(R.id.btnGoogle).setOnClickListener(v -> startRealGoogleSignIn());
    }

    private void handleLogin() {
        String usernameOrEmail = inputEmail.getText() != null ? inputEmail.getText().toString().trim() : "";
        String password = inputPassword.getText() != null ? inputPassword.getText().toString() : "";

        if (usernameOrEmail.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email hoặc tên đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        LoginRequest loginRequest = new LoginRequest(usernameOrEmail, password);
        authService.login(loginRequest).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");

                if (response.isSuccessful() && response.body() != null) {
                    JwtResponse jwtResponse = response.body();
                    onLoginSuccess(jwtResponse);
                } else {
                    String errorMsg = "Đăng nhập thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JwtResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                Toast.makeText(LoginActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startRealGoogleSignIn() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void showMockGoogleDialog() {
        EditText input = new EditText(this);
        input.setHint("Tên tài khoản (Ví dụ: quangdat, test_user)");
        input.setText("demouser");
        input.setSingleLine(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng nhập Google Mock (Demo)")
                .setMessage("Nhập tên định danh tài khoản Google giả lập:")
                .setView(input)
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    String suffix = input.getText().toString().trim();
                    if (suffix.isEmpty()) {
                        suffix = "demouser";
                    }
                    sendGoogleTokenToBackend("mock_google_" + suffix);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showGoogleSignInErrorDialog(int statusCode) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Lỗi Đăng nhập Google")
                .setMessage("Không thể đăng nhập bằng Google (Mã lỗi: " + statusCode + ").\n\n" +
                        "Hướng dẫn cấu hình Google đăng nhập thật:\n" +
                        "1. Truy cập Google Cloud Console và tạo dự án.\n" +
                        "2. Đăng ký Package Name: com.dermacare.clinic\n" +
                        "3. Điền mã SHA-1 của bạn:\n   4A:56:A0:F4:44:A1:6A:73:D1:36:2C:8A:50:8F:64:F1:79:B5:65:B3\n" +
                        "4. Tạo Web Client ID và cập nhật biến GOOGLE_CLIENT_ID trong LoginActivity.java.\n\n" +
                        "Bạn có muốn dùng thử tài khoản giả lập (Mock) để tiếp tục kiểm tra ứng dụng không?")
                .setPositiveButton("Sử dụng Mock", (dialog, which) -> showMockGoogleDialog())
                .setNegativeButton("Đóng", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    sendGoogleTokenToBackend(account.getIdToken());
                } else {
                    Toast.makeText(this, "Đăng nhập Google thành công nhưng không lấy được ID Token.", Toast.LENGTH_LONG).show();
                }
            } catch (ApiException e) {
                showGoogleSignInErrorDialog(e.getStatusCode());
            }
        }
    }

    private void sendGoogleTokenToBackend(String idToken) {
        Toast.makeText(this, "Đang xác thực với server...", Toast.LENGTH_SHORT).show();
        GoogleLoginRequest request = new GoogleLoginRequest(idToken);
        authService.googleLogin(request).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JwtResponse jwtResponse = response.body();
                    onLoginSuccess(jwtResponse);
                } else {
                    String errorMsg = "Xác thực tài khoản Google thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JwtResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onLoginSuccess(JwtResponse jwtResponse) {
        // Save token and basics
        boolean hasDoctorRole = false;
        if (jwtResponse.getRoles() != null) {
            for (String r : jwtResponse.getRoles()) {
                if ("ROLE_DOCTOR".equalsIgnoreCase(r)) {
                    hasDoctorRole = true;
                    break;
                }
            }
        }

        String role = hasDoctorRole ? SessionManager.ROLE_DOCTOR : SessionManager.ROLE_PATIENT;
        
        // Log in to SessionManager
        sessionManager.login(
                jwtResponse.getToken(),
                jwtResponse.getUserId(),
                jwtResponse.getUsername(),
                jwtResponse.getEmail(),
                role
        );

        // Fetch detailed profile to save detailed fields in SessionManager
        final boolean finalHasDoctorRole = hasDoctorRole;
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
                }
                
                // Proceed to navigate
                navigateToMain(finalHasDoctorRole);
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                // Proceed to navigate even if detail fetch fails
                navigateToMain(finalHasDoctorRole);
            }
        });
    }

    private void navigateToMain(boolean hasDoctorRole) {
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        
        Intent intent;
        if (hasDoctorRole) {
            intent = new Intent(LoginActivity.this, DoctorMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, PatientMainActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
