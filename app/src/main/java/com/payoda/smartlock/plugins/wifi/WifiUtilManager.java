package com.payoda.smartlock.plugins.wifi;

import static com.payoda.smartlock.plugins.bluetooth.BleManager.MANUFACTURER_CODE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;

import com.payoda.smartlock.model.LockVersionConfig;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.utils.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by david on 14/11/18.
 */
@SuppressLint("MissingPermission")
public class WifiUtilManager {

    private Context context = null;
    private CountDownTimer wifiScan;
    private WifiListener wifiListener = null;
    private final long WIFI_SCAN_TIME = 13000;
    private boolean isRequestNetwork = false;
    private static ConnectivityManager.NetworkCallback networkCallback = null;


    public WifiUtilManager(Context context, WifiListener wifiListener) {
        this.context = context;
        this.wifiListener = wifiListener;
    }

    private void connectToWifi(String SSID, String password) {

        String networkSSID = SSID;
        String networkPass = password;
        Logger.d("### Connecting SSID " + networkSSID + " " + password);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";
            conf.preSharedKey = "\"".concat(networkPass).concat("\"");
            //conf.preSharedKey = "\"" + networkPass + "\"";
            conf.priority = 100; // Set your desired priority

            /*
            networkFound,
            configuredNetworks,
            iteration,
            condition.
            Added newly in Fort App
             */
            boolean networkFound = false;
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

            for (WifiConfiguration config : configuredNetworks) {

                if (config.SSID != null && config.SSID.equals("\"" + SSID + "\"")) {
                    // Network already configured, connect to it
                    wifiManager.enableNetwork(config.networkId, true);

                    // Register a BroadcastReceiver to listen for connection changes
                    // This broadcast receiver used for avoid connection delay
                    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {

                            if (wifiManager.getConnectionInfo().getNetworkId() == config.networkId) {
                                context.unregisterReceiver(this);
                            }
                        }
                    };

                    // Register the BroadcastReceiver to listen for Wi-Fi state changes
                    context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                    networkFound = true;
                    break;
                }
            }

