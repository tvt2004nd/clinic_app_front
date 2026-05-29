package com.dermacare.clinic.doctor;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.dermacare.clinic.R;
import com.google.android.material.button.MaterialButton;

public class InvoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        MaterialButton btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        btnConfirmPayment.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        // Mock API call to payInvoice
        new AlertDialog.Builder(this)
                .setTitle("Thanh toán thành công")
                .setMessage("Hóa đơn đã được thanh toán và lưu vào hệ thống.")
                .setPositiveButton("Hoàn tất", (dialog, which) -> {
                    Toast.makeText(this, "Đã in hóa đơn", Toast.LENGTH_SHORT).show();
                    finish(); // Trở về màn hình chính của bác sĩ
                })
                .setCancelable(false)
                .show();
    }
}
