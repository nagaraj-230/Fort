package com.payoda.smartlock.locks;

import static com.payoda.smartlock.utils.DateTimeUtils.DDMMYY_HHMMSS;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import com.payoda.smartlock.R;
import com.payoda.smartlock.locks.callback.IKeyListener;
import com.payoda.smartlock.locks.model.LockFlow;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.bluetooth.ICharCallBack;
import com.payoda.smartlock.plugins.bluetooth.IDeviceCallBack;
import com.payoda.smartlock.plugins.bluetooth.IServiceCallBack;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.HexUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

public class LockManager {

    private static final String TAG = LockManager.class.getSimpleName();
    private static LockManager mInstance;
    private ArrayList<String> alIds;
    private ArrayList<String> alKeys;
    private ArrayList<String> alLogs;
    public final static int TOTAL_IDS = 2;
    public final static int TOTAL_KEYS = 24;
    private final static int TOTAL_LOGS = 50;
    private final static long MAX_SCAN_TIME = 7 * 1000;

    private LockManager() {
    }

    public static synchronized LockManager getInstance() {
        if (mInstance == null) {
            mInstance = new LockManager();
        }
        return mInstance;
    }

    private IKeyListener iKeyListener;

    protected void activateDevice(final Activity activity, final String address, final String ssid,
                                  final String activationCode, IKeyListener listener) {
        BleManager.getInstance().stopScan();
        if (listener == null) return;
        this.iKeyListener = listener;
            final String serviceId = "00FF";
        BleManager.getInstance().setDeviceConnected(false);
        startDisconnectScheduler();
        BleManager.getInstance().connectDevice(activity, address, ssid, new IDeviceCallBack() {
            @Override
            public void onDeviceConnected() {
                stopDisconnectScheduler();
            }

            @Override
            public void onServiceDiscovered() {
                BleManager.getInstance().connectService(serviceId, new IServiceCallBack() {
                    @Override
                    public void onServiceConnected(BluetoothGattService service) {
                        activateLock(activity, service, activationCode);
                    }

                    @Override
                    public void onServiceDisconnected() {
                        AppDialog.showAlertDialog(activity, "Lock service disconnected. Please connect again.");
                    }
                });
            }

            @Override
            public void onAuthFailure() {
                AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
            }

            @Override
            public void onDeviceDisconnected() {

                if (!BleManager.getInstance().isDeviceConnected()) {
                    if (listener != null) {
                        listener.onDeviceNotConnected();
                    } else {
                        Loader.getInstance().hideLoader(activity);
                    }
                } else {
                    Loader.getInstance().hideLoader(activity);
                }
                stopDisconnectScheduler();
            }

            @Override
            public void onError(String error) {
                AppDialog.showAlertDialog(activity, error);
            }

            @Override
            public void onLocationPermissionError(String error) {
                AppDialog.showAlertDialog(activity, error);
            }

            @Override
            public void onPermissionError(String error) {
                BleManager.getInstance().checkLocationPermission(activity);
            }

            @Override
            public void onUpdateMacAddress(String ssid, String macAddress) {
                if (listener != null) {
                    listener.onMacAddressUpdate(ssid, macAddress);
                }
            }
        });
    }