            if (!networkFound) {
                int networkId = wifiManager.addNetwork(conf);
                wifiManager.enableNetwork(networkId, true);
            }
        }
        else {
            if (isRequestNetwork)
                return;
            WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(SSID)
                    .setWpa2Passphrase(password)
                    .build();

            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build();

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    createNetworkRoute(network, connectivityManager);
                    Logger.d("### onAvailable");
                }

                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    Logger.d("### onLosing");
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Logger.d("### onLost");
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Logger.d("### onUnavailable");
                }
            };
            connectivityManager.requestNetwork(networkRequest, networkCallback);
            isRequestNetwork = true;
        }

    }

    private static void createNetworkRoute(Network network, ConnectivityManager connectivityManager) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(network);
        } else {
            ConnectivityManager.setProcessDefaultNetwork(network);
        }
    }

    int count = 0;

    public void startScanning(String lockVersion, String SSID, String password) {

        count = 0;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            //Toast.makeText(context, "Enable WIFI", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        long wifiScanTime = WIFI_SCAN_TIME;

        if (lockVersion != null) {

            LockVersionConfig.ConfigData lockConfigData = SecuredStorageManager.getInstance().getLockVersionData(lockVersion);
            if (lockConfigData != null && !lockConfigData.getWifiTime().isEmpty()) {
                wifiScanTime = Long.parseLong(lockConfigData.getWifiTime()) * 1000;
            }

        }

        // TODO delete this
        // wifiScanTime = 20000;

        Logger.d("### startScanning wifi time --> ", String.valueOf(wifiScanTime));

        wifiScan = new CountDownTimer(wifiScanTime, 1000) { // 20

            public void onTick(long millisUntilFinished) {
                count++;

                Logger.d("### getSSID() = " + getSSID(context));

                if (SSID.equalsIgnoreCase(getSSID(context))) {
                    if (wifiListener != null) {
                        wifiListener.connectionSuccess();
                        wifiScan.cancel();
                    }
                }
                else {
                    Logger.d("### Connection Attempt " + count);
                    Logger.d("### Connection SSID " + SSID);
                    Logger.d("### Connection password " + password);
                    connectToWifi(SSID, password);
                }

            }

            public void onFinish() {
                Logger.d("WIFI Scan Time End");
                if (wifiListener != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                    }

                    wifiListener.connectionTimeOut();
                }
            }


        }.start();

    }

    public String getSSID(final Context context) {

        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());

            if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
                    return mWifiManager.getConnectionInfo().getSSID() != null ?
                            mWifiManager.getConnectionInfo().getSSID().replaceAll("^\"|\"$", "") : null;
                }
            }
        }
        return null;
    }

    private String SEARCHING_SSID = null;

    public void startSearching(long wifiScanTime, String password) {

        SEARCHING_SSID = null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            //Toast.makeText(context, "Enable WIFI", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        Logger.d("startSearching wifi time --> ", String.valueOf(wifiScanTime));
        wifiScan = new CountDownTimer(wifiScanTime, 3000) {
            public void onTick(long millisUntilFinished) {
                if (SEARCHING_SSID == null) {
                    wifiManager.startScan();
                    // Checking Available WIFI name Starts with Manufacture code (Eg: Astrix or Fort)
                     List<ScanResult> list = wifiManager.getScanResults();
                    //List<WifiConfiguration> list = wifiManager.getScanResults();
                    for (ScanResult i : list) {
                        if (isValidSSID(i.SSID)) {
                            Logger.d("Valid SSID " + i.SSID);
                            SEARCHING_SSID = i.SSID.replaceAll("\"", "");
                            //connectToWifi(SEARCHING_SSID,"12345678");
                            connectToWifi(SEARCHING_SSID, password);
                            break;
                        }
                    }
                } else {
                    if (SEARCHING_SSID.equalsIgnoreCase(getSSID(context))) {
                        // Connect to Specific WIFI
                        if (wifiListener != null) {
                            wifiListener.connectionSuccess();
                            wifiScan.cancel();
                        }
                    } else {
                        Logger.d("Connection Attempt " + count);
                        connectToWifi(SEARCHING_SSID, password);
                    }

                }

            }

            public void onFinish() {
                Logger.d("WIFI Scan Time End");
                if (wifiListener != null) {
                    wifiListener.connectionTimeOut();
                }
            }
        }.start();

    }

    private boolean isValidSSID(String SSID) {
        Logger.d("SSID " + SSID);
        if (SSID != null) {
            Logger.d("SSID is not null");
            if (SSID.contains(MANUFACTURER_CODE)) {
                Logger.d("SSID contains with " + MANUFACTURER_CODE);
                return true;
            }

        }
        return false;
    }

    public static void forgetMyNetwork(Context context,String ssidToForget) {
        Logger.d("### forgetMyNetwork");


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration config : configuredNetworks) {
                if (config.SSID != null && config.SSID.equals("\"" + ssidToForget + "\"")) {
                    // Remove the network configuration
                    wifiManager.removeNetwork(config.networkId);
                    break;
                }
            }

        } else {

            // Code for Android Q and later
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            connectivityManager.bindProcessToNetwork(null);

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            // Get the connected WiFi SSID
            String ssid = wifiInfo.getSSID();
            // Remove surrounding quotes from the SSID
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                String cSSid =  ssid.substring(1, ssid.length() - 1);
                if (cSSid.equalsIgnoreCase(ssidToForget)){
                    connectivityManager.bindProcessToNetwork(null);
                }
            }

            if (networkCallback != null){
                connectivityManager.unregisterNetworkCallback(networkCallback);

            }
        }



    }

    public static void forgetMyNetwork1(Context context, String ssid) {

        if (isConnectedToWifi(context, ssid)) {

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager == null) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .build();

                NetworkRequest request = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(specifier)
                        .build();

                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivityManager != null) {
                    connectivityManager.unregisterNetworkCallback(new ConnectivityManager.NetworkCallback());
                    connectivityManager.requestNetwork(request, new ConnectivityManager.NetworkCallback());
                } else {
                    Logger.d("### ConnectivityManager is null");
                }
            } else {
                for (WifiConfiguration configuredNetwork : wifiManager.getConfiguredNetworks()) {
                    if (configuredNetwork.SSID != null && configuredNetwork.SSID.equals("\"" + ssid + "\"")) {
                        wifiManager.removeNetwork(configuredNetwork.networkId);
                        wifiManager.saveConfiguration();
                        break;
                    }
                }
            }

        }

    }

    public static boolean isConnectedToWifi(Context context, String ssid) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                String currentSsid = wifiInfo.getSSID().replace("\"", ""); // Remove surrounding quotes
                Logger.d("### isConnectedToWifi currentSsid = " + currentSsid);
                Logger.d("### isConnectedToWifi givenSsid = " + ssid);
                return ssid.equals(currentSsid);
            }
        }
        return false;
    }

    public interface WifiListener {
        void connectionSuccess();

        public void connectionTimeOut();
    }

    private void disconnectFromWifi(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getMethod("disconnect");
            method.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
