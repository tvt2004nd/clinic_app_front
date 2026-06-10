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
import com.dermacare.clinic.adapter.PatientAppointmentAdapter;
import com.dermacare.clinic.adapter.PatientRecordsAdapter;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.AppointmentResponse;
import com.dermacare.clinic.data.api.model.HealthProfileRequest;
import com.dermacare.clinic.data.api.model.HealthProfileResponse;
import com.dermacare.clinic.data.api.model.PatientRecordSummaryResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import android.util.Log;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientListFragment extends Fragment {
    public static final String TYPE_APPOINTMENTS = "appointments";
    public static final String TYPE_RECORDS = "records";
    private static final String ARG_TITLE = "title";
    private static final String ARG_TYPE = "type";

    // Common views
    private View layoutLoading;
    private View layoutError;
    private View btnRetry;

    // Appointments screen views
    private View scrollContent;
    private RecyclerView rvUpcoming;
    private RecyclerView rvHistory;
    private View layoutUpcomingEmpty;
    private View layoutHistoryEmpty;
    private TextView tvUpcomingCount;

    private final List<AppointmentResponse> upcomingList = new ArrayList<>();
    private PatientAppointmentAdapter upcomingAdapter;

    // Records used in BOTH the appointments history section AND the records tab
    private PatientRecordsAdapter historyRecordsAdapter;
    private final List<PatientRecordSummaryResponse> historyRecords = new ArrayList<>();
    private final List<PatientRecordSummaryResponse> records = new ArrayList<>();

    // Health Profile screen views
    private TextInputEditText etBloodType, etMedicalHistory, etInsuranceNumber, etEmergencyContact, etEmergencyPhone;
    private View btnSave;
    private HealthProfileResponse currentProfile;

    // Track pending loads for appointments screen
    private boolean appointmentsLoaded = false;
    private boolean recordsLoaded = false;

    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private Long currentUserId;
    private String token;

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
        } else {
            setupAppointmentsView(view);
            showLoading();
            fetchAppointmentsAndRecords();
            // Initialize STOMP subscription to receive appointment updates
            com.dermacare.clinic.util.SessionManager session = new com.dermacare.clinic.util.SessionManager(
                    requireContext());
            currentUserId = session.getUserId();
            token = session.getToken();
            initStompClient();
        }
    }

    private void initStompClient() {
        if (currentUserId == null || currentUserId == -1L)
            return;

        final String WEBSOCKET_URL = "ws://10.0.2.2:8080/ws-raw";

        List<ua.naiksoftware.stomp.dto.StompHeader> headers = new java.util.ArrayList<>();
        if (token != null && !token.isBlank()) {
            headers.add(new ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer " + token));
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WEBSOCKET_URL);
        compositeDisposable = new CompositeDisposable();

        Disposable dispLifecycle = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d("PatientListFragment", "Stomp opened");
                            break;
                        case ERROR:
                            Log.e("PatientListFragment", "Stomp error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d("PatientListFragment", "Stomp closed");
                            break;
                    }
                });
        compositeDisposable.add(dispLifecycle);

        Disposable dispTopic = stompClient.topic("/topic/appointments/patient-" + currentUserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    try {
                        JSONObject json = new JSONObject(topicMessage.getPayload());
                        if (json.has("appointmentId")) {
                            long apptId = json.getLong("appointmentId");
                            String status = json.has("status") ? json.getString("status") : null;
                            Long scheduleId = json.has("scheduleId") ? json.optLong("scheduleId") : null;
                            Long roomId = json.has("roomId") ? json.optLong("roomId") : null;
                            // Update local list if present
                            boolean changed = false;
                            for (int i = 0; i < upcomingList.size(); i++) {
                                com.dermacare.clinic.data.api.model.AppointmentResponse a = upcomingList.get(i);
                                if (a.appointmentId != null && a.appointmentId.longValue() == apptId) {
                                    if (status != null)
                                        a.status = status;
                                    if (scheduleId != null && scheduleId != 0)
                                        a.scheduleId = scheduleId;
                                    if (roomId != null && roomId != 0)
                                        a.roomId = roomId;
                                    changed = true;
                                }
                            }
                            if (changed && getActivity() != null) {
                                upcomingAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("PatientListFragment", "Failed to parse stomp message", e);
                    }
                }, throwable -> Log.e("PatientListFragment", "Stomp topic error", throwable));

        compositeDisposable.add(dispTopic);

        stompClient.connect(headers);
    }

    @Override
    public void onDestroyView() {
        if (stompClient != null)
            stompClient.disconnect();
        if (compositeDisposable != null)
            compositeDisposable.dispose();
        super.onDestroyView();
    }

    // ===================== HEALTH PROFILE VIEW =====================

    private void setupRecordsView(View view) {
        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutError = view.findViewById(R.id.layoutError);
        btnRetry = view.findViewById(R.id.btnRetry);
        scrollContent = view.findViewById(R.id.scrollContent); // The nested scroll view

        etBloodType = view.findViewById(R.id.etBloodType);
        etMedicalHistory = view.findViewById(R.id.etMedicalHistory);
        etInsuranceNumber = view.findViewById(R.id.etInsuranceNumber);
        etEmergencyContact = view.findViewById(R.id.etEmergencyContact);
        etEmergencyPhone = view.findViewById(R.id.etEmergencyPhone);
        btnSave = view.findViewById(R.id.btnSave);

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                showRecordsLoading();
                fetchRecords();
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveHealthProfile());
        }
    }

    private void fetchRecords() {
        showRecordsLoading();
        ApiClient.getPatientService(requireContext()).getHealthProfile()
                .enqueue(new Callback<HealthProfileResponse>() {
                    @Override
                    public void onResponse(Call<HealthProfileResponse> call, Response<HealthProfileResponse> response) {
                        if (!isAdded())
                            return;
                        if (response.isSuccessful() && response.body() != null) {
                            currentProfile = response.body();
                            populateProfileForm();
                            showRecordsContent();
                        } else {
                            showRecordsError();
                        }
                    }

                    @Override
                    public void onFailure(Call<HealthProfileResponse> call, Throwable t) {
                        if (!isAdded())
                            return;
                        showRecordsError();
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateProfileForm() {
        if (currentProfile == null)
            return;
        if (etBloodType != null)
            etBloodType.setText(currentProfile.bloodType != null ? currentProfile.bloodType : "");
        if (etMedicalHistory != null)
            etMedicalHistory.setText(currentProfile.medicalHistory != null ? currentProfile.medicalHistory : "");
        if (etInsuranceNumber != null)
            etInsuranceNumber.setText(currentProfile.insuranceNumber != null ? currentProfile.insuranceNumber : "");
        if (etEmergencyContact != null)
            etEmergencyContact.setText(currentProfile.emergencyContact != null ? currentProfile.emergencyContact : "");
        if (etEmergencyPhone != null)
            etEmergencyPhone.setText(currentProfile.emergencyPhone != null ? currentProfile.emergencyPhone : "");
    }

    private void saveHealthProfile() {
        if (currentProfile == null)
            return;

        HealthProfileRequest request = new HealthProfileRequest(
                etBloodType.getText().toString().trim(),
                etMedicalHistory.getText().toString().trim(),
                etInsuranceNumber.getText().toString().trim(),
                etEmergencyContact.getText().toString().trim(),
                etEmergencyPhone.getText().toString().trim(),
                currentProfile.allergies // Keep allergies as is for MVP
        );

        showRecordsLoading();
        ApiClient.getPatientService(requireContext()).updateHealthProfile(request)
                .enqueue(new Callback<HealthProfileResponse>() {
                    @Override
                    public void onResponse(Call<HealthProfileResponse> call, Response<HealthProfileResponse> response) {
                        if (!isAdded())
                            return;
                        if (response.isSuccessful() && response.body() != null) {
                            currentProfile = response.body();
                            populateProfileForm();
                            showRecordsContent();
                            Toast.makeText(requireContext(), "Đã lưu thông tin sức khỏe thành công", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            showRecordsContent();
                            Toast.makeText(requireContext(), "Lỗi khi lưu thông tin: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<HealthProfileResponse> call, Throwable t) {
                        if (!isAdded())
                            return;
                        showRecordsContent();
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRecordsLoading() {
        if (layoutLoading != null)
            layoutLoading.setVisibility(View.VISIBLE);
        if (layoutError != null)
            layoutError.setVisibility(View.GONE);
        if (scrollContent != null)
            scrollContent.setVisibility(View.GONE);
    }

    private void showRecordsContent() {
        if (layoutLoading != null)
            layoutLoading.setVisibility(View.GONE);
        if (layoutError != null)
            layoutError.setVisibility(View.GONE);
        if (scrollContent != null)
            scrollContent.setVisibility(View.VISIBLE);
    }

    private void showRecordsError() {
        if (layoutLoading != null)
            layoutLoading.setVisibility(View.GONE);
        if (layoutError != null)
            layoutError.setVisibility(View.VISIBLE);
        if (scrollContent != null)
            scrollContent.setVisibility(View.GONE);
    }

    // ===================== APPOINTMENTS VIEW =====================

    private void setupAppointmentsView(View view) {
        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutError = view.findViewById(R.id.layoutError);
        btnRetry = view.findViewById(R.id.btnRetry);
        scrollContent = view.findViewById(R.id.scrollContent);

        rvUpcoming = view.findViewById(R.id.rvUpcoming);
        rvHistory = view.findViewById(R.id.rvHistory);
        layoutUpcomingEmpty = view.findViewById(R.id.layoutUpcomingEmpty);
        layoutHistoryEmpty = view.findViewById(R.id.layoutHistoryEmpty);
        tvUpcomingCount = view.findViewById(R.id.tvUpcomingCount);

        // Upcoming appointments adapter
        rvUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
        upcomingAdapter = new PatientAppointmentAdapter(upcomingList);
        rvUpcoming.setAdapter(upcomingAdapter);

        // Medical history (records) adapter for the history section
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyRecordsAdapter = new PatientRecordsAdapter(historyRecords, recordId -> {
            if (recordId == null)
                return;
            Intent intent = new Intent(requireContext(), RecordDetailActivity.class);
            intent.putExtra("recordId", recordId.longValue());
            startActivity(intent);
        });
        rvHistory.setAdapter(historyRecordsAdapter);

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                showLoading();
                fetchAppointmentsAndRecords();
            });
        }
    }

    private void fetchAppointmentsAndRecords() {
        appointmentsLoaded = false;
        recordsLoaded = false;

        // Fetch appointments
        ApiClient.getAppointmentService(requireContext()).getMyAppointments()
                .enqueue(new Callback<List<AppointmentResponse>>() {
                    @Override
                    public void onResponse(Call<List<AppointmentResponse>> call,
                            Response<List<AppointmentResponse>> response) {
                        if (!isAdded())
                            return;
                        if (response.isSuccessful() && response.body() != null) {
                            splitAppointments(response.body());
                        }
                        appointmentsLoaded = true;
                        checkBothLoaded();
                    }

                    @Override
                    public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                        if (!isAdded())
                            return;
                        appointmentsLoaded = true;
                        showError();
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Fetch records (medical history)
        ApiClient.getExaminationService(requireContext()).getMyRecords()
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        if (!isAdded())
                            return;
                        if (response.isSuccessful() && response.body() != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<PatientRecordSummaryResponse>>() {
                            }.getType();
                            List<PatientRecordSummaryResponse> fetched = gson.fromJson(
                                    gson.toJsonTree(response.body()), listType);
                            records.clear();
                            records.addAll(fetched);
                        }
                        recordsLoaded = true;
                        checkBothLoaded();
                    }

                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        if (!isAdded())
                            return;
                        recordsLoaded = true;
                        checkBothLoaded();
                    }
                });
    }

    private void splitAppointments(List<AppointmentResponse> all) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        upcomingList.clear();

        for (AppointmentResponse appt : all) {
            boolean isUpcoming = ("PENDING".equals(appt.status) || "CONFIRMED".equals(appt.status))
                    && appt.date != null && appt.date.compareTo(today) >= 0;
            if (isUpcoming) {
                upcomingList.add(appt);
            }
        }
    }

    private void checkBothLoaded() {
        if (!appointmentsLoaded || !recordsLoaded)
            return;
        if (!isAdded())
            return;

        // Update adapters
        upcomingAdapter.notifyDataSetChanged();

        // Populate history records from the fetched records list
        historyRecords.clear();
        historyRecords.addAll(records);
        historyRecordsAdapter.notifyDataSetChanged();

        // Show/hide upcoming section
        if (upcomingList.isEmpty()) {
            rvUpcoming.setVisibility(View.GONE);
            layoutUpcomingEmpty.setVisibility(View.VISIBLE);
            tvUpcomingCount.setVisibility(View.GONE);
        } else {
            rvUpcoming.setVisibility(View.VISIBLE);
            layoutUpcomingEmpty.setVisibility(View.GONE);
            tvUpcomingCount.setVisibility(View.VISIBLE);
            tvUpcomingCount.setText(String.valueOf(upcomingList.size()));
        }

        // Show/hide history section (medical records)
        if (historyRecords.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            layoutHistoryEmpty.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            layoutHistoryEmpty.setVisibility(View.GONE);
        }

        showContent();
    }

    private void showLoading() {
        if (layoutLoading != null)
            layoutLoading.setVisibility(View.VISIBLE);
        if (layoutError != null)
            layoutError.setVisibility(View.GONE);
        if (scrollContent != null)
            scrollContent.setVisibility(View.GONE);
    }

    private void showContent() {
        if (layoutLoading != null)
            layoutLoading.setVisibility(View.GONE);
        if (layoutError != null)
            layoutError.setVisibility(View.GONE);
        if (scrollContent != null)
            scrollContent.setVisibility(View.VISIBLE);
    }

    private void showError() {
        if (layoutLoading != null)
            layoutLoading.setVisibility(View.GONE);
        if (layoutError != null)
            layoutError.setVisibility(View.VISIBLE);
        if (scrollContent != null)
            scrollContent.setVisibility(View.GONE);
    }
}
