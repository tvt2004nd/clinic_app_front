package com.dermacare.clinic.data.api;

import com.dermacare.clinic.data.api.model.DoctorResponse;
import com.dermacare.clinic.data.api.model.ScheduleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PublicService {
    @GET("/api/public/doctors")
    Call<List<DoctorResponse>> getDoctors();

    @GET("/api/public/schedules/{doctorId}")
    Call<List<ScheduleResponse>> getSchedules(@Path("doctorId") Long doctorId);
}
