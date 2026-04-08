package com.payoda.smartlock.users.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 10/12/18.
 */

public class Schedule {
    @SerializedName("is_schedule_access")
    private int is_schedule_access;
    @SerializedName("schedule_date_from")
    private String schedule_date_from;
    @SerializedName("schedule_date_to")
    private String schedule_date_to;
    @SerializedName("schedule_time_from")
    private String schedule_time_from;
    @SerializedName("schedule_time_to")
    private String schedule_time_to;

    public int getIs_schedule_access() {
        return is_schedule_access;
    }

    public void setIs_schedule_access(int is_schedule_access) {
        this.is_schedule_access = is_schedule_access;
    }

    public String getSchedule_date_from() {
        return schedule_date_from;
    }

    public void setSchedule_date_from(String schedule_date_from) {
        this.schedule_date_from = schedule_date_from;
    }

    public String getSchedule_date_to() {
        return schedule_date_to;
    }

    public void setSchedule_date_to(String schedule_date_to) {
        this.schedule_date_to = schedule_date_to;
    }

    public String getSchedule_time_from() {
        return schedule_time_from;
    }

    public void setSchedule_time_from(String schedule_time_from) {
        this.schedule_time_from = schedule_time_from;
    }

    public String getSchedule_time_to() {
        return schedule_time_to;
    }

    public void setSchedule_time_to(String schedule_time_to) {
        this.schedule_time_to = schedule_time_to;
    }
}
