package com.payoda.smartlock.constants;

import com.payoda.smartlock.BuildConfig;

/**
 * Service URLs specifically for the Door Sensor feature.
 * Automatically resolves against the active build flavor (Staging vs Production).
 */
public class DoorSensorServiceUrl {

    // e.g. https://techlocstaging.in/door_sensor/api/web/v1 (Staging)
    // e.g. https://techloc.in/door_sensor/api/web/v1 (Production)
    public static final String SENSOR_SERVICE_URL = BuildConfig.BASE_URL + "/door_sensor/api/web/v1";

    // Door Sensor API endpoints (cloned from lock endpoints under /door_sensor)
    public static final String SENSOR_LIST = SENSOR_SERVICE_URL + "/locks/locklist?limit=%s&offset=%s";
    public static final String ADD_SENSOR = SENSOR_SERVICE_URL + "/locks/newlock";
    public static final String UPDATE_SENSOR = SENSOR_SERVICE_URL + "/locks/updatelock?id=";
    public static final String SENSOR_HISTORY = SENSOR_SERVICE_URL + "/activities/activitylist?id=%s&limit=%s&offset=%s";

    // Wi-Fi Configuration HTTP endpoint destination (sent to the sensor hardware)
    public static final String SENSOR_WIFI_ACTIVITIES = SENSOR_SERVICE_URL + "/activities";
}

