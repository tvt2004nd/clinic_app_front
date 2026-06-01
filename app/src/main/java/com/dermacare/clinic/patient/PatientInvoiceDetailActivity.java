package com.dermacare.clinic.patient;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientInvoiceDetailActivity extends AppCompatActivity {

    private TextView tvInvoiceCode, tvDoctorName, tvDiagnosis, tvFee, tvStatus, tvTotalAmount, tvDoctorInitials;
    private MaterialButton btnPay;
    private MaterialCardView statusChip;
    private Long invoiceId;
    private long totalAmount;
    private PaymentSheet paymentSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_invoice_detail);

        invoiceId = getIntent().getLongExtra("invoiceId", -1);
        String invoiceCode = getIntent().getStringExtra("invoiceCode");
        String doctorName = getIntent().getStringExtra("doctorName");
        String diagnosis = getIntent().getStringExtra("diagnosis");
        totalAmount = getIntent().getLongExtra("totalAmount", 0);
        String paymentStatus = getIntent().getStringExtra("paymentStatus");

        tvInvoiceCode = findViewById(R.id.tvInvoiceCode);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDiagnosis = findViewById(R.id.tvDiagnosis);
        tvFee = findViewById(R.id.tvFee);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDoctorInitials = findViewById(R.id.tvDoctorInitials);
        statusChip = findViewById(R.id.statusChip);
        btnPay = findViewById(R.id.btnPay);

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        tvInvoiceCode.setText("Mã hóa đơn: " + (invoiceCode != null ? invoiceCode : ""));
        tvDoctorName.setText(doctorName != null ? doctorName : "—");
        tvDiagnosis.setText(diagnosis != null ? diagnosis : "—");

        String amountText = fmt.format(totalAmount) + "₫";
        tvFee.setText(amountText);
        tvTotalAmount.setText(amountText);

        if (doctorName != null && !doctorName.isEmpty()) {
            String[] parts = doctorName.trim().split("\\s+");
            String initials;
            if (parts.length >= 2) {
                initials = String.valueOf(parts[parts.length - 2].charAt(0))
                        + parts[parts.length - 1].charAt(0);
            } else {
                initials = doctorName.substring(0, Math.min(2, doctorName.length()));
            }
            tvDoctorInitials.setText(initials.toUpperCase());
        }

        boolean isPaid = "PAID".equals(paymentStatus);
        tvStatus.setText(isPaid ? "Đã thanh toán" : "Chưa thanh toán");
        tvStatus.setTextColor(ContextCompat.getColor(this, isPaid ? R.color.success : R.color.white));
        statusChip.setCardBackgroundColor(isPaid
                ? ContextCompat.getColor(this, R.color.primary_container)
                : 0x9922C55E);
        btnPay.setVisibility(isPaid ? android.view.View.GONE : android.view.View.VISIBLE);
        tvFee.setTextColor(ContextCompat.getColor(this, isPaid ? R.color.success : R.color.text_primary));
        tvTotalAmount.setTextColor(ContextCompat.getColor(this, isPaid ? R.color.success : R.color.primary));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        PaymentConfiguration.init(this, "pk_test_51Ru7HNCa8ca5YKr0uLj7IQjo1NsLHYew794dryKl8ugh7UMYSo4HbS6FXBSjAkretbViAfxmxcbkpuvUFErdmpDZ009Ch0BKe6");
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        btnPay.setOnClickListener(v -> startPayment());
    }

    private void startPayment() {
        if (invoiceId == -1) return;
        btnPay.setEnabled(false);
        btnPay.setText("Đang xử lý...");

        ApiClient.getInvoiceService(this).createPaymentIntent(invoiceId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String clientSecret = response.body().get("clientSecret").getAsString();
                    PaymentSheet.Configuration config = new PaymentSheet.Configuration("DermaCare");
                    paymentSheet.presentWithPaymentIntent(clientSecret, config);
                } else {
                    btnPay.setEnabled(true);
                    btnPay.setText("Thanh toán ngay");
                    Toast.makeText(PatientInvoiceDetailActivity.this, "Không thể tạo thanh toán", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnPay.setEnabled(true);
                btnPay.setText("Thanh toán ngay");
                Toast.makeText(PatientInvoiceDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            confirmPayment();
        } else if (result instanceof PaymentSheetResult.Canceled) {
            btnPay.setEnabled(true);
            btnPay.setText("Thanh toán ngay");
            Toast.makeText(this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            btnPay.setEnabled(true);
            btnPay.setText("Thanh toán ngay");
            String error = ((PaymentSheetResult.Failed) result).getError().getLocalizedMessage();
            Toast.makeText(this, "Thanh toán thất bại: " + error, Toast.LENGTH_LONG).show();
        }
    }

    private void confirmPayment() {
        JsonObject body = new JsonObject();
        body.addProperty("amount", totalAmount);
        body.addProperty("paymentMethod", "CREDIT_CARD");

        ApiClient.getInvoiceService(this).payInvoice(invoiceId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PatientInvoiceDetailActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    btnPay.setVisibility(android.view.View.GONE);
                    tvStatus.setText("Đã thanh toán");
                    tvStatus.setTextColor(ContextCompat.getColor(PatientInvoiceDetailActivity.this, R.color.success));
                    statusChip.setCardBackgroundColor(ContextCompat.getColor(PatientInvoiceDetailActivity.this, R.color.primary_container));
                    tvFee.setTextColor(ContextCompat.getColor(PatientInvoiceDetailActivity.this, R.color.success));
                } else {
                    btnPay.setEnabled(true);
                    btnPay.setText("Thanh toán ngay");
                    Toast.makeText(PatientInvoiceDetailActivity.this, "Xác nhận thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnPay.setEnabled(true);
                btnPay.setText("Thanh toán ngay");
                Toast.makeText(PatientInvoiceDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