    private void activateLock(final Activity activity, final BluetoothGattService service, String data) {
        Logger.d("activateLock() data->" + data);
        final String charId = "FF0B";

        Handler handler = new Handler(activity.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().setBleData(service, charId, data, new ICharCallBack() {
                    @Override
                    public void readCallBack(String data) {

                    }

                    @Override
                    public void writeCallback(String data) {
                        getOwnerIds(activity, service);
                    }

                    @Override
                    public void onAuthFailure() {
                        AppDialog.showAlertDialog(activity, "Invalid Lock code.");
                    }

                    @Override
                    public void onFailure() {
                        AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
                    }
                });
            }
        });


    }

    private void getOwnerIds(final Activity activity, final BluetoothGattService service) {
        alIds = new ArrayList<>();
        getOwnerId(activity, service, alIds.size());
    }

    private void getOwnerId(final Activity activity, final BluetoothGattService service, int data) {
        Logger.d("getOwnerId() data->" + data);
        final String charId = "FF01";
        BleManager.getInstance().setBleIntData(service, charId, data, new ICharCallBack() {
            @Override
            public void readCallBack(String data) {

            }

            @Override
            public void writeCallback(String result) {
                //Read Ids
                final String charId = "FF13";
                BleManager.getInstance().getBleData(service, charId, new ICharCallBack() {
                    @Override
                    public void readCallBack(String result) {
                        alIds.add(result);
                        if (alIds.size() < TOTAL_IDS) {
                            getOwnerId(activity, service, alIds.size());
                        } else {
                            if (iKeyListener != null) {
                                iKeyListener.onLockIds(alIds);
                            }
                            getLockKeys(activity, service);
                        }
                    }

                    @Override
                    public void writeCallback(String result) {

                    }

                    @Override
                    public void onAuthFailure() {

                    }

                    @Override
                    public void onFailure() {
                        AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
                    }
                });


            }

            @Override
            public void onAuthFailure() {

            }

            @Override
            public void onFailure() {
                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
            }
        });
    }

    private void getLockKeys(final Activity activity, final BluetoothGattService service) {
        alKeys = new ArrayList<>();
        getLockKey(activity, service, alKeys.size());
    }

    private void getHWVersion(final Activity activity, final BluetoothGattService service) {
        final String versionCharId = "FF17";
        Logger.d("======Fetch Version Version=======");
        BleManager.getInstance().getBleData(service, versionCharId, new ICharCallBack() {
            @Override
            public void readCallBack(String result) {
                Logger.d("======Version=======" + result);
                if (iKeyListener != null) {
                    iKeyListener.onBatteryUpdate(result);
                }
                deactivateScratchCode(activity, service);
            }

            @Override
            public void writeCallback(String result) {

            }

            @Override
            public void onAuthFailure() {
                deactivateScratchCode(activity, service);
            }

            @Override
            public void onFailure() {
                deactivateScratchCode(activity, service);
            }
        });
    }

    private void getLockKey(final Activity activity, final BluetoothGattService service, int data) {
        Logger.d("getLockKey() data->" + data);
        final String charId = "FF02";
        BleManager.getInstance().setBleIntData(service, charId, data, new ICharCallBack() {
            @Override
            public void readCallBack(String data) {

            }

            @Override
            public void writeCallback(String data) {
                //Read Slot Key
                final String charId = "FF13";
                BleManager.getInstance().getBleData(service, charId, new ICharCallBack() {
                    @Override
                    public void readCallBack(String data) {
                        alKeys.add(data);
                        //Call recursive function until all keys fetched.
                        if (alKeys.size() < TOTAL_KEYS) {
                            getLockKey(activity, service, alKeys.size());
                        } else {
                            Logger.d("All keys fetched.");
                            if (iKeyListener != null) {
                                iKeyListener.onLockKeys(alKeys);
                            }
                            //deactivateScratchCode(activity, service);
                            getHWVersion(activity, service);
                        }
                    }

                    @Override
                    public void writeCallback(String data) {

                    }

                    @Override
                    public void onAuthFailure() {

                    }

                    @Override
                    public void onFailure() {
                        AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
                    }
                });
            }

            @Override
            public void onAuthFailure() {

            }

            @Override
            public void onFailure() {
                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
            }
        });
    }

    private void deactivateScratchCode(final Activity activity, final BluetoothGattService service) {
        Logger.d("deactivateScratchCode()");
        final String charId = "FF0B";
        final String data = "00000000";
        BleManager.getInstance().setBleData(service, charId, data, new ICharCallBack() {
            @Override
            public void readCallBack(String data) {

            }

            @Override
            public void writeCallback(String data) {
                Logger.d("Scratch Code deactivated.");
                BleManager.getInstance().disConnectDevice();
                if (iKeyListener != null) {
                    BleManager.getInstance().setDeviceConnected(true);
                    iKeyListener.onLockActivated();
                }
            }

            @Override
            public void onAuthFailure() {

            }

            @Override
            public void onFailure() {
                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
            }
        });
    }

    protected void engageLock(final Activity activity, final String address, final String ssid, final String key, final LockFlow flow,
                              final int slotNumber, final IKeyListener listener) {


        Logger.d("### Lock Manager Ble engageLock() address " + address);
        Logger.d("### Lock Manager Ble engageLock() ssid " + ssid);
        Logger.d("### Lock Manager Ble engageLock() key " + key);
        Logger.d("### Lock Manager Ble engageLock() flow " + flow);
        Logger.d("### Lock Manager Ble engageLock() slotNumber " + slotNumber);

        /*  address 0
            ssid FORT_FO00000000000004
            key 14E7D959FO00000000000004EDF26761
            flow ENGAGE
            slotNumber 1    */

        Loader.getInstance().showLoader(activity);
        BleManager.getInstance().setDeviceConnected(false);
        BleManager.getInstance().stopScan();
        startDisconnectScheduler();

        final String serviceId = "00FF";

        BleManager.getInstance().connectDevice(activity, address, ssid, new IDeviceCallBack() {

            @Override
            public void onDeviceConnected() {
                stopDisconnectScheduler();
            }

            @Override
            public void onServiceDiscovered() {

                BleManager.getInstance().connectService(serviceId, new IServiceCallBack() {

                    @Override
                    public void onServiceConnected(final BluetoothGattService service) {
                        // Authenticate the lock
                        final String charId = "FF10";

                        BleManager.getInstance().setBleHexData(service, charId, key, new ICharCallBack() {

                            @Override
                             public void readCallBack(String data) {

                            }

                            @Override
                            public void writeCallback(String data) {

                                if (flow == LockFlow.ENGAGE) {

                                    setDateTime(activity, service, listener);

                                } else if (flow == LockFlow.TRANSFER_OWNER) {

                                    final String charId = "FF01";
                                    BleManager.getInstance().setBleIntData(service, charId, slotNumber, new ICharCallBack() {
                                        @Override
                                        public void readCallBack(String data) {

                                        }

                                        @Override
                                        public void writeCallback(String data) {

                                            final String charId = "FF13";
                                            BleManager.getInstance().getBleData(service, charId, new ICharCallBack() {

                                                @Override
                                                public void readCallBack(String data) {

                                                    ArrayList<String> alIds = new ArrayList<>();
                                                    alIds.add(data);
                                                    listener.onLockIds(alIds);
                                                    setDateTime(activity, service, listener);

                                                }

                                                @Override
                                                public void writeCallback(String data) {

                                                }

                                                @Override
                                                public void onAuthFailure() {

                                                }

                                                @Override
                                                public void onFailure() {

                                                    Loader.getInstance().hideLoader();
                                                    AppDialog.showAlertDialog(activity, "Failed to engage the Lock. Please try again.");

                                                }
                                            });
                                        }

                                        @Override
                                        public void onAuthFailure() {

                                        }

                                        @Override
                                        public void onFailure() {
                                            Loader.getInstance().hideLoader();
                                            AppDialog.showAlertDialog(activity, "Failed to engage the Lock. Please try again.");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onAuthFailure() {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
                            }

                            @Override
                            public void onFailure() {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(activity, "Failed to engage the Lock. Please try again.");
                            }

                        });

                    }

                    @Override
                    public void onServiceDisconnected() {
                        Loader.getInstance().hideLoader();
                        //AppDialog.showAlertDialog(activity, "Lock service disconnected. Please connect again.");
                    }

                });

            }

            @Override
            public void onAuthFailure() {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
            }

            @Override
            public void onDeviceDisconnected() {

                if (!BleManager.getInstance().isDeviceConnected()) {
                    listener.onDeviceNotConnected();
                } else {
                    Loader.getInstance().hideLoader();
                }
                stopDisconnectScheduler();
            }

            @Override
            public void onError(String error) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(activity, error);
            }

            @Override
            public void onLocationPermissionError(String error) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(activity, error);
            }

            @Override
            public void onPermissionError(String error) {
                Loader.getInstance().hideLoader();
                BleManager.getInstance().checkLocationPermission(activity);
            }

            @Override
            public void onUpdateMacAddress(String ssid, String macAddress) {
                if (listener != null) {
                    listener.onMacAddressUpdate(ssid, macAddress);
                }
            }

        });
    }

    public String preCheckBLE(Activity activity) {

        if (!BleManager.getInstance().isBluetoothSupported()) {
            return "Your device does not support Bluetooth functionality. Do not worry. You can still  engage in the Wi Fi Mode. :-)";
        }

        if (!BleManager.getInstance().isBleSupported(activity)) {
            return "Your device does not support Bluetooth functionality. Do not worry. You can still  engage in the Wi Fi Mode. :-)";
        }

        if (!BleManager.getInstance().isBluetoothEnabled()) {
            return "Please ensure your phone Bluetooth is turned ON.";
        }

        if (!BleManager.getInstance().isLocationEnabled(activity)) {
            return "Please enable the location to scan bluetooth devices. To enable location identification, go to Settings>Connections>Location";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WifiManager wifiManager = (WifiManager) ((Context) activity).getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                return "Please ensure your phone WiFi is turned ON";
            }
        }

        return "success";
    }

    public String preCheckBLEVersion6(Activity activity) {

        if (!BleManager.getInstance().isLocationEnabled(activity)) {
            return "Please enable the location to scan wifi. To enable location identification, go to Settings>Connections>Location";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WifiManager wifiManager = (WifiManager) ((Context) activity).getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                return "Please ensure your phone WiFi is turned ON";
            }
        }

        return "success";
    }

    private void setDateTime(final Activity activity, final BluetoothGattService service, final IKeyListener listener)  {

        Logger.d("setDate()");
        final String charId = "FF04";
        String dateString = DateTimeUtils.getCurrentGMTTime(DDMMYY_HHMMSS);
        String[] dateAndTime = dateString.split(" ");
        // final String data = "29-06-18\0";
        BleManager.getInstance().setBleData(service, charId, dateAndTime[0], new ICharCallBack() {
            @Override
            public void readCallBack(String data) {

            }

            @Override
            public void writeCallback(String result) {
                Logger.d("Date Set Successfully");
                final String charId = "FF05";
                BleManager.getInstance().setBleData(service, charId, dateAndTime[1], new ICharCallBack() {
                    @Override
                    public void readCallBack(String data) {

                    }

                    @Override
                    public void writeCallback(String data) {
                        Logger.d("Time Set Successfully");
                        getLogsBeforeEngage(activity, service, listener);
                    }

                    @Override
                    public void onAuthFailure() {

                    }

                    @Override
                    public void onFailure() {
                        AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
                    }
                });
            }

            @Override
            public void onAuthFailure() {

            }

            @Override
            public void onFailure() {
                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
            }
        });

    }

        private void engageLock(final Activity activity, final BluetoothGattService service, final IKeyListener listener) {

            if (listener == null) return;
            final String charId = "FF0E";
            String data = "BB";

            BleManager.getInstance().setBleHexData(service, charId, data, new ICharCallBack() {
                @Override
                public void readCallBack(String data) {

                }

                @Override
                public void writeCallback(String data) {
                    BleManager.getInstance().setDeviceConnected(true);
                    listener.onLockActivated();
                    /*Loader.getInstance().hideLoader();*/
                    getBatteryStatus(activity, service, listener);
                }

                @Override
                public void onAuthFailure() {
                    Loader.getInstance().hideLoader();
                    AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
                }

                @Override
                public void onFailure() {
                    Loader.getInstance().hideLoader();
                    AppDialog.showAlertDialog(activity, "Failed to engage the Lock. Please try again.");
                }
            });
        }

    private void getBatteryStatus(final Activity activity, final BluetoothGattService service, final IKeyListener listener) {

        final String charId = "FF15";

        BleManager.getInstance().getBleData(service, charId, new ICharCallBack() {
            @Override
            public void readCallBack(String data) {
                Logger.d("Battery Status ->" + data);
                if (listener != null) {
                    listener.onBatteryUpdate(data);
                }
                getLogs(activity, service, listener);
            }

            @Override
            public void writeCallback(String data) {

            }

            @Override
            public void onAuthFailure() {

            }

            @Override
            public void onFailure() {
                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
            }
        });
    }

    private void getLogsBeforeEngage(final Activity activity, final BluetoothGattService service, final IKeyListener listener) {
        alLogs = new ArrayList<>();
        getActivityHistoryBeforeEngage(activity, service, alLogs.size(), listener);
    }

    private void getActivityHistoryBeforeEngage(final Activity activity, final BluetoothGattService service, int data, final IKeyListener listener) {

        Logger.d("getActivityHistory() data->" + data);
        final String charId = "FF07";

        BleManager.getInstance().getBleData(service, charId, new ICharCallBack() {

            @Override
            public void readCallBack(String data) {

                Logger.d("ActivityHistory data->" + data);

                if ((!data.equalsIgnoreCase("000000000000000000000000000000000000000000"))
                        && (!data.equalsIgnoreCase("303030303030303030303030303030303030303030"))
                        && (alLogs.size() + 1) < TOTAL_LOGS) {

                    alLogs.add(HexUtils.hexToString(data));
                    //Call recursive function until all keys fetched.
                    Logger.d("All logs Size : " + alLogs.size());
                    Logger.d("Total Size : " + TOTAL_LOGS);
                    if (alLogs.size() < TOTAL_LOGS) {
                        getActivityHistoryBeforeEngage(activity, service, alLogs.size(), listener);
                    }

                } else {
                    Logger.d("All logs fetched.");
                    engageLock(activity, service, listener);
                }

            }

            @Override
            public void writeCallback(String data) {

            }

            @Override
            public void onAuthFailure() {

            }

            @Override
            public void onFailure() {
                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
            }
        });

    }

    private void getLogs(final Activity activity, final BluetoothGattService service, final IKeyListener listener) {
        //alLogs = new ArrayList<>();
        getActivityHistory(activity, service, alLogs.size(), listener);
    }

    private void getActivityHistory(final Activity activity, final BluetoothGattService service, int data, final IKeyListener listener) {

        Logger.d("getActivityHistory() data->" + data);
        final String charId = "FF07";

        BleManager.getInstance().getBleData(service, charId, new ICharCallBack() {

            @Override
            public void readCallBack(String data) {

                Logger.d("ActivityHistory data->" + data);

                if ((!data.equalsIgnoreCase("000000000000000000000000000000000000000000"))
                        && (!data.equalsIgnoreCase("303030303030303030303030303030303030303030"))

                        && (alLogs.size() + 1) < TOTAL_LOGS) {

                    alLogs.add(HexUtils.hexToString(data));
                    //Call recursive function until all keys fetched.
                    Logger.d("All logs Size : " + alLogs.size());
                    Logger.d("Total Size : " + TOTAL_LOGS);
                    if (alLogs.size() < TOTAL_LOGS) {
                        getActivityHistory(activity, service, alLogs.size(), listener);
                    }

                } else {

                    Logger.d("All logs fetched.");
                    listener.onAccessLog(alLogs);
                    //engageLock(activity, service, listener);
                    BleManager.getInstance().disConnectDevice();
                    Loader.getInstance().hideLoader();

                }
            }

            @Override
            public void writeCallback(String data) {

            }

            @Override
            public void onAuthFailure() {

            }

            @Override
            public void onFailure() {
                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
            }
        });
    }

    public void reWriteSlot(final Activity activity, final String address, final String ssid, final String key,
                            final int slotNumber, final IKeyListener listener) {
        Loader.getInstance().showLoader(activity);
        BleManager.getInstance().stopScan();
        final String serviceId = "00FF";
        BleManager.getInstance().setDeviceConnected(false);
        startDisconnectScheduler();
        BleManager.getInstance().connectDevice(activity, address, ssid, new IDeviceCallBack() {
            @Override
            public void onDeviceConnected() {
                stopDisconnectScheduler();
            }

            @Override
            public void onServiceDiscovered() {
                BleManager.getInstance().connectService(serviceId, new IServiceCallBack() {
                    @Override
                    public void onServiceConnected(final BluetoothGattService service) {
                        // Authenticate the lock
                        final String charId = "FF10";
                        BleManager.getInstance().setBleHexData(service, charId, key, new ICharCallBack() {
                            @Override
                            public void readCallBack(String result) {
                                Logger.d("New Key ->" + result);
                            }

                            @Override
                            public void writeCallback(String result) {
                                rewriteSlotKeys(activity, service, slotNumber, listener);
                            }

                            @Override
                            public void onAuthFailure() {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
                            }

                            @Override
                            public void onFailure() {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
                            }
                        });
                    }

                    @Override
                    public void onServiceDisconnected() {
                        Loader.getInstance().hideLoader();
                        //AppDialog.showAlertDialog(activity, "Lock service disconnected. Please connect again.");


                    }
                });
            }

            @Override
            public void onAuthFailure() {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
            }

            @Override
            public void onDeviceDisconnected() {

                if (!BleManager.getInstance().isDeviceConnected()) {
                    if (listener != null) {
                        listener.onDeviceNotConnected();
                    } else {
                        Loader.getInstance().hideLoader();
                    }
                } else {
                    Loader.getInstance().hideLoader();
                }
                stopDisconnectScheduler();
            }

            @Override
            public void onError(String error) {
                AppDialog.showAlertDialog(activity, error);
            }

            @Override
            public void onLocationPermissionError(String error) {
                AppDialog.showAlertDialog(activity, error);
            }

            @Override
            public void onPermissionError(String error) {
                BleManager.getInstance().checkLocationPermission(activity);
            }

            @Override
            public void onUpdateMacAddress(String ssid, String macAddress) {
            }
        });
    }

    private void rewriteSlotKeys(final Activity activity, final BluetoothGattService service,
                                 final int slotNumber, final IKeyListener listener) {
        final String charId = "FF0C";
        BleManager.getInstance().setBleIntData(service, charId, slotNumber, new ICharCallBack() {
            @Override
            public void readCallBack(String data) {

            }

            @Override
            public void writeCallback(String data) {
                final String charId = "FF02";
                BleManager.getInstance().setBleIntData(service, charId, slotNumber, new ICharCallBack() {
                    @Override
                    public void readCallBack(String data) {

                    }

                    @Override
                    public void writeCallback(String data) {
                        // Read new Key
                        final String charId = "FF13";
                        BleManager.getInstance().getBleData(service, charId, new ICharCallBack() {
                            @Override
                            public void readCallBack(String result) {
                                BleManager.getInstance().setDeviceConnected(true);
                                Logger.d("New Key ->" + result);
                                ArrayList<String> alKey = new ArrayList<>();
                                alKey.add(result);
                                listener.onLockKeys(alKey);
                                BleManager.getInstance().disConnectDevice();
                                Loader.getInstance().hideLoader();
                            }

                            @Override
                            public void writeCallback(String result) {

                            }

                            @Override
                            public void onAuthFailure() {

                            }

                            @Override
                            public void onFailure() {
                                AppDialog.showAlertDialog(activity, "Failed to read Lock data. Please try again.");
                            }
                        });
                    }

                    @Override
                    public void onAuthFailure() {
                        Loader.getInstance().hideLoader();
                        AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
                    }

                    @Override
                    public void onFailure() {
                        Loader.getInstance().hideLoader();
                        AppDialog.showAlertDialog(activity, "Failed to engage the Lock. Please try again.");
                    }
                });
            }

            @Override
            public void onAuthFailure() {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
            }

            @Override
            public void onFailure() {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(activity, "Failed to engage the Lock. Please try again.");
            }
        });
    }

    protected void factoryReset(final Activity activity, final String address,
                                final String ssid, final String key, final IKeyListener listener) {
        Loader.getInstance().showLoader(activity);
        BleManager.getInstance().stopScan();
        BleManager.getInstance().setDeviceConnected(false);
        startDisconnectScheduler();
        final String serviceId = "00FF";
        BleManager.getInstance().connectDevice(activity, address, ssid, new IDeviceCallBack() {
            @Override
            public void onDeviceConnected() {

                stopDisconnectScheduler();
            }

            @Override
            public void onServiceDiscovered() {


                BleManager.getInstance().connectService(serviceId, new IServiceCallBack() {
                    @Override
                    public void onServiceConnected(final BluetoothGattService service) {
                        // Authenticate the lock
                        final String charId = "FF10";


                        BleManager.getInstance().setBleHexData(service, charId, key, new ICharCallBack() {
                            @Override
                            public void readCallBack(String data) {

                            }

                            @Override
                            public void writeCallback(String data) {
                                //Do Factory reset
                                final String charId = "FF16";
                                data = "DD";
                                BleManager.getInstance().setBleHexData(service, charId, data, new ICharCallBack() {
                                    @Override
                                    public void readCallBack(String data) {

                                    }

                                    @Override
                                    public void writeCallback(String data) {
                                        if (listener != null) {
                                            BleManager.getInstance().setDeviceConnected(true);
                                            listener.onLockActivated();
                                        }
                                    }

                                    @Override
                                    public void onAuthFailure() {
                                        Loader.getInstance().hideLoader();
                                        AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
                                    }

                                    @Override
                                    public void onFailure() {
                                        Loader.getInstance().hideLoader();
                                        AppDialog.showAlertDialog(activity, "Failed to reset the Lock. Please try again.");
                                    }
                                });


                            }

                            @Override
                            public void onAuthFailure() {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
                            }

                            @Override
                            public void onFailure() {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(activity, "Failed to reset the Lock. Please try again.");
                            }
                        });

                    }

                    @Override
                    public void onServiceDisconnected() {
                        Loader.getInstance().hideLoader();
                    }
                });
            }

            @Override
            public void onAuthFailure() {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(activity, "Unauthorized Access. Please connect with valid key.");
            }

            @Override
            public void onDeviceDisconnected() {
                if (!BleManager.getInstance().isDeviceConnected()) {
                    if (listener != null) {
                        listener.onDeviceNotConnected();
                    } else {
                        Loader.getInstance().hideLoader();
                    }
                } else {
                    Loader.getInstance().hideLoader();
                }
                stopDisconnectScheduler();
            }

            @Override
            public void onError(String error) {
                AppDialog.showAlertDialog(activity, error);
            }

            @Override
            public void onLocationPermissionError(String error) {
                AppDialog.showAlertDialog(activity, error);
            }

            @Override
            public void onPermissionError(String error) {
                BleManager.getInstance().checkLocationPermission(activity);
            }

            @Override
            public void onUpdateMacAddress(String ssid, String macAddress) {
            }

        });
    }

    private void showWifiSettingsConfirmDialog(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppDialog.showAlertDialog(activity, activity.getResources().getString(R.string.app_name), activity.getResources().getString(R.string.ble_not_found), "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }, "NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
            }
        });
    }

    private Handler handler = null;
    private Runnable runnable = null;

    private void startDisconnectScheduler() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!BleManager.getInstance().isDeviceConnected()) {
                    BleManager.getInstance().forceStop();
                }
            }
        };
        handler.postDelayed(runnable, MAX_SCAN_TIME);
    }

    private void stopDisconnectScheduler() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

}
