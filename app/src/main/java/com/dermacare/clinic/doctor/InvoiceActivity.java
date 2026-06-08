package com.dermacare.clinic.doctor;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.google.gson.JsonObject;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceActivity extends AppCompatActivity {

    private TextView tvInvoiceCode, tvPatientName;
    private TextView tvConsultationFee, tvMedicationFee, tvOtherFee, tvDiscount, tvTotalAmount;
    private TextView tvPaymentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        tvInvoiceCode = findViewById(R.id.tvInvoiceCode);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvConsultationFee = findViewById(R.id.tvConsultationFee);
        tvMedicationFee = findViewById(R.id.tvMedicationFee);
        tvOtherFee = findViewById(R.id.tvOtherFee);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);

        Long recordId = getIntent().getLongExtra("recordId", -1L);
        if (recordId != -1L) {
            loadInvoice(recordId);
        }

        findViewById(R.id.btnConfirmPayment).setOnClickListener(v ->
                Toast.makeText(this, "Bệnh nhân sẽ thanh toán qua ứng dụng", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadInvoice(Long recordId) {
        ApiClient.getInvoiceService(this).getInvoiceByRecord(recordId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

                    tvInvoiceCode.setText("Mã HD: " + getStr(data, "invoiceCode"));
                    tvPatientName.setText("Bệnh nhân: " + getStr(data, "patientName"));

                    tvConsultationFee.setText(fmt.format(getLong(data, "consultationFee")) + " ₫");
                    tvMedicationFee.setText(fmt.format(getLong(data, "medicationFee")) + " ₫");
                    tvOtherFee.setText(fmt.format(getLong(data, "otherFee")) + " ₫");
                    tvDiscount.setText(fmt.format(getLong(data, "discount")) + " ₫");
                    tvTotalAmount.setText(fmt.format(getLong(data, "totalAmount")) + " ₫");

                    String status = getStr(data, "paymentStatus");
                    if ("PAID".equals(status)) {
                        tvPaymentStatus.setText("Đã thanh toán");
                        tvPaymentStatus.setTextColor(0xFF10B981);
                    } else {
                        tvPaymentStatus.setText("Chưa thanh toán");
                        tvPaymentStatus.setTextColor(0xFFEF4444);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(InvoiceActivity.this, "Không tải được hóa đơn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private long getLong(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsLong() : 0;
    }
}
