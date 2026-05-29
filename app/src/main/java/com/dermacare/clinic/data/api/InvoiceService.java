package com.dermacare.clinic.data.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface InvoiceService {

    @POST("api/invoices")
    Call<JsonObject> createInvoice(@Body JsonObject request);

    @GET("api/invoices/record/{recordId}")
    Call<JsonObject> getInvoiceByRecord(@Path("recordId") Long recordId);

    @PUT("api/invoices/{invoiceId}/pay")
    Call<JsonObject> payInvoice(@Path("invoiceId") Long invoiceId, @Body JsonObject request);
}
