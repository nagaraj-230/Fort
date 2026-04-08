package com.payoda.smartlock.splash.model;

import com.google.gson.annotations.SerializedName;

public class BrandInfoResponse {

    @SerializedName("message")
    private String message;
    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private BrandInfo brandInfo;

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

    public BrandInfo getBrandInfo() {
        return brandInfo;
    }

    public void setBrandInfo(BrandInfo brandInfo) {
        this.brandInfo = brandInfo;
    }

    public class BrandInfo {

        @SerializedName("company_name")
        private String companyName;
        @SerializedName("company_logo")
        private String companyLogo;
        @SerializedName("api_service_url")
        private String apiServiceUrl;
        @SerializedName("terms_condition_url")
        private String termsConditionUrl;
        @SerializedName("privacy_url")
        private String privacyUrl;
        @SerializedName("scratch_code_length")
        private int scratchCodeLength;
        @SerializedName("scratch_code_prefix")
        private String scratchCodePrefix;
        @SerializedName("manufacturer_code")
        private String manufacturerCode;
        @SerializedName("mqtt_service_url")
        private String mqttServiceUrl;

        public BrandInfo(){}

        public BrandInfo(String companyName, String companyLogo, String apiServiceUrl, String termsConditionUrl, String privacyUrl, int scratchCodeLength, String scratchCodePrefix, String manufacturerCode, String mqttServiceUrl) {
            this.companyName = companyName;
            this.companyLogo = companyLogo;
            this.apiServiceUrl = apiServiceUrl;
            this.termsConditionUrl = termsConditionUrl;
            this.privacyUrl = privacyUrl;
            this.scratchCodeLength = scratchCodeLength;
            this.scratchCodePrefix = scratchCodePrefix;
            this.manufacturerCode = manufacturerCode;
            this.mqttServiceUrl = mqttServiceUrl;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getCompanyLogo() {
            return companyLogo;
        }

        public void setCompanyLogo(String companyLogo) {
            this.companyLogo = companyLogo;
        }

        public String getApiServiceUrl() {
            return apiServiceUrl;
        }

        public void setApiServiceUrl(String apiServiceUrl) {
            this.apiServiceUrl = apiServiceUrl;
        }

        public String getTermsConditionUrl() {
            return termsConditionUrl;
        }

        public void setTermsConditionUrl(String termsConditionUrl) {
            this.termsConditionUrl = termsConditionUrl;
        }

        public String getPrivacyUrl() {
            return privacyUrl;
        }

        public void setPrivacyUrl(String privacyUrl) {
            this.privacyUrl = privacyUrl;
        }

        public int getScratchCodeLength() {
            return scratchCodeLength;
        }

        public void setScratchCodeLength(int scratchCodeLength) {
            this.scratchCodeLength = scratchCodeLength;
        }

        public String getScratchCodePrefix() {
            return scratchCodePrefix;
        }

        public void setScratchCodePrefix(String scratchCodePrefix) {
            this.scratchCodePrefix = scratchCodePrefix;
        }

        public String getManufacturerCode() {
            return manufacturerCode;
        }

        public void setManufacturerCode(String manufacturerCode) {
            this.manufacturerCode = manufacturerCode;
        }

        public String getMqttServiceUrl() {
            return mqttServiceUrl;
        }

        public void setMqttServiceUrl(String mqttServiceUrl) {
            this.mqttServiceUrl = mqttServiceUrl;
        }

        @Override
        public String toString() {
            return "BrandInfo{" +
                    "companyName='" + companyName + '\'' +
                    ", companyLogo='" + companyLogo + '\'' +
                    ", apiServiceUrl='" + apiServiceUrl + '\'' +
                    ", termsConditionUrl='" + termsConditionUrl + '\'' +
                    ", privacyUrl='" + privacyUrl + '\'' +
                    ", scratchCodeLength=" + scratchCodeLength +
                    ", scratchCodePrefix='" + scratchCodePrefix + '\'' +
                    ", manufacturerCode='" + manufacturerCode + '\'' +
                    ", mqttServiceUrl='" + mqttServiceUrl + '\'' +
                    '}';
        }
    }


    @Override
    public String toString() {
        return "BrandInfoResponse{" +
                "message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", brandInfo=" + brandInfo +
                '}';
    }

}

