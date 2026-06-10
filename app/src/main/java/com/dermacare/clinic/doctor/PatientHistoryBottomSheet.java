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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.patient.RecordDetailActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PatientHistoryBottomSheet extends BottomSheetDialogFragment {

    private Long patientId;
    private String patientName;
    
    private RecyclerView rvRecentHistory;
    private View pbLoading, tvEmptyHistory, btnViewAllHistory;
    private TextView tvPatientNameTitle;
    
    private DoctorHistoryFragment.HistoryAdapter adapter;
    private final List<DoctorHistoryFragment.HistoryItem> items = new ArrayList<>();

    public static PatientHistoryBottomSheet newInstance(Long patientId, String patientName) {
        PatientHistoryBottomSheet fragment = new PatientHistoryBottomSheet();
        Bundle args = new Bundle();
        args.putLong("patientId", patientId);
        args.putString("patientName", patientName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patientId = getArguments().getLong("patientId", -1);
            patientName = getArguments().getString("patientName", "Bệnh nhân");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_patient_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tvPatientNameTitle = view.findViewById(R.id.tvPatientNameTitle);
        rvRecentHistory = view.findViewById(R.id.rvRecentHistory);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
        btnViewAllHistory = view.findViewById(R.id.btnViewAllHistory);

        tvPatientNameTitle.setText("Lịch sử khám - " + patientName);

        rvRecentHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DoctorHistoryFragment.HistoryAdapter(items, true, item -> {
            if (item.recordId == null) return;
            Intent intent = new Intent(requireContext(), RecordDetailActivity.class);
            intent.putExtra("recordId", item.recordId.longValue());
            startActivity(intent);
        });
        rvRecentHistory.setAdapter(adapter);

        btnViewAllHistory.setOnClickListener(v -> {
            dismiss();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_doctor, DoctorHistoryFragment.newInstance(patientId))
                    .addToBackStack(null)
                    .commit();
        });

        fetchHistory();
    }

    private void fetchHistory() {
        pbLoading.setVisibility(View.VISIBLE);
        rvRecentHistory.setVisibility(View.GONE);
        tvEmptyHistory.setVisibility(View.GONE);
        btnViewAllHistory.setVisibility(View.GONE);

        ApiClient.getExaminationService(requireContext()).getDoctorHistory()
                .enqueue(new retrofit2.Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<JsonObject>> call,
                                           retrofit2.Response<List<JsonObject>> response) {
                        if (!isAdded()) return;
                        pbLoading.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<DoctorHistoryFragment.HistoryItem>>() {}.getType();
                            List<DoctorHistoryFragment.HistoryItem> data = gson.fromJson(gson.toJsonTree(response.body()), listType);
                            
                            items.clear();
                            for (DoctorHistoryFragment.HistoryItem item : data) {
                                if (item.patientId != null && item.patientId.equals(patientId)) {
                                    items.add(item);
                                }
                            }
                            
                            if (items.isEmpty()) {
                                tvEmptyHistory.setVisibility(View.VISIBLE);
                            } else {
                                boolean hasMore = items.size() > 3;
                                List<DoctorHistoryFragment.HistoryItem> previewItems = new ArrayList<>();
                                for (int i = 0; i < Math.min(3, items.size()); i++) {
                                    previewItems.add(items.get(i));
                                }
                                
                                items.clear();
                                items.addAll(previewItems);
                                adapter.notifyDataSetChanged();
                                rvRecentHistory.setVisibility(View.VISIBLE);
                                
                                if (hasMore) {
                                    btnViewAllHistory.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            tvEmptyHistory.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<JsonObject>> call, Throwable t) {
                        if (!isAdded()) return;
                        pbLoading.setVisibility(View.GONE);
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
