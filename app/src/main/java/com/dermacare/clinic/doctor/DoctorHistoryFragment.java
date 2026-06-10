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
import com.dermacare.clinic.patient.RecordDetailActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DoctorHistoryFragment extends Fragment {

    private RecyclerView rv;
    private View layoutEmpty, layoutLoading;
    private HistoryAdapter adapter;
    private final List<HistoryItem> items = new ArrayList<>();

    public static DoctorHistoryFragment newInstance() {
        return new DoctorHistoryFragment();
    }

    public static DoctorHistoryFragment newInstance(Long patientId) {
        DoctorHistoryFragment fragment = new DoctorHistoryFragment();
        Bundle args = new Bundle();
        args.putLong("patientId", patientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rvHistory);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutLoading = view.findViewById(R.id.layoutLoading);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        boolean isSimpleMode = getArguments() != null && getArguments().getLong("patientId", -1) != -1;
        adapter = new HistoryAdapter(items, isSimpleMode, item -> {
            if (item.recordId == null) return;
            Intent intent = new Intent(requireContext(), RecordDetailActivity.class);
            intent.putExtra("recordId", item.recordId.longValue());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        fetchHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchHistory();
    }

    private void fetchHistory() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);

        ApiClient.getExaminationService(requireContext()).getDoctorHistory()
                .enqueue(new retrofit2.Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<JsonObject>> call,
                                           retrofit2.Response<List<JsonObject>> response) {
                        if (!isAdded()) return;
                        layoutLoading.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<HistoryItem>>() {}.getType();
                            List<HistoryItem> data = gson.fromJson(gson.toJsonTree(response.body()), listType);
                            items.clear();
                            Long targetPatientId = getArguments() != null ? getArguments().getLong("patientId", -1) : -1;
                            if (targetPatientId != -1) {
                                for (HistoryItem item : data) {
                                    if (item.patientId != null && item.patientId.equals(targetPatientId)) {
                                        items.add(item);
                                    }
                                }
                            } else {
                                items.addAll(data);
                            }
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

    public static class HistoryItem {
        public Long recordId;
        public String recordCode;
        public Long patientId;
        public String patientName;
        public String patientPhone;
        public String diagnosis;
        public String diseaseName;
        public String examinedAt;
        public Integer prescriptionCount;
        public Boolean hasInvoice;
        public String invoiceStatus;
    }

    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {
        public interface OnItemClick {
            void onClick(HistoryItem item);
        }

        private final List<HistoryItem> items;
        private final boolean isSimpleMode;
        private final OnItemClick listener;

        public HistoryAdapter(List<HistoryItem> items, boolean isSimpleMode, OnItemClick listener) {
            this.items = items;
            this.isSimpleMode = isSimpleMode;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_doctor_history, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            HistoryItem item = items.get(position);
            String name = item.patientName != null ? item.patientName : "Bệnh nhân";
            holder.tvPatientName.setText(name);

            String initials = name.length() > 0 ? name.substring(0, 1).toUpperCase() : "?";
            holder.tvAvatarText.setText(initials);
            
            if (isSimpleMode) {
                holder.tvPatientName.setVisibility(View.GONE);
                if (holder.cardAvatar != null) {
                    holder.cardAvatar.setVisibility(View.GONE);
                }
            } else {
                holder.tvPatientName.setVisibility(View.VISIBLE);
                if (holder.cardAvatar != null) {
                    holder.cardAvatar.setVisibility(View.VISIBLE);
                }
            }

            String diag = item.diagnosis != null && !item.diagnosis.isEmpty()
                    ? item.diagnosis : (item.diseaseName != null ? item.diseaseName : "Khám bệnh");
            holder.tvDiagnosis.setText(diag);

            String date = item.examinedAt != null && item.examinedAt.length() >= 10
                    ? item.examinedAt.substring(0, 10) : "--";
            holder.tvExaminedDate.setText("Đã khám: " + date);

            if (Boolean.TRUE.equals(item.hasInvoice)) {
                if ("PAID".equals(item.invoiceStatus)) {
                    holder.tvInvoiceStatus.setText("Đã TT");
                    holder.tvInvoiceStatus.setTextColor(0xFF10B981);
                    holder.tvInvoiceStatus.setBackgroundResource(R.drawable.bg_chip_done);
                } else {
                    holder.tvInvoiceStatus.setText("Chưa TT");
                    holder.tvInvoiceStatus.setTextColor(0xFFD97706);
                    holder.tvInvoiceStatus.setBackgroundResource(R.drawable.bg_badge_amber);
                }
            } else {
                holder.tvInvoiceStatus.setText("Chưa có HĐ");
                holder.tvInvoiceStatus.setTextColor(0xFF94A3B8);
                holder.tvInvoiceStatus.setBackgroundResource(R.drawable.bg_chip_rating);
            }

            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView tvPatientName, tvDiagnosis, tvExaminedDate, tvInvoiceStatus, tvAvatarText;
            final View cardAvatar;

            Holder(@NonNull View itemView) {
                super(itemView);
                tvPatientName = itemView.findViewById(R.id.tvPatientName);
                tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
                tvExaminedDate = itemView.findViewById(R.id.tvExaminedDate);
                tvInvoiceStatus = itemView.findViewById(R.id.tvInvoiceStatus);
                tvAvatarText = itemView.findViewById(R.id.tvAvatarText);
                cardAvatar = itemView.findViewById(R.id.cardAvatar);
            }
        }
    }
}
