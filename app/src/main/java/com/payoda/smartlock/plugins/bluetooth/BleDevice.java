package com.payoda.smartlock.plugins.bluetooth;

import android.os.ParcelUuid;

public class BleDevice {

    private int id;
    private String name;
    private String address;
    private int bondState;
    private int type;
    private ParcelUuid[] uuids;
    private String serviceId;
    private String characteristicsId;
    private String data;
    private boolean status;

    public BleDevice(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public BleDevice(String name, String address, int bondState) {
        this.name = name;
        this.address = address;
        this.bondState = bondState;
    }

    public BleDevice(String name, String address, int bondState, int type, ParcelUuid[] uuids) {
        this.name = name;
        this.address = address;
        this.bondState = bondState;
        this.type = type;
        this.uuids = uuids;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getBondState() {
        return bondState;
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ParcelUuid[] getUuids() {
        return uuids;
    }

    public void setUuids(ParcelUuid[] uuids) {
        this.uuids = uuids;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCharacteristicsId() {
        return characteristicsId;
    }

    public void setCharacteristicsId(String characteristicsId) {
        this.characteristicsId = characteristicsId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
