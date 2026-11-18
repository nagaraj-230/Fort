package com.payoda.smartlock.sensors.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.DoorSensorServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.sensors.model.Sensors;
import com.payoda.smartlock.utils.Logger;

import org.json.JSONObject;

import java.util.Locale;

public class DoorSensorService {

    private static DoorSensorService instance;

    private DoorSensorService() {
    }

    public static synchronized DoorSensorService getInstance() {
        if (instance == null) {
            instance = new DoorSensorService();
        }
        return instance;
    }

    /**
     * Fetch the list of door sensors from the backend.
     */
    public void getSensorList(int limit, int offset, final ResponseHandler responseHandler) {
        String url = String.format(Locale.getDefault(), DoorSensorServiceUrl.WIFI_DOOR_SENSOR_LIST, limit, offset);
        ServiceManager.getInstance().get(url, null, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                try {
                    if (data != null) {
                        Sensors sensors = new Gson().fromJson(data.toString(), Sensors.class);
                        if (responseHandler != null) {
                            responseHandler.onSuccess(sensors);
                        }
                    } else if (responseHandler != null) {
                        responseHandler.onSuccess(new Sensors());
                    }
                } catch (Exception e) {
                    Logger.e(e);
                    if (responseHandler != null) {
                        responseHandler.onError(ServiceManager.getInstance().getErrorMessage(e));
                    }
                }
            }

            @Override
            public void onAuthError(String message) {
                if (responseHandler != null) {
                    responseHandler.onAuthError(message);
                }
            }

            @Override
            public void onError(String message) {
                if (responseHandler != null) {
                    responseHandler.onError(message);
                }
            }
        });
    }

    /**
     * Add a new door sensor.
     * Expects payload: { "name": name, "serialNumber": serial, "scratch_code": scratchCode }
     */
    public void addSensor(JSONObject payload, final ResponseHandler responseHandler) {
        ServiceManager.getInstance().post(DoorSensorServiceUrl.ADD_DOOR_SENSOR, payload, responseHandler);
    }

    /**
     * Update door sensor name.
     */
    public void updateSensorName(String sensorId, JSONObject payload, final ResponseHandler responseHandler) {
        String url = DoorSensorServiceUrl.UPDATE_DOOR_SENSOR + sensorId;
        ServiceManager.getInstance().post(url, payload, responseHandler);
    }

    /**
     * Delete a door sensor on the backend server.
     */
    public void deleteSensor(String sensorId, final ResponseHandler responseHandler) {
        String url = DoorSensorServiceUrl.DELETE_DOOR_SENSOR + sensorId;
        ServiceManager.getInstance().delete(url, null, responseHandler);
    }
}

