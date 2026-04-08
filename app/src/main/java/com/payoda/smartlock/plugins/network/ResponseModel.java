package com.payoda.smartlock.plugins.network;

import com.google.gson.annotations.SerializedName;

/**
 * This class contains POJO model for ResponseModel
 * Created by david.
 */
public class ResponseModel {
    @SerializedName("status")
    private String status;
    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "status='" + status + '\'' +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
