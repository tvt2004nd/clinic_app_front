package com.dermacare.clinic.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientInvoicesFragment extends Fragment {

    private RecyclerView rvInvoices;
    private InvoiceAdapter adapter;

    private TextView tvInvoiceCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_invoices, container, false);
        rvInvoices = view.findViewById(R.id.rvInvoices);
        tvInvoiceCount = view.findViewById(R.id.tvInvoiceCount);
        rvInvoices.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InvoiceAdapter();
        rvInvoices.setAdapter(adapter);
        loadInvoices();
        return view;
    }

    private void loadInvoices() {
        ApiClient.getInvoiceService(requireContext()).getMyInvoices().enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<JsonObject> data = response.body();
                    adapter.setData(data);
                    if (tvInvoiceCount != null) {
                        tvInvoiceCount.setText(data.size() + " hóa đơn");
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể tải hóa đơn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.VH> {

        private final List<JsonObject> items = new ArrayList<>();

        void setData(List<JsonObject> data) {
            items.clear();
            items.addAll(data);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_invoice, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int i) {
            JsonObject inv = items.get(i);

            h.tvCode.setText(opt(inv, "invoiceCode"));
            h.tvDoctor.setText("Bác sĩ: " + opt(inv, "doctorName"));
            h.tvDiagnosis.setText(opt(inv, "recordDiagnosis"));

            long total = inv.has("totalAmount") && !inv.get("totalAmount").isJsonNull()
                    ? inv.get("totalAmount").getAsLong() : 0;
            h.tvTotal.setText(NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(total) + "₫");

            boolean paid = "PAID".equals(opt(inv, "paymentStatus"));
            h.tvStatus.setText(paid ? "Đã thanh toán" : "Chưa thanh toán");
            h.tvStatus.setTextColor(paid
                    ? requireContext().getColor(R.color.success)
                    : requireContext().getColor(R.color.error));

            h.card.setOnClickListener(v -> {
                Intent intent = new Intent(h.itemView.getContext(), PatientInvoiceDetailActivity.class);
                intent.putExtra("invoiceId", inv.get("invoiceId").getAsLong());
                intent.putExtra("invoiceCode", opt(inv, "invoiceCode"));
                intent.putExtra("doctorName", opt(inv, "doctorName"));
                intent.putExtra("diagnosis", opt(inv, "recordDiagnosis"));
                intent.putExtra("totalAmount", total);
                intent.putExtra("paymentStatus", opt(inv, "paymentStatus"));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView tvCode, tvDoctor, tvDiagnosis, tvTotal, tvStatus;

            VH(@NonNull View v) {
                super(v);
                card = (MaterialCardView) v;
                tvCode = v.findViewById(R.id.tvInvoiceCode);
                tvDoctor = v.findViewById(R.id.tvDoctorName);
                tvDiagnosis = v.findViewById(R.id.tvDiagnosis);
                tvTotal = v.findViewById(R.id.tvTotal);
                tvStatus = v.findViewById(R.id.tvStatus);
            }
        }

        private String opt(JsonObject obj, String key) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) return obj.get(key).getAsString();
            return "";
        }
    }
}
