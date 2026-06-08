package com.dermacare.clinic.data.api.model;

public class ScheduleResponse {
    public Long scheduleId;
    public String date;
    public String startTime;
    public String endTime;
    public String status;
    public int bookedCount;
    public int maxPatients;
    public boolean isFull;
}
