package com.payoda.smartlock.managepins.model;

import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.locks.model.LockKeyList;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.storage.lock.DataTypeConverter;
import com.payoda.smartlock.users.model.User;

import java.util.ArrayList;

public class OtpResponse {

    @SerializedName("message")
    private String message;
    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private OtpData otpData;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OtpData getOtpData() {
        return otpData;
    }

    public void setOtpData(OtpData otpData) {
        this.otpData = otpData;
    }


    public static class OtpData {

        @TypeConverters(DataTypeConverter.class)
        @SerializedName("keys")
        private ArrayList<LockKeys> lockKeyList;
        @SerializedName("get_otp")
        private boolean getOtp;

        public ArrayList<LockKeys> getLockKeyList() {
            return lockKeyList;
        }

        public void setLockKeyList(ArrayList<LockKeys> lockKeyList) {
            this.lockKeyList = lockKeyList;
        }

        public boolean isGetOtp() {
            return getOtp;
        }

        public void setGetOtp(boolean getOtp) {
            this.getOtp = getOtp;
        }

    }

}