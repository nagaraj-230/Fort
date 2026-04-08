package com.payoda.smartlock.constants;

public class Constant {

    public static final String OFFLINE_LOCK_ID="_offline";

    public static final String TAG = "PAYODA";
    public static final String ANDROID = "Android";
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";
    public static final String SCREEN_NAME = "SCREEN_NAME";
    public static final String SCREEN_CODE = "SCREEN_CODE";
    public static final String SCREEN_DATA = "SCREEN_DATA";
    public static final String VALIDATE_PIN_FLOW = "VALIDATE_PIN_FLOW";
    public static final String NAVIGATION_LOCK_TRANSFER_OWNER="NAVIGATE_LOCK_TRANSFER_OWNER";
    public static final String NAVIGATION_LOCK_FACTORY_RESET="NAVIGATE_LOCK_FACTORY_RESET";

    public static final long WIFI_DELAY_TIME=3000;

    public static final String OWNER_ID = "OwnerID";
    public static final String OWNER = "Owner";
    public static final String MASTER = "Master";
    public static final String USER = "User";

    public static final String INACTIVE = "0";
    public static final String ACTIVE = "1";
    public static final String TRANSFER = "2";
    public static final String FACTORY_RESET = "2";

    public static final String ANDROID_VERSION_CODE="android_version_code";
    public static final String ANDROID_FORCE_UPDATE="android_force_update";
    public static final String ANDROID_FORCE_UPDATE_MESSAGE="android_force_update_message";

    public static final int LIMIT=50;

    public static final String HW_VERSION_1="v1.0";
    public static final String HW_VERSION_2="v2.0";
    public static final String HW_VERSION_2_1="v2.1";
    public static final String HW_VERSION_3="v3.0";
    public static final String HW_VERSION_3_1="v3.1";
    public static final String HW_VERSION_3_2="v3.2";
    public static final String HW_VERSION_4_0="v4.0";
    public static final String HW_VERSION_6_0="v6.0";
    //public static final String HW_VERSION_2="0F";
    public static long WIFI_SCAN=5000;

    public static String WIFI_NOT_CONNECTED = "WIFI_NOT_CONNECTED";
    public static String MQTT_NOT_CONNECTED = "MQTT_NOT_CONNECTED";

    public static final String RFID="RFID";
    public static final String FP="Fingerprint";

    public static final String RFID_EMPTY="00000000";

    //V4.0
    public static final String ENGAGE_COMMAND = "ENGAGE";
    public static final String USER_REVOKE_COMMAND = "USER_REVOKE";
    public static final String MASTER_REVOKE_COMMAND = "MASTER_REVOKE";
    public static final String FP_DELETE_COMMAND = "FP_DELETE";
    public static final String RFID_DELETE_COMMAND = "RFID_DELETE";
    public static final String FP_ON_COMMAND = "FP_ON";
    public static final String FP_OFF_COMMAND = "FP_OFF";
    public static final String PIN_ON_COMMAND = "PIN_ON";
    public static final String PIN_OFF_COMMAND = "PIN_OFF";
    public static final String PIN_REWRITE_COMMAND = "PIN_REWRITE";
    public static final String OTP_REWRITE_COMMAND = "OTP_REWRITE";
    public static final String OWNER_TRANSFER = "OWNER_TRANSFER";

    public static final String STATUS = "status";
    public static final String BODY = "body";
    public static final String TITLE = "title";
    public static final String COMMAND = "command";

    // V6.0
    public static final String ACCEPT = "1";
    public static final String REJECT = "2";
    public static final String REMOTE_ACCESS_COMMAND = "1";
    public static final String REMOTE_ACCESS_COMMAND_BATTERY = "3";
    public static final String REMOTE_ACCESS_DATA = "remote_access_payloads";
    public static final String V6_MANUFACTURE_CODE = "FORT_";
    public static final String DEFAULT_MANUFACTURE_CODE = "ASTRIX_";
    public static final String PASSAGE_ON = "1";
    public static final String PASSAGE_OFF = "0";
    //public static final Integer DIALOG_DISMISS_SECS = 35000; // 35secs
    public static final String PUSH_WIFI_CONFIG = "PUSH_WIFI_CONFIG";


    public enum SCREEN {

        ERROR,
        SPLASH,
        LOGIN,
        SIGNUP,
        FORGOT_PASSWORD,
        FORGOT_PASSWORD_SUCCESS,
        SUCCESSFULLY_REGISTERED,
        PIN,
        PIN_SUCCESS,
        PROFILE,
        LOCK_ADD,
        LOCK_DETAILS,
        WIFI_MQTT_CONFIGURATION,
        ASSIGN_USERS,
        USERS_REQUEST,
        NOTIFICATIONS,
        TRANSFER_OWNER,
        HISTORY,
        TIME_OUT_PIN,
        TERMS,
        PRIVACY_POLICY,
        LOCK_LIST,
        OWNER_LOCK_LIST,
        SCHEDULE,
        FINGER_PRINT_LIST,
        FINGER_PRINT_ADD,
        RFID_LIST,
        RFID_ADD,
        RFID_SUCCESS,
        USER_LIST
    }

    public enum JOBS {
        ALL,
        ADD_LOCK,
        UPDATE_KEYS,
        ACTIVITY_HISTORY,
        WIFI_MQTT_CONFIG
    }

}















