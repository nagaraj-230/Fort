package com.payoda.smartlock.utils;

import android.os.Build;

public class AX100Util {

    public static boolean isNotEqualAndAboveQ(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true;
        }
        return false;
    }
}
