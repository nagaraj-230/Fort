package com.payoda.smartlock.fp.model;

import com.google.gson.annotations.SerializedName;

public class ManageFpPrivilege {
    @SerializedName("enable_fp")
    private String enableFp;
    public String getEnableFp() {
        return enableFp;
    }
    public void setEnableFp(String enableFp) {
        this.enableFp = enableFp;
    }
}
