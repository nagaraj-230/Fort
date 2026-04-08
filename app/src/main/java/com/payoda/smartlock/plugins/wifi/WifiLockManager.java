package com.payoda.smartlock.plugins.wifi;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.utils.Logger;

import java.util.List;

public class WifiLockManager {

    private static final String TAG = WifiLockManager.class.getSimpleName();
    private static WifiLockManager mInstance;
    private static String MANUFACTURER_CODE = BuildConfig.MANUFACTURER_CODE;


    public static synchronized WifiLockManager getInstance() {

        if (mInstance == null) {

            mInstance = new WifiLockManager();

            BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
            if(brandInfo != null && !brandInfo.getManufacturerCode().isEmpty()){
                MANUFACTURER_CODE = brandInfo.getManufacturerCode();
                Logger.d("### MANUFACTURER_CODE WifiLockManager = " + MANUFACTURER_CODE);
            }

        }


        return mInstance;

    }

    /**
     * Method to check wifi is enabled or not
     *
     * @param context - current context of the app
     * @return boolean - return true if wifi enabled otherwise false.
     */
    public boolean isWifiEnabled(final Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null)
            return mWifiManager.isWifiEnabled();
        else
            return false;
    }

    /**
     * Method to check lock wifi is enabled or not
     *
     * @param context - current context of the app
     * @return boolean - return true if wifi enabled otherwise false.
     */
    public boolean isWifiLockConnected(final Context context) {
        String connectedWifi = getSsid(context);

        if (connectedWifi != null && !TextUtils.isEmpty(connectedWifi)) {
            connectedWifi = connectedWifi.toLowerCase();
            return connectedWifi.contains(MANUFACTURER_CODE.toLowerCase());
        }
        return false;
    }

    public boolean isSameWifiLockConnected(final Context context, String ssid) {

        String connectedWifi = getSsid(context);

        if (connectedWifi != null && !TextUtils.isEmpty(connectedWifi)) {
            connectedWifi = connectedWifi.toLowerCase();
            return connectedWifi.equalsIgnoreCase(MANUFACTURER_CODE + ssid);
        }

        return false;
    }

    public String getConnectedWifiName(final Context context) {
        String connectedWifi = getSsid(context);
        if (connectedWifi != null && !TextUtils.isEmpty(connectedWifi)) {
            connectedWifi = connectedWifi.toLowerCase();
        }
        return connectedWifi;
    }

    public String getSerialNumber(String ssid) {
        if (ssid != null && !TextUtils.isEmpty(ssid)) {
            return ssid.replace(MANUFACTURER_CODE, "");
        }
        return null;
    }

    public String getSsid(final Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
            if (state == DetailedState.CONNECTED || state == DetailedState.OBTAINING_IPADDR) {
                if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
                    return mWifiManager.getConnectionInfo().getSSID() != null ? mWifiManager.getConnectionInfo().getSSID().replaceAll("^\"|\"$", "") : null;
                }
            }
        }
        return null;
    }



    /**
     *  CAUTION
     *  **/

    // TODO if this app support all lock version means use this
    // TODO if this app support V6.0 only means delete this and you need to update Brand info-manufacturerCode from Backend as 'FORT_'
    // TODO if this app support from v1.0 to V3.2 means delete this and you need to update Brand info- manufacturerCode from Backend as 'ASTRIX_'
    // TODO currently brand info - manufacturer code from Backend is 'FORT_'
    // TODO Make sure Aware of this
    public void updateManufactureCode(Lock mLock) {

        Logger.d("### updateManufactureCode ");

        if (mLock != null ) {

            if (!mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_6_0)) {
                BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
                brandInfo.setManufacturerCode(Constant.DEFAULT_MANUFACTURE_CODE);
                Logger.d("### updateManufactureCode Wifi  brandInfo " + brandInfo);
                SecuredStorageManager.getInstance().setBrandInfo(brandInfo);
                MANUFACTURER_CODE = brandInfo.getManufacturerCode();
            }
        }
    }


}
