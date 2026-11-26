package com.payoda.smartlock.sensors.service;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.DoorSensorServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.sensors.model.Sensor;
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
     * Factory reset a door sensor on the backend server.
     * Sends the complete sensor model with status = "2" (Constant.FACTORY_RESET),
     * matching Lock's updatelock behavior so the backend successfully deletes the DB row.
     */
    public void resetSensorOnServer(Sensor sensor, final ResponseHandler responseHandler) {
        if (sensor == null || TextUtils.isEmpty(sensor.getId())) {
            if (responseHandler != null) {
                responseHandler.onError("Sensor ID is missing");
            }
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("id", sensor.getId());
            payload.put("name", sensor.getName() != null ? sensor.getName() : "");

            String serial = sensor.getSerialNumber() != null ? sensor.getSerialNumber() : sensor.getCode();
            if (serial != null) {
                payload.put("serial_number", serial);
                payload.put("serialNumber", serial);
            }
            if (!TextUtils.isEmpty(sensor.getScratchCode())) {
                payload.put("scratch_code", sensor.getScratchCode());
                payload.put("scratchCode", sensor.getScratchCode());
            }
            if (!TextUtils.isEmpty(sensor.getOwnerId())) {
                payload.put("owner_id", sensor.getOwnerId());
            }
            if (!TextUtils.isEmpty(sensor.getBattery())) {
                payload.put("battery", sensor.getBattery());
            }

            // Exactly matching Lock: status = "2" (Constant.FACTORY_RESET)
            payload.put("status", Constant.FACTORY_RESET);

            String url = DoorSensorServiceUrl.UPDATE_DOOR_SENSOR + sensor.getId();
            Logger.d("##### [SENSOR_RESET] Resetting sensor on server via: " + url + " | payload: " + payload.toString());

            ServiceManager.getInstance().post(url, payload, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Logger.d("##### [SENSOR_RESET] Server reset success: " + data);
                    if (responseHandler != null) {
                        responseHandler.onSuccess(data);
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Logger.d("##### [SENSOR_RESET] Server reset auth error: " + message);
                    if (responseHandler != null) {
                        responseHandler.onAuthError(message);
                    }
                }

                @Override
                public void onError(String message) {
                    Logger.d("##### [SENSOR_RESET] Server reset error: " + message);
                    if (responseHandler != null) {
                        responseHandler.onError(message);
                    }
                }
            });

        } catch (Exception e) {
            Logger.e(e);
            if (responseHandler != null) {
                responseHandler.onError(e.getMessage());
            }
        }
    }

    public void resetSensorOnServer(String sensorId, final ResponseHandler responseHandler) {
        Sensor s = new Sensor();
        s.setId(sensorId);
        resetSensorOnServer(s, responseHandler);
    }

    /**
     * Delete / Reset a door sensor on the backend server.
     */
    public void deleteSensor(String sensorId, final ResponseHandler responseHandler) {
        resetSensorOnServer(sensorId, responseHandler);
    }

    public void deleteSensor(Sensor sensor, final ResponseHandler responseHandler) {
        resetSensorOnServer(sensor, responseHandler);
    }
}

