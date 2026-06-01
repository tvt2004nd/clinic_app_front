package com.dermacare.clinic.data.api;

import com.dermacare.clinic.model.MedicalRecord;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MedicalRecordService {
    @POST("api/medical-records")
    Call<Void> saveMedicalRecord(@Body MedicalRecord record);
}
