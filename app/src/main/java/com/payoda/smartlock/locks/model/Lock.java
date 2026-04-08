package com.payoda.smartlock.locks.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.storage.lock.DataTypeConverter;

import java.io.Serializable;
import java.util.ArrayList;

@Entity
public class Lock implements Serializable {
    @NonNull
    @PrimaryKey
    @SerializedName("id")
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("name")
    private String name;
    @SerializedName("uuid")
    private String uuid;
    @SerializedName("ssid")
    private String ssid;
    @SerializedName("serial_number")
    private String serialNumber;
    @SerializedName("scratch_code")
    private String scratchCode;
    @SerializedName("status")
    private String status;

    @SerializedName("battery")
    private String battery;

    @ColumnInfo(name = "unAssignedKeys")
    @TypeConverters(DataTypeConverter.class)
    @SerializedName("lock_owner_id")
    private ArrayList<LockKeys> unAssignedKeys;
    @ColumnInfo(name = "owner_ids")
    @TypeConverters(DataTypeConverter.class)
    @SerializedName("lock_ids")
    private ArrayList<LockKeys> lockIds;
    @ColumnInfo(name = "lock_keys")
    @TypeConverters(DataTypeConverter.class)
    @SerializedName("lock_keys")
    private ArrayList<LockKeys> lockKeys;
    private boolean isOffline;
    @SerializedName("lock_version")
    private String lockVersion;
    @SerializedName("user_privileges")
    private String privilege;
    @ColumnInfo(name = "rfid_keys")
    @TypeConverters(DataTypeConverter.class)
    @SerializedName("rfid")
    private ArrayList<LockKeys> rfids;
    @SerializedName("is_secured")
    private String isEncrypted = "0";
    private boolean sync=true;
    @ColumnInfo(name = "enable_fp")
    @SerializedName("enable_fp")
    private String enableFp;
    @ColumnInfo(name = "enable_pin")
    @SerializedName("enable_pin")
    private String enablePin;

    @ColumnInfo(name = "enable_passage")
    @SerializedName("enable_passage")
    private String enablePassage;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getScratchCode() {
        return scratchCode;
    }

    public void setScratchCode(String scratchCode) {
        this.scratchCode = scratchCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public ArrayList<LockKeys> getUnAssignedKeys() {
        return unAssignedKeys;
    }

    public void setUnAssignedKeys(ArrayList<LockKeys> unAssignedKeys) {
        this.unAssignedKeys = unAssignedKeys;
    }

    public ArrayList<LockKeys> getLockIds() {
        return lockIds;
    }

    public void setLockIds(ArrayList<LockKeys> lockIds) {
        this.lockIds = lockIds;
    }

    public ArrayList<LockKeys> getLockKeys() {
        return lockKeys;
    }

    public void setLockKeys(ArrayList<LockKeys> lockKeys) {
        this.lockKeys = lockKeys;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public String getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(String lockVersion) {
        this.lockVersion = lockVersion;
    }

    public ArrayList<LockKeys> getRfids() {
        return rfids;
    }

    public void setRfids(ArrayList<LockKeys> rfids) {
        this.rfids = rfids;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }


    public String getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(String isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public boolean isEncrypted(){
        if(isEncrypted != null && isEncrypted.equalsIgnoreCase("1"))
            return true;
        return false;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public String getEnableFp() {
        return enableFp;
    }

    public String getEnablePin() {
        return enablePin;
    }

    public void setEnableFp(String enableFp) {
        this.enableFp = enableFp;
    }

    public void setEnablePin(String enablePin) {
        this.enablePin = enablePin;
    }

    public String getEnablePassage() {
        return enablePassage;
    }

    public void setEnablePassage(String enablePassage) {
        this.enablePassage = enablePassage;
    }

    @Override
    public String toString() {
        return "Lock = -------------> {" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", ssid='" + ssid + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", scratchCode='" + scratchCode + '\'' +
                ", status='" + status + '\'' +
                ", battery='" + battery + '\'' +
                ", unAssignedKeys=" + unAssignedKeys +
                ", lockIds=" + lockIds +
                ", lockKeys=" + lockKeys +
                ", isOffline=" + isOffline +
                ", lockVersion='" + lockVersion + '\'' +
                ", privilege='" + privilege + '\'' +
                ", rfids=" + rfids +
                ", isEncrypted='" + isEncrypted + '\'' +
                ", sync=" + sync +
                ", enableFp='" + enableFp + '\'' +
                ", enablePin='" + enablePin + '\'' +
                ", enablePassage='" + enablePassage + '\'' +
               "} ------------------------- ";
    }

}
