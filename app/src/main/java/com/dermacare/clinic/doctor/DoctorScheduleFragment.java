package com.dermacare.clinic.doctor;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.AppointmentResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorScheduleFragment extends Fragment {
    private DoctorDashboardAppointmentAdapter adapter;
    private View layoutEmpty;
    private RecyclerView rv;
    private LocalDate selectedDate;

    public static DoctorScheduleFragment newInstance() {
        return new DoctorScheduleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_schedule, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSchedule();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedDate = LocalDate.now();

        TextView tvDate = view.findViewById(R.id.tvCurrentDate);
        tvDate.setText(formatDateHeader(selectedDate));

        rv = view.findViewById(R.id.recyclerView);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new DoctorDashboardAppointmentAdapter(new ArrayList<>(),
                new DoctorDashboardAppointmentAdapter.ActionListener() {
                    @Override
                    public void onConfirm(AppointmentResponse appt) {
                        confirmAppointment(appt);
                    }

                    @Override
                    public void onExamine(AppointmentResponse appt) {
                        android.content.Intent intent = new android.content.Intent(requireContext(), ExamineActivity.class);
                        intent.putExtra("appointmentId", appt.appointmentId);
                        startActivity(intent);
                    }

                    @Override
                    public void onViewRecord(AppointmentResponse appt) {
                        if (appt.recordId != null) {
                            android.content.Intent intent = new android.content.Intent(requireContext(), com.dermacare.clinic.patient.RecordDetailActivity.class);
                            intent.putExtra("recordId", appt.recordId.longValue());
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(), "Không tìm thấy hồ sơ bệnh án", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        rv.setAdapter(adapter);

        buildCalendarStrip(view);
        loadSchedule();
    }

    private String formatDateHeader(LocalDate date) {
        String[] months = {"", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        String[] days = {"Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"};
        String dayName = days[date.getDayOfWeek().getValue() % 7];
        return dayName + ", " + date.getDayOfMonth() + " " + months[date.getMonthValue()];
    }

    private String getDayOfWeekVi(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        switch (dow) {
            case MONDAY:    return "T2";
            case TUESDAY:   return "T3";
            case WEDNESDAY: return "T4";
            case THURSDAY:  return "T5";
            case FRIDAY:    return "T6";
            case SATURDAY:  return "T7";
            case SUNDAY:    return "CN";
            default: return "";
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void buildCalendarStrip(View root) {
        LinearLayout strip = root.findViewById(R.id.calendarStrip);
        strip.removeAllViews();

        LocalDate today = LocalDate.now();
        LocalDate monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            boolean isToday = day.equals(today);
            boolean isSelected = day.equals(selectedDate);

            LinearLayout dayLayout = new LinearLayout(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dpToPx(3), 0, dpToPx(3), 0);
            dayLayout.setLayoutParams(lp);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            int circleSize = dpToPx(40);

            // Day number circle with gradient background
            TextView tvDayNum = new TextView(requireContext());
            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(circleSize, circleSize);
            dayParams.topMargin = dpToPx(6);
            tvDayNum.setLayoutParams(dayParams);
            tvDayNum.setGravity(Gravity.CENTER);
            tvDayNum.setText(String.valueOf(day.getDayOfMonth()));
            tvDayNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            if (isSelected) {
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{0xFF0D9488, 0xFF2DD4BF});
                gd.setCornerRadius(circleSize / 2f);
                tvDayNum.setBackground(gd);
                tvDayNum.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                tvDayNum.setTypeface(null, android.graphics.Typeface.BOLD);
                tvDayNum.setElevation(dpToPx(3));
            } else if (isToday) {
                GradientDrawable gd = new GradientDrawable();
                gd.setShape(GradientDrawable.OVAL);
                gd.setColor(0x1A0D9488);
                gd.setStroke(dpToPx(1), 0xFF0D9488);
                tvDayNum.setBackground(gd);
                tvDayNum.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                tvDayNum.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvDayNum.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                tvDayNum.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            // Day of week label
            TextView tvDow = new TextView(requireContext());
            LinearLayout.LayoutParams dowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvDow.setLayoutParams(dowParams);
            tvDow.setText(getDayOfWeekVi(day));
            tvDow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

            if (isSelected) {
                tvDow.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                tvDow.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (isToday) {
                tvDow.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                tvDow.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvDow.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                tvDow.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            if (!isSelected && !isToday) {
                tvDow.setPadding(0, dpToPx(2), 0, 0);
            }

            // Today dot indicator
            TextView tvDot = new TextView(requireContext());
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(
                    dpToPx(4), dpToPx(4));
            dotParams.topMargin = dpToPx(4);
            tvDot.setLayoutParams(dotParams);
            tvDot.setGravity(Gravity.CENTER);

            if (isToday && !isSelected) {
                tvDot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_primary));
            } else if (isSelected) {
                tvDot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_primary));
                tvDot.getBackground().setAlpha(0);
            }

            dayLayout.addView(tvDow);
            dayLayout.addView(tvDayNum);
            dayLayout.addView(tvDot);

            final LocalDate clickedDay = day;
            dayLayout.setOnClickListener(v -> {
                selectedDate = clickedDay;
                TextView tvDate = root.findViewById(R.id.tvCurrentDate);
                tvDate.setText(formatDateHeader(selectedDate));
                buildCalendarStrip(root);
                loadSchedule();
            });

            strip.addView(dayLayout);
        }
    }

    private void loadSchedule() {
        ApiClient.getAppointmentService(requireContext())
                .getDoctorAppointments(null, selectedDate != null ? selectedDate.toString() : null)
                .enqueue(new Callback<List<AppointmentResponse>>() {
                    @Override
                    public void onResponse(Call<List<AppointmentResponse>> call,
                                           Response<List<AppointmentResponse>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<AppointmentResponse> list = new ArrayList<>();
                            for (AppointmentResponse a : response.body()) {
                                if ("PENDING".equals(a.status)
                                        || "CONFIRMED".equals(a.status)) {
                                    list.add(a);
                                }
                            }
                            if (list.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rv.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rv.setVisibility(View.VISIBLE);
                                adapter.setData(list);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                        if (!isAdded()) return;
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                });
    }

    private void confirmAppointment(AppointmentResponse appt) {
        ApiClient.getAppointmentService(requireContext())
                .confirmAppointment(appt.appointmentId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            loadSchedule();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                                Toast.makeText(requireContext(), "Xác nhận thất bại: " + errorBody, Toast.LENGTH_SHORT).show();
                            } catch (Exception ignored) {
                                Toast.makeText(requireContext(), "Xác nhận thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
