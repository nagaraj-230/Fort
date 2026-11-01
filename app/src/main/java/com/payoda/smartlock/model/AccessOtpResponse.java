package com.payoda.smartlock.model;

import com.google.gson.annotations.SerializedName;

public class AccessOtpResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("error-code")
    private int errorCode;

    @SerializedName("response")
    private AccessOtpData response;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public AccessOtpData getResponse() {
        return response;
    }

    public void setResponse(AccessOtpData response) {
        this.response = response;
    }

    public static class AccessOtpData {
        @SerializedName("lock-id")
        private int lockId;

        @SerializedName("lock-serial-no")
        private String lockSerialNo;

        @SerializedName("otp")
        private String otp;

        public int getLockId() {
            return lockId;
        }

        public void setLockId(int lockId) {
            this.lockId = lockId;
        }

        public String getLockSerialNo() {
            return lockSerialNo;
        }

        public void setLockSerialNo(String lockSerialNo) {
            this.lockSerialNo = lockSerialNo;
        }

        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }
    }
}

