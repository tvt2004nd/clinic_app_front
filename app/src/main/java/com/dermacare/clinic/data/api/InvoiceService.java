package com.dermacare.clinic.data.api;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface InvoiceService {

    @POST("api/invoices")
    Call<JsonObject> createInvoice(@Body JsonObject request);

    @GET("api/invoices/doctor")
    Call<List<JsonObject>> getDoctorInvoices();

    @GET("api/invoices/my")
    Call<List<JsonObject>> getMyInvoices();

    @GET("api/invoices/record/{recordId}")
    Call<JsonObject> getInvoiceByRecord(@Path("recordId") Long recordId);

    @POST("api/invoices/{invoiceId}/create-payment-intent")
    Call<JsonObject> createPaymentIntent(@Path("invoiceId") Long invoiceId, @Body JsonObject body);

    @PUT("api/invoices/{invoiceId}/pay")
    Call<JsonObject> payInvoice(@Path("invoiceId") Long invoiceId, @Body JsonObject request);
}
