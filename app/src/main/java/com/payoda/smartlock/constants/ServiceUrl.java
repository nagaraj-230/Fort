package com.payoda.smartlock.constants;

import com.payoda.smartlock.BuildConfig;

/**
 * This class contains all service url details and environment.
 * Created by david.
 */

@SuppressWarnings("ConstantConditions")
public class ServiceUrl {

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    public static native String getLockURL();

    private static final String VERSION_PATH = "/api/web/v1";
    private static final String SERVICE_URL = BuildConfig.BASE_URL + VERSION_PATH;

    public static final String WIFI_LOCK_SERVICE_URL = getLockURL(); //BuildConfig.WIFI_LOCK_SERVICE_URL;


    public static final String WIFI_LOCK_ACTIVATE = WIFI_LOCK_SERVICE_URL + "/activate";
    public static final String WIFI_LOCK_DEACTIVATE = WIFI_LOCK_SERVICE_URL + "/deactivate";

    public static final String WIFI_LOCK_DISENGAGE = WIFI_LOCK_SERVICE_URL + "/disengage";
    public static final String WIFI_LOCK_BATTERY = WIFI_LOCK_SERVICE_URL + "/battery-level";
    public static final String WIFI_LOCK_DATE_TIME = WIFI_LOCK_SERVICE_URL + "/date-time";
    public static final String WIFI_LOCK_ACCESS_LOG = WIFI_LOCK_SERVICE_URL + "/access-logs";

    public static final String WIFI_LOCK_STATUS = WIFI_LOCK_SERVICE_URL + "/lock-status";
    public static final String WIFI_LOCK_REWRITE_SLOT = WIFI_LOCK_SERVICE_URL + "/rewrite-slot";
    public static final String WIFI_LOCK_ALL_KEYS = WIFI_LOCK_SERVICE_URL + "/read-keys";
    public static final String WIFI_LOCK_RESET = WIFI_LOCK_SERVICE_URL + "/reset-device";

    public static final String WIFI_LOCK_ENROLL_FP = WIFI_LOCK_SERVICE_URL + "/enroll-fp";
    public static final String WIFI_LOCK_DELETE_FP = WIFI_LOCK_SERVICE_URL + "/delete-fp";
    public static final String WIFI_LOCK_ENROLL_RF = WIFI_LOCK_SERVICE_URL + "/enroll-rf";
    public static final String WIFI_LOCK_DELETE_RF = WIFI_LOCK_SERVICE_URL + "/delete-rf";

    public static final String WIFI_LOCK_RE_WRITE_PIN = WIFI_LOCK_SERVICE_URL + "/rewrite-pin";
    public static final String WIFI_LOCK_RE_WRITE_OTP = WIFI_LOCK_SERVICE_URL + "/rewrite-otp";
    public static final String WIFI_LOCK_AUTH_VIA_PIN = WIFI_LOCK_SERVICE_URL + "/authvia-pin";
    public static final String WIFI_LOCK_AUTH_VIA_FP = WIFI_LOCK_SERVICE_URL + "/authvia-fp";

    // v4.0
    public static final String WIFI_LOCK_CONFIG_MQTT = WIFI_LOCK_SERVICE_URL + "/config-wifi-mqtt";

