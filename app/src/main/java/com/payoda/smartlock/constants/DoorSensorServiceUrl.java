package com.payoda.smartlock.constants;

import com.payoda.smartlock.BuildConfig;

/**
 * Service URLs and P2P endpoints specifically for the Door Sensor feature.
 * Follows the DOOR_SENSOR naming convention for clarity and maintainability.
 */
public class DoorSensorServiceUrl {

    // Cloud Backend Service URL (e.g. https://techlocstaging.in/door_sensor/api/web/v1)
    public static final String DOOR_SENSOR_SERVICE_URL = BuildConfig.BASE_URL + "/door_sensor/api/web/v1";

    // Cloud Backend Endpoints
    public static final String WIFI_DOOR_SENSOR_LIST = DOOR_SENSOR_SERVICE_URL + "/locks/locklist?limit=%s&offset=%s";
    public static final String ADD_DOOR_SENSOR = DOOR_SENSOR_SERVICE_URL + "/locks/newlock";
    public static final String UPDATE_DOOR_SENSOR = DOOR_SENSOR_SERVICE_URL + "/locks/updatelock?id=";
    public static final String DELETE_DOOR_SENSOR = UPDATE_DOOR_SENSOR;
    public static final String DOOR_SENSOR_HISTORY = DOOR_SENSOR_SERVICE_URL + "/activities/activitylist?id=%s&limit=%s&offset=%s";

    // Destination URL sent to the sensor hardware to report events (http-ip)
    public static final String DOOR_SENSOR_ACTIVITIES_DESTINATION = DOOR_SENSOR_SERVICE_URL + "/activities";

    // Local P2P SoftAP Provisioning Endpoints (ESP32-C3 hardware SoftAP at http://192.168.4.1)
    public static final String WIFI_DOOR_SENSOR_BASE = "http://192.168.4.1";
    public static final String WIFI_DOOR_SENSOR_ADD = WIFI_DOOR_SENSOR_BASE + "/add";
    public static final String WIFI_DOOR_SENSOR_CONFIG_HTTP = WIFI_DOOR_SENSOR_BASE + "/config-wifi-http";
    public static final String WIFI_DOOR_SENSOR_RESET = WIFI_DOOR_SENSOR_BASE + "/reset-device";

    // Legacy Aliases for backwards compatibility
    public static final String SENSOR_SERVICE_URL = DOOR_SENSOR_SERVICE_URL;
    public static final String SENSOR_LIST = WIFI_DOOR_SENSOR_LIST;
    public static final String ADD_SENSOR = ADD_DOOR_SENSOR;
    public static final String UPDATE_SENSOR = UPDATE_DOOR_SENSOR;
    public static final String SENSOR_HISTORY = DOOR_SENSOR_HISTORY;
    public static final String SENSOR_WIFI_ACTIVITIES = DOOR_SENSOR_ACTIVITIES_DESTINATION;
}
