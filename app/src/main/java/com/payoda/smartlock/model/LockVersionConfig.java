package com.payoda.smartlock.model;

import com.google.gson.annotations.SerializedName;

public class LockVersionConfig {
    @SerializedName("message")
    private String message;
    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private LockVersionData lockVersionData;

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

    public LockVersionData getLockVersionData() {
        return lockVersionData;
    }

    public void setLockVersionData(LockVersionData lockVersionData) {
        this.lockVersionData = lockVersionData;
    }

    public class LockVersionData {

        @SerializedName("v1.0")
        private ConfigData versionOne;
        @SerializedName("v2.0")
        private ConfigData versionTwo;
        @SerializedName("v2.1")
        private ConfigData versionTwoOne;
        @SerializedName("v3.0")
        private ConfigData versionThree;
        @SerializedName("v3.1")
        private ConfigData versionThreeOne;
        @SerializedName("v3.2")
        private ConfigData versionThreeTwo;
        @SerializedName("v4.0")
        private ConfigData versionFour;
        @SerializedName("v6.0")
        private ConfigData versionSix;

        public ConfigData getVersionOne() {
            return versionOne;
        }

        public void setVersionOne(ConfigData versionOne) {
            this.versionOne = versionOne;
        }

        public ConfigData getVersionTwo() {
            return versionTwo;
        }

        public void setVersionTwo(ConfigData versionTwo) {
            this.versionTwo = versionTwo;
        }

        public ConfigData getVersionTwoOne() {
            return versionTwoOne;
        }

        public void setVersionTwoOne(ConfigData versionTwoOne) {
            this.versionTwoOne = versionTwoOne;
        }

        public ConfigData getVersionThree() {
            return versionThree;
        }

        public void setVersionThree(ConfigData versionThree) {
            this.versionThree = versionThree;
        }

        public ConfigData getVersionThreeOne() {
            return versionThreeOne;
        }

        public void setVersionThreeOne(ConfigData versionThreeOne) {
            this.versionThreeOne = versionThreeOne;
        }

        public ConfigData getVersionThreeTwo() {
            return versionThreeTwo;
        }

        public void setVersionThreeTwo(ConfigData versionThreeTwo) {
            this.versionThreeTwo = versionThreeTwo;
        }

        public ConfigData getVersionFour() {
            return versionFour;
        }

        public void setVersionFour(ConfigData versionFour) {
            this.versionFour = versionFour;
        }

        public ConfigData getVersionSix() {
            return versionSix;
        }

        public void setVersionSix(ConfigData versionSix) {
            this.versionSix = versionSix;
        }
    }

    public class ConfigData {
        @SerializedName("wifi-time")
        private String wifiTime;
        @SerializedName("ble-time")
        private String bleTime;

        public String getWifiTime() {
            return wifiTime;
        }

        public void setWifiTime(String wifiTime) {
            this.wifiTime = wifiTime;
        }

        public String getBleTime() {
            return bleTime;
        }

        public void setBleTime(String bleTime) {
            this.bleTime = bleTime;
        }
    }
}
