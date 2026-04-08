package com.payoda.smartlock.locks.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

@Entity
public class LockKeys implements Serializable {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "key_id")
    @SerializedName("id")
    private String id;
    @SerializedName("lock_id")
    private String lockId;
    @ColumnInfo(name = "key_user_id")
    @SerializedName("user_id")
    private String userId;
    @SerializedName("user_type")
    private String userType;
    @ColumnInfo(name = "key_name")
    @SerializedName("name")
    private String name;
    @SerializedName("key")
    private String key;
    @SerializedName("slot_number")
    private String slotNumber;
    @ColumnInfo(name = "key_status")
    @SerializedName("status")
    private String status;
    @Ignore
    @SerializedName("userDetails")
    private LockUser lockUser;
    @Ignore
    @SerializedName("requestDetails")
    private RequestDetail requestDetail;
    @SerializedName("is_schedule_access")
    private String is_schedule_access;
    @SerializedName("schedule_date_from")
    private String schedule_date_from;
    @SerializedName("schedule_date_to")
    private String schedule_date_to;
    @SerializedName("schedule_time_from")
    private String schedule_time_from;
    @SerializedName("schedule_time_to")
    private String schedule_time_to;
    @SerializedName("assigned_datetime")
    @Ignore
    private String assigned_datetime;
    @SerializedName("registration_id")
    private String guestId;
    @Ignore
    @SerializedName("registrationDetails")
    private LockUser registrationDetails;
    @Ignore
    private ArrayList<LockKeys> fpUsers;
    @Ignore
    private ArrayList<String> originalFPIds;

    public String getIs_schedule_access() {
        return is_schedule_access;
    }

    public void setIs_schedule_access(String is_schedule_access) {
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

    private String buttonText;
    public LockKeys() {
    }
    @Ignore
    public LockKeys(String name,String key, String slotNumber) {
        this.key = key;
        this.slotNumber = slotNumber;
        this.name=name;
    }
    @Ignore
    public LockKeys(String key, String slotNumber) {
        this.key = key;
        this.slotNumber = slotNumber;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LockUser getLockUser() {
        return lockUser;
    }

    public void setLockUser(LockUser lockUser) {
        this.lockUser = lockUser;
    }

    public RequestDetail getRequestDetail() {
        return requestDetail;
    }

    public void setRequestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public LockUser getRegistrationDetails() {
        return registrationDetails;
    }

    public void setRegistrationDetails(LockUser registrationDetails) {
        this.registrationDetails = registrationDetails;
    }

    public ArrayList<LockKeys> getFpUsers() {
        return fpUsers;
    }

    public void setFpUsers(ArrayList<LockKeys> fpUsers) {
        this.fpUsers = fpUsers;
    }

    public ArrayList<String> getOriginalFPIds() {
        return originalFPIds;
    }

    public void setOriginalFPIds(ArrayList<String> originalFPIds) {
        this.originalFPIds = originalFPIds;
    }

    public String getAssigned_datetime() {
        return assigned_datetime;
    }

    public void setAssigned_datetime(String assigned_datetime) {
        this.assigned_datetime = assigned_datetime;
    }

    @Override
    public String toString() {
        return "LockKeys{" +
                "id='" + id + '\'' +
                ", lockId='" + lockId + '\'' +
                ", userId='" + userId + '\'' +
                ", userType='" + userType + '\'' +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", slotNumber='" + slotNumber + '\'' +
                ", status='" + status + '\'' +
                ", lockUser=" + lockUser +
                ", requestDetail=" + requestDetail +
                ", is_schedule_access='" + is_schedule_access + '\'' +
                ", schedule_date_from='" + schedule_date_from + '\'' +
                ", schedule_date_to='" + schedule_date_to + '\'' +
                ", schedule_time_from='" + schedule_time_from + '\'' +
                ", schedule_time_to='" + schedule_time_to + '\'' +
                ", assigned_datetime='" + assigned_datetime + '\'' +
                ", guestId='" + guestId + '\'' +
                ", registrationDetails=" + registrationDetails +
                ", fpUsers=" + fpUsers +
                ", originalFPIds=" + originalFPIds +
                ", buttonText='" + buttonText + '\'' +
                '}';
    }
}
