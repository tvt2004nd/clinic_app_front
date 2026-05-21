package com.dermacare.clinic.data.api;
 
import com.dermacare.clinic.data.api.model.ChangePasswordRequest;
import com.dermacare.clinic.data.api.model.ProfileUpdateRequest;
import com.dermacare.clinic.data.api.model.UserProfileResponse;
 
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
 
public interface UserService {
 
    @GET("api/users/profile")
    Call<UserProfileResponse> getProfile();
 
    @PUT("api/users/profile")
    Call<UserProfileResponse> updateProfile(@Body ProfileUpdateRequest request);
 
    @PUT("api/users/change-password")
    Call<ResponseBody> changePassword(@Body ChangePasswordRequest request);
 
    @Multipart
    @POST("api/users/upload-avatar")
    Call<ResponseBody> uploadAvatar(@Part MultipartBody.Part file);
}
