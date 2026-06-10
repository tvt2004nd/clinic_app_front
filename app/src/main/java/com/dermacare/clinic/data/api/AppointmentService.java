package com.dermacare.clinic.data.api;

import com.dermacare.clinic.data.api.model.AppointmentRequest;
import com.dermacare.clinic.data.api.model.AppointmentResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AppointmentService {

        @POST("/api/appointments")
        Call<Map<String, Object>> bookAppointment(@Body AppointmentRequest request);

        @GET("/api/appointments/my")
        Call<List<AppointmentResponse>> getMyAppointments();

        @GET("/api/appointments/doctor")
        Call<List<AppointmentResponse>> getDoctorAppointments(
                        @Query("status") String status,
                        @Query("date") String date);

        @PUT("/api/appointments/{id}/confirm")
        Call<Map<String, Object>> confirmAppointment(@Path("id") Long id);

        @PUT("/api/appointments/{id}/cancel")
        Call<Map<String, Object>> cancelAppointment(@Path("id") Long id, @Body Map<String, String> body);

        @PUT("/api/appointments/{id}/complete")
        Call<Map<String, Object>> completeAppointment(@Path("id") Long id);

        @PUT("/api/appointments/{id}/reschedule")
        Call<Map<String, Object>> rescheduleAppointment(@Path("id") Long id, @Body Map<String, Object> body);

        @GET("/api/chat/conversation")
        Call<Map<String, Object>> getConversation(
                        @Query("doctorId") Long doctorId,
                        @Query("patientId") Long patientId);
}
