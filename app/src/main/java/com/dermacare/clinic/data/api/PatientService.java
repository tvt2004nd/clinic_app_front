package com.dermacare.clinic.data.api;

import com.dermacare.clinic.data.api.model.HealthProfileRequest;
import com.dermacare.clinic.data.api.model.HealthProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface PatientService {

    @GET("api/patients/health-profile")
    Call<HealthProfileResponse> getHealthProfile();

    @PUT("api/patients/health-profile")
    Call<HealthProfileResponse> updateHealthProfile(@Body HealthProfileRequest request);
}
