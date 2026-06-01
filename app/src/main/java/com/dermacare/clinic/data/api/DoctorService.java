package com.dermacare.clinic.data.api;

import com.dermacare.clinic.data.api.model.DoctorPatientResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DoctorService {

    @GET("api/doctors/my-patients")
    Call<List<DoctorPatientResponse>> getMyPatients();
}
