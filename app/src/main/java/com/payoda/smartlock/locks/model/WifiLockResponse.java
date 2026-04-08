package com.payoda.smartlock.locks.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WifiLockResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("error-code")
    private Integer errorCode;

    @SerializedName("error-message")
    private String errorMessage;

    @SerializedName("ssid")
    private String ssid;

    @SerializedName("response")
    private Response response;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public class Response {

        @SerializedName("Battery-Level")
        private String battery;
        @SerializedName("slot-key")
        private String slotKey;
        @SerializedName("fp-id")
        private String fpId;
        @SerializedName("mac-addr")
        @Expose
        private String macAddr;
        @SerializedName("owner-id-0")
        @Expose
        private String ownerId0;
        @SerializedName("owner-id-1")
        @Expose
        private String ownerId1;
        @SerializedName("slot-key-0")
        @Expose
        private String slotKey0;
        @SerializedName("slot-key-1")
        @Expose
        private String slotKey1;
        @SerializedName("slot-key-2")
        @Expose
        private String slotKey2;
        @SerializedName("slot-key-3")
        @Expose
        private String slotKey3;
        @SerializedName("slot-key-4")
        @Expose
        private String slotKey4;
        @SerializedName("slot-key-5")
        @Expose
        private String slotKey5;
        @SerializedName("slot-key-6")
        @Expose
        private String slotKey6;
        @SerializedName("slot-key-7")
        @Expose
        private String slotKey7;
        @SerializedName("slot-key-8")
        @Expose
        private String slotKey8;
        @SerializedName("slot-key-9")
        @Expose
        private String slotKey9;
        @SerializedName("slot-key-10")
        @Expose
        private String slotKey10;
        @SerializedName("slot-key-11")
        @Expose
        private String slotKey11;
        @SerializedName("slot-key-12")
        @Expose
        private String slotKey12;
        @SerializedName("slot-key-13")
        @Expose
        private String slotKey13;
        @SerializedName("slot-key-14")
        @Expose
        private String slotKey14;
        @SerializedName("slot-key-15")
        @Expose
        private String slotKey15;
        @SerializedName("slot-key-16")
        @Expose
        private String slotKey16;
        @SerializedName("slot-key-17")
        @Expose
        private String slotKey17;
        @SerializedName("slot-key-18")
        @Expose
        private String slotKey18;
        @SerializedName("slot-key-19")
        @Expose
        private String slotKey19;
        @SerializedName("slot-key-20")
        @Expose
        private String slotKey20;
        @SerializedName("slot-key-21")
        @Expose
        private String slotKey21;
        @SerializedName("slot-key-22")
        @Expose
        private String slotKey22;
        @SerializedName("slot-key-23")
        @Expose
        private String slotKey23;
        @SerializedName("slot-key-24")
        @Expose
        private String slotKey24;
        @SerializedName("rf-id-0")
        private String rfid0;
        @SerializedName("rf-id-1")
        private String rfid1;
        @SerializedName("rf-id-2")
        private String rfid2;
        @SerializedName("HW-Version")
        private String hardwareVersion="";
        @SerializedName("SW-Feature")
        private String softwareFeature;
        @SerializedName("rf-id")
        private String rfid;

        public String getBattery() {
            return battery;
        }

        public void setBattery(String battery) {
            this.battery = battery;
        }

        public String getSlotKey() {
            return slotKey;
        }

        public void setSlotKey(String slotKey) {
            this.slotKey = slotKey;
        }

        public String getMacAddr() {
            return macAddr;
        }

        public void setMacAddr(String macAddr) {
            this.macAddr = macAddr;
        }

        public String getOwnerId0() {
            return ownerId0;
        }

        public void setOwnerId0(String ownerId0) {
            this.ownerId0 = ownerId0;
        }

        public String getOwnerId1() {
            return ownerId1;
        }

        public void setOwnerId1(String ownerId1) {
            this.ownerId1 = ownerId1;
        }

        public String getSlotKey0() {
            return slotKey0;
        }

        public void setSlotKey0(String slotKey0) {
            this.slotKey0 = slotKey0;
        }

        public String getSlotKey1() {
            return slotKey1;
        }

        public void setSlotKey1(String slotKey1) {
            this.slotKey1 = slotKey1;
        }

        public String getSlotKey2() {
            return slotKey2;
        }

        public void setSlotKey2(String slotKey2) {
            this.slotKey2 = slotKey2;
        }

        public String getSlotKey3() {
            return slotKey3;
        }

        public void setSlotKey3(String slotKey3) {
            this.slotKey3 = slotKey3;
        }

        public String getSlotKey4() {
            return slotKey4;
        }

        public void setSlotKey4(String slotKey4) {
            this.slotKey4 = slotKey4;
        }

        public String getSlotKey5() {
            return slotKey5;
        }

        public void setSlotKey5(String slotKey5) {
            this.slotKey5 = slotKey5;
        }

        public String getSlotKey6() {
            return slotKey6;
        }

        public void setSlotKey6(String slotKey6) {
            this.slotKey6 = slotKey6;
        }

        public String getSlotKey7() {
            return slotKey7;
        }

        public void setSlotKey7(String slotKey7) {
            this.slotKey7 = slotKey7;
        }

        public String getSlotKey8() {
            return slotKey8;
        }

        public void setSlotKey8(String slotKey8) {
            this.slotKey8 = slotKey8;
        }

        public String getSlotKey9() {
            return slotKey9;
        }

        public void setSlotKey9(String slotKey9) {
            this.slotKey9 = slotKey9;
        }

        public String getSlotKey10() {
            return slotKey10;
        }

        public void setSlotKey10(String slotKey10) {
            this.slotKey10 = slotKey10;
        }

        public String getSlotKey11() {
            return slotKey11;
        }

        public void setSlotKey11(String slotKey11) {
            this.slotKey11 = slotKey11;
        }

        public String getSlotKey12() {
            return slotKey12;
        }

        public void setSlotKey12(String slotKey12) {
            this.slotKey12 = slotKey12;
        }

        public String getSlotKey13() {
            return slotKey13;
        }

        public void setSlotKey13(String slotKey13) {
            this.slotKey13 = slotKey13;
        }

        public String getSlotKey14() {
            return slotKey14;
        }

        public void setSlotKey14(String slotKey14) {
            this.slotKey14 = slotKey14;
        }

        public String getSlotKey15() {
            return slotKey15;
        }

        public void setSlotKey15(String slotKey15) {
            this.slotKey15 = slotKey15;
        }

        public String getSlotKey16() {
            return slotKey16;
        }

        public void setSlotKey16(String slotKey16) {
            this.slotKey16 = slotKey16;
        }

        public String getSlotKey17() {
            return slotKey17;
        }

        public void setSlotKey17(String slotKey17) {
            this.slotKey17 = slotKey17;
        }

        public String getSlotKey18() {
            return slotKey18;
        }

        public void setSlotKey18(String slotKey18) {
            this.slotKey18 = slotKey18;
        }

        public String getSlotKey19() {
            return slotKey19;
        }

        public void setSlotKey19(String slotKey19) {
            this.slotKey19 = slotKey19;
        }

        public String getSlotKey20() {
            return slotKey20;
        }

        public void setSlotKey20(String slotKey20) {
            this.slotKey20 = slotKey20;
        }

        public String getSlotKey21() {
            return slotKey21;
        }

        public void setSlotKey21(String slotKey21) {
            this.slotKey21 = slotKey21;
        }

        public String getSlotKey22() {
            return slotKey22;
        }

        public void setSlotKey22(String slotKey22) {
            this.slotKey22 = slotKey22;
        }

        public String getSlotKey23() {
            return slotKey23;
        }

        public void setSlotKey23(String slotKey23) {
            this.slotKey23 = slotKey23;
        }

        public String getSlotKey24() {
            return slotKey24;
        }

        public void setSlotKey24(String slotKey24) {
            this.slotKey24 = slotKey24;
        }

        public String getFpId() {
            return fpId;
        }

        public void setFpId(String fpId) {
            this.fpId = fpId;
        }

        public String getRfid0() {
            return rfid0;
        }

        public void setRfid0(String rfid0) {
            this.rfid0 = rfid0;
        }

        public String getRfid1() {
            return rfid1;
        }

        public void setRfid1(String rfid1) {
            this.rfid1 = rfid1;
        }

        public String getRfid2() {
            return rfid2;
        }

        public void setRfid2(String rfid2) {
            this.rfid2 = rfid2;
        }

        public String getHardwareVersion() {
            return hardwareVersion;
        }

        public void setHardwareVersion(String hardwareVersion) {
            this.hardwareVersion = hardwareVersion;
        }

        public String getSoftwareFeature() {
            return softwareFeature;
        }

        public void setSoftwareFeature(String softwareFeature) {
            this.softwareFeature = softwareFeature;
        }

        public String getRfid() {
            return rfid;
        }

        public void setRfid(String rfid) {
            this.rfid = rfid;
        }


    }
}
