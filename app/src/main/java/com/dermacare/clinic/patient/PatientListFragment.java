package com.dermacare.clinic.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.PatientRecordsAdapter;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.PatientRecordSummaryResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientListFragment extends Fragment {
    public static final String TYPE_APPOINTMENTS = "appointments";
    public static final String TYPE_RECORDS = "records";
    private static final String ARG_TITLE = "title";
    private static final String ARG_TYPE = "type";

    private View layoutLoading;
    private View layoutEmpty;
    private View layoutError;
    private View btnRetry;
    private RecyclerView rvRecords;
    private PatientRecordsAdapter adapter;
    private final List<PatientRecordSummaryResponse> records = new ArrayList<>();

    public static PatientListFragment newInstance(String title, String type) {
        PatientListFragment f = new PatientListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_TYPE, type);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String type = args != null ? args.getString(ARG_TYPE, TYPE_APPOINTMENTS) : TYPE_APPOINTMENTS;

        if (TYPE_RECORDS.equals(type)) {
            return inflater.inflate(R.layout.fragment_patient_records, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_patient_appointments, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String type = args != null ? args.getString(ARG_TYPE, TYPE_APPOINTMENTS) : TYPE_APPOINTMENTS;

        if (TYPE_RECORDS.equals(type)) {
            setupRecordsView(view);
            fetchRecords();
        }
    }

    private void setupRecordsView(View view) {
        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutError = view.findViewById(R.id.layoutError);
        btnRetry = view.findViewById(R.id.btnRetry);
        rvRecords = view.findViewById(R.id.rvRecords);

        rvRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PatientRecordsAdapter(records, recordId -> {
            Intent intent = new Intent(requireContext(), RecordDetailActivity.class);
            intent.putExtra("recordId", recordId);
            startActivity(intent);
        });
        rvRecords.setAdapter(adapter);

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                showLoading();
                fetchRecords();
            });
        }
    }

    private void fetchRecords() {
        ApiClient.getExaminationService(requireContext()).getMyRecords()
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<PatientRecordSummaryResponse>>() {}.getType();
                            List<PatientRecordSummaryResponse> fetched = gson.fromJson(
                                    gson.toJsonTree(response.body()), listType);
                            records.clear();
                            records.addAll(fetched);
                            adapter.notifyDataSetChanged();

                            if (records.isEmpty()) {
                                showEmpty();
                            } else {
                                showContent();
                            }
                        } else {
                            showError();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        if (!isAdded()) return;
                        showError();
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (rvRecords != null) rvRecords.setVisibility(View.GONE);
    }

    private void showContent() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (rvRecords != null) rvRecords.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (rvRecords != null) rvRecords.setVisibility(View.GONE);
    }

    private void showError() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.VISIBLE);
        if (rvRecords != null) rvRecords.setVisibility(View.GONE);
    }
}
