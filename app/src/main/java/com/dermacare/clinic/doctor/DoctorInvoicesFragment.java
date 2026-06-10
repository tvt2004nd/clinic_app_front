package com.dermacare.clinic.doctor;

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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DoctorInvoicesFragment extends Fragment {

    private RecyclerView rv;
    private View layoutEmpty, layoutLoading;
    private InvoiceAdapter adapter;
    private final List<InvoiceItem> items = new ArrayList<>();

    public static DoctorInvoicesFragment newInstance() {
        return new DoctorInvoicesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_invoices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rvInvoices);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutLoading = view.findViewById(R.id.layoutLoading);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InvoiceAdapter(items, item -> {
            Intent intent = new Intent(requireContext(), InvoiceActivity.class);
            intent.putExtra("recordId", item.recordId);
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        fetchInvoices();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchInvoices();
    }

    private void fetchInvoices() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);

        ApiClient.getInvoiceService(requireContext()).getDoctorInvoices()
                .enqueue(new retrofit2.Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<JsonObject>> call,
                                           retrofit2.Response<List<JsonObject>> response) {
                        if (!isAdded()) return;
                        layoutLoading.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<InvoiceItem>>() {}.getType();
                            List<InvoiceItem> data = gson.fromJson(gson.toJsonTree(response.body()), listType);
                            items.clear();
                            items.addAll(data);
                            adapter.notifyDataSetChanged();

                            if (items.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                            } else {
                                rv.setVisibility(View.VISIBLE);
                            }
                        } else {
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<JsonObject>> call, Throwable t) {
                        if (!isAdded()) return;
                        layoutLoading.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class InvoiceItem {
        public Long invoiceId;
        public String invoiceCode;
        public Long recordId;
        public Long patientId;
        public String patientName;
        public Double consultationFee;
        public Double medicationFee;
        public Double totalAmount;
        public String paymentStatus;
        public String recordDiagnosis;
        public String createdAt;
    }

    static class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.Holder> {
        public interface OnItemClick {
            void onClick(InvoiceItem item);
        }

        private final List<InvoiceItem> items;
        private final OnItemClick listener;
        private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        InvoiceAdapter(List<InvoiceItem> items, OnItemClick listener) {
            this.items = items;
            this.listener = listener;
            currencyFmt.setMinimumFractionDigits(0);
            currencyFmt.setMaximumFractionDigits(0);
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invoice, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            InvoiceItem item = items.get(position);
            holder.tvPatientName.setText(item.patientName != null ? item.patientName : "Bệnh nhân");
            holder.tvInvoiceCode.setText(item.invoiceCode != null ? item.invoiceCode : "");

            String diag = item.recordDiagnosis != null && !item.recordDiagnosis.isEmpty()
                    ? item.recordDiagnosis : "Khám bệnh";
            holder.tvDiagnosis.setText(diag);

            double total = item.totalAmount != null ? item.totalAmount : 0;
            holder.tvTotalAmount.setText(currencyFmt.format(total) + "₫");

            if ("PAID".equals(item.paymentStatus)) {
                holder.tvPaymentStatus.setText("Đã thanh toán");
                holder.tvPaymentStatus.setTextColor(0xFF10B981);
                holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_chip_done);
            } else {
                holder.tvPaymentStatus.setText("Chưa thanh toán");
                holder.tvPaymentStatus.setTextColor(0xFFD97706);
                holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_badge_amber);
            }

            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView tvPatientName, tvInvoiceCode, tvDiagnosis, tvTotalAmount, tvPaymentStatus;

            Holder(@NonNull View itemView) {
                super(itemView);
                tvPatientName = itemView.findViewById(R.id.tvPatientName);
                tvInvoiceCode = itemView.findViewById(R.id.tvInvoiceCode);
                tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
                tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
                tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            }
        }
    }
}
