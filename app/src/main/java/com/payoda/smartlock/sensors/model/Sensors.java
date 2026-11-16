package com.payoda.smartlock.sensors.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Sensors {

    @SerializedName("status")
    private String status;

    @SerializedName(value = "data", alternate = {"sensors"})
    private ArrayList<Sensor> sensorList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<Sensor> getSensorList() {
        return sensorList != null ? sensorList : new ArrayList<>();
    }

    public void setSensorList(ArrayList<Sensor> sensorList) {
        this.sensorList = sensorList;
    }
}
