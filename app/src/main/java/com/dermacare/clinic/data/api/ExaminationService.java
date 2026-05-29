package com.dermacare.clinic.data.api;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ExaminationService {

    @GET("api/examinations/appointments")
    Call<List<JsonObject>> getAppointmentQueue(@Query("date") String date);

    @POST("api/examinations/intake")
    Call<JsonObject> intakePatient(@Body JsonObject request);

    @GET("api/examinations/records/{recordId}")
    Call<JsonObject> getMedicalRecord(@Path("recordId") Long recordId);

    @PUT("api/examinations/records/{recordId}/symptoms")
    Call<JsonObject> updateSymptoms(@Path("recordId") Long recordId, @Body JsonObject request);

    @PUT("api/examinations/records/{recordId}/ai-reference")
    Call<JsonObject> updateAiReference(@Path("recordId") Long recordId, @Body JsonObject request);

    @PUT("api/examinations/records/{recordId}/final-diagnosis")
    Call<JsonObject> updateFinalDiagnosis(@Path("recordId") Long recordId, @Body JsonObject request);

    @PUT("api/examinations/records/{recordId}/prescription")
    Call<JsonObject> updatePrescription(@Path("recordId") Long recordId, @Body JsonObject request);

    @POST("api/examinations/records/{recordId}/follow-up")
    Call<JsonObject> scheduleFollowUp(@Path("recordId") Long recordId, @Body JsonObject request);

    @GET("api/examinations/references/medications")
    Call<List<JsonObject>> searchMedications(@Query("keyword") String keyword);

    @GET("api/examinations/references/diseases")
    Call<List<JsonObject>> searchDiseases(@Query("keyword") String keyword);
}
