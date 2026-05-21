package com.dermacare.clinic.data.api;
 
import com.dermacare.clinic.data.api.model.ForgotPasswordRequest;
import com.dermacare.clinic.data.api.model.GoogleLoginRequest;
import com.dermacare.clinic.data.api.model.JwtResponse;
import com.dermacare.clinic.data.api.model.LoginRequest;
import com.dermacare.clinic.data.api.model.RegisterRequest;
import com.dermacare.clinic.data.api.model.ResetPasswordRequest;
 
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
 
public interface AuthService {
 
    @POST("api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest request);
 
    @POST("api/auth/register")
    Call<ResponseBody> register(@Body RegisterRequest request);
 
    @POST("api/auth/google")
    Call<JwtResponse> googleLogin(@Body GoogleLoginRequest request);
 
    @POST("api/auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest request);
 
    @POST("api/auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest request);
}