    public static final String BRAND_INFO = SERVICE_URL + "/requests/branding";
    public static final String LOGIN = SERVICE_URL + "/users/login";
    public static final String SIGNUP = SERVICE_URL + "/users/createuser";
    public static final String FORGOT_PASSWORD = SERVICE_URL + "/users/forgotpassword";
    public static final String PROFILE = SERVICE_URL + "/users/profile";
    public static final String UPDATE_PROFILE = SERVICE_URL + "/users/updateprofile";
    public static final String UPDATE_TOKEN = SERVICE_URL + "/users/updatetoken";
    public static final String LOCK = SERVICE_URL + "/locks/locklist?limit=%s&offset=%s";
    public static final String ADD_LOCK = SERVICE_URL + "/locks/addlock";
    public static final String UPDATE_LOCK = SERVICE_URL + "/locks/updatelock?id=";
    public static final String NOTIFICATION = SERVICE_URL + "/notifications/notificationlist?limit=%s&offset=%s";
    public static final String ADD_HISTORY = SERVICE_URL + "/activities/addactivity?id=";
    public static final String GET_HISTORY = SERVICE_URL + "/activities/activitylist?id=%s&limit=%s&offset=%s";
    public static final String GET_LOCK_NOTIFICATION = SERVICE_URL + "/activities/masterlog?lock_id=%s&limit=%s&offset=%s";
    public static final String ASSIGN_USER = SERVICE_URL + "/keys/keylist?id=%s&owner=%s&type=%s";
    public static final String REQUEST_LIST = SERVICE_URL + "/requests/requestlist?limit=%s&offset=%s";
    public static final String TRANSFER_OWNER = SERVICE_URL + "/requests/createrequest";
    public static final String CREATE_REQUEST = SERVICE_URL + "/requests/createrequest";
    public static final String UPDATE_REQUEST = SERVICE_URL + "/requests/updaterequest?id=";
    public static final String REVOKE_REQUEST = SERVICE_URL + "/keys/updatekey?id=";
    public static final String TRANSFER_OWNER_REJECT = SERVICE_URL + "/requests/updaterequest?id=";
    public static final String TRANSFER_OWNER_ACCEPT = SERVICE_URL + "/locks/transfer?id=";
    public static final String UPDATE_KEY = SERVICE_URL + "/keys/updatekey?id=";
    public static final String SCHEDULE = SERVICE_URL + "/keys/updateschedule?key_id=";
    public static final String LOGOUT = SERVICE_URL + "/users/logout";
    public static final String CURRENT = SERVICE_URL + "/users/getcurrenttime";
    public static final String DELETE_ACCOUNT = SERVICE_URL + "/users/deleteaccount";

    public static final String ADD_FP = SERVICE_URL + "/locks/addfingerprint";
    public static final String USER_LIST = SERVICE_URL + "/locks/userlist?lock_id=";
    public static final String UPDATE_NAME = SERVICE_URL + "/locks/updateguestname";
    public static final String ADD_PRIVILEGE = SERVICE_URL + "/locks/adduserprivilege";

    public static final String ADD_PINS = SERVICE_URL + "/locks/addpins";
    public static final String ADD_OTPS = SERVICE_URL + "/locks/addotps";
    public static final String GET_OTP = SERVICE_URL + "/locks/getotp?lock_id=%s&next=%s";

    public static final String GET_LOCK_CONFIG = SERVICE_URL + "/versions/configlist";

    //V4.0
    public static final String ADD_LOCK_NEW = SERVICE_URL + "/locks/newlock";
    public static final String ENGAGE_LOCK_MQTT = SERVICE_URL + "/locks/%s/engage";
    public static final String REVOKE_MASTER_MQTT = SERVICE_URL + "/locks/%s/master/%s/revoke";
    public static final String REVOKE_USER_MQTT = SERVICE_URL + "/locks/%s/user/%s/revoke";
    public static final String DELETE_FP_MQTT = SERVICE_URL + "/locks/%s/key/%s/fp/%s";
    public static final String DELETE_RFID_MQTT = SERVICE_URL + "/locks/%s/rfid/%s";
    public static final String MANAGE_FP_MQTT = SERVICE_URL + "/locks/%s/fp/manage";
    public static final String MANAGE_PIN_MQTT = SERVICE_URL + "/locks/%s/pin/manage";
    public static final String REWRITE_PIN_MQTT = SERVICE_URL + "/locks/%s/pin";
    public static final String REWRITE_OTP_MQTT = SERVICE_URL + "/locks/%s/otp";
    public static final String TRANSFER_OWNER_ACCEPT_MQTT = SERVICE_URL + "/locks/%s/transferowner/%s";

    //v6.0
    public static final String REMOTE_ACCESS = SERVICE_URL + "/activities/remote_activity";
    public static final String WIFI_LOCK_CONFIG_HTTP = WIFI_LOCK_SERVICE_URL + "/config-wifi-http";
    public static final String WIFI_LOCK_PASSAGE_MODE = WIFI_LOCK_SERVICE_URL + "/passage-mode";
    public static final String PUSH_WIFI_CONFIG_STATUS = SERVICE_URL + "/ddddd"; //To get given wifi ssid and password  is connected to lock or not by remote server

    public static final String GET_VERSION_CONTROL = SERVICE_URL + "/activities/version-control";
}
