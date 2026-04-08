package com.payoda.smartlock.plugins.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class BleManager {

    private static final String TAG = BleManager.class.getSimpleName();
    private static BleManager mInstance;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private SingBroadcastReceiver mReceiver;

    private boolean isBleScanning;
    private BleResponseHandler mBleResponseHandler;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 8 * 1000; // 1 minute

    public static String MANUFACTURER_CODE  = BuildConfig.MANUFACTURER_CODE; ;

    public static synchronized BleManager getInstance() {

        if (mInstance == null) {

            mInstance = new BleManager();
            BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
            if (brandInfo != null && !brandInfo.getManufacturerCode().isEmpty()) {
                MANUFACTURER_CODE = brandInfo.getManufacturerCode();
            }
        }

        return mInstance;
    }

    public boolean isBluetoothSupported() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null;
    }

    public boolean isBleSupported(Context context) {
        try {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    //method to enable bluetooth
    public void enableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    public boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isLocationPermissionEnabled(Context activity) {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private static final int REQUEST_LOCATION = 13;

    public boolean checkLocationPermission(Activity activity) {


        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (result == PackageManager.PERMISSION_DENIED) {
                showLocationDisabledDialog(activity);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                }
            }
        }
        return false;
    }

    private static final int REQUEST_BLUETOOTH_SCAN_CONNECT = 14;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public boolean checkBluetoothScanPermission(Activity activity) {
        int scan_result = ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN);
        int connect_result = ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT);
        if (scan_result == PackageManager.PERMISSION_GRANTED && connect_result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (scan_result == PackageManager.PERMISSION_DENIED || connect_result == PackageManager.PERMISSION_DENIED) {
                showBluetoothScanConnectDisabledDialog(activity);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    activity.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,}, REQUEST_BLUETOOTH_SCAN_CONNECT);
                }
            }
        }
        return false;
    }

    /**
     *
     */
    private void showLocationDisabledDialog(final Context context) {
        AppDialog.showAlertDialog(context, "Permission Denied", "Please enable location permission in app settings to scan the bluetooth devices.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppPermissionSettings(context);
            }
        });
    }

    /**
     *
     */
    private void showBluetoothScanConnectDisabledDialog(final Context context) {
        AppDialog.showAlertDialog(context, "Permission Denied", "Please enable the nearby devices permission in app settings to scan and connect the bluetooth devices.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppPermissionSettings(context);
            }
        });
    }

    /**
     * Method to launch app permissions to enable
     */
    private void openAppPermissionSettings(Context context) {
        final Intent mIntent = new Intent();
        mIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        mIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mIntent.setData(Uri.parse("package:" + context.getPackageName()));
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(mIntent);
    }

    //method to disable bluetooth

    public void disableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    private BluetoothAdapter getBleAdapter(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null)
                return bluetoothManager.getAdapter();
        }
        return null;
    }

    public void startScan(Context context, final BleResponseHandler handler) {
        if (handler == null) return;
        
        mBleResponseHandler = handler;

        mBluetoothAdapter = getBleAdapter(context);

        if (mBluetoothAdapter == null) {
            mBleResponseHandler.onError("Bluetooth not supported.");
            return;
        }

        if (isBluetoothEnabled()) {
            if (mHandler != null) {
                mHandler = null;
            }
            mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopLeScan();
                    mBleResponseHandler.onTimeOut();
                }
            }, SCAN_PERIOD);
            isBleScanning = startLeScan();
            if (!isBleScanning) {
                mBleResponseHandler.onError("BLE not supported");
            }
        } else {
            mBleResponseHandler.onError("Bluetooth not enabled.");
        }
    }

    public void stopScan() {

        Logger.d("stopScan()");
        isBleScanning = false;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopLeScan();
            }
        });

        if (mHandler != null) {
            mHandler = null;
        }
    }

    public void forceStop() {

        stopScan();
        if (iDeviceCallBack != null) {
            iDeviceCallBack.onDeviceDisconnected();
        }

        disConnectDevice();

    }

    private boolean startLeScan() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && mBluetoothAdapter != null) {

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    mBluetoothAdapter.enable();
                    //mBluetoothAdapter.startDiscovery();
                    mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                        ArrayList<BleDevice> mDeviceList = new ArrayList<>();

                        @Override
                        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {

                            if (device != null && device.getName() != null && device.getName().contains(MANUFACTURER_CODE)) {
                                BleDevice mDevice;
                                mDevice = new BleDevice(device.getName(), device.getAddress(), device.getBondState(), device.getType(), device.getUuids());
                                mDevice.setName(mDevice.getName().replace(MANUFACTURER_CODE, ""));
                                for (BleDevice tempDevice : mDeviceList) {
                                    if (tempDevice.getAddress() != null && tempDevice.getAddress().equalsIgnoreCase(device.getAddress())) {
                                        return;
                                    }
                                }
                                mDeviceList.add(mDevice);
                                mBleResponseHandler.onScanResult(mDeviceList);
                            }
                        }
                    };
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            });

            return true;
        } else {
            return false;
        }
    }

    private boolean stopLeScan() {

        Logger.d("stopLeScan()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter = null;
            Logger.d("stopLeScan() stopped");
            return true;
        } else {
            return false;
        }

    }

    public void connectDevice(Activity activity, String address, String ssid, IDeviceCallBack callback) {
        connectDevice(activity, address, ssid, callback, false);
    }

    private IDeviceCallBack iDeviceCallBack;
    private boolean isDeviceConnected;

    public boolean isDeviceConnected() {
        return isDeviceConnected;
    }

    public void setDeviceConnected(boolean deviceConnected) {
        isDeviceConnected = deviceConnected;
    }

    public void connectDevice(Activity activity, String address, String ssid, IDeviceCallBack callback, boolean isAutoConnect) {

        Logger.d("connectDevice()");
        if (callback == null) return;
        this.iDeviceCallBack = callback;

        /*if (!isBluetoothSupported()) {
            iDeviceCallBack.onError("Bluetooth not supported");
            return;
        }

        if (!isBleSupported(activity)) {
            iDeviceCallBack.onError("BLE not supported");
            return;
        }

        if (!isBluetoothEnabled()) {
            iDeviceCallBack.onError("Please Turn ON bluetooth and try again.");
            return;
        }

        if (!isLocationEnabled(activity)) {
            iDeviceCallBack.onLocationPermissionError("Please enable location to scan Bluetooth devices.");
            return;
        }

        if (!isLocationPermissionEnabled(activity)) {
            iDeviceCallBack.onPermissionError("Location permission not enabled.");
            return;
        }*/

        BluetoothAdapter mBluetoothAdapter = getBleAdapter(activity);
        if (mBluetoothAdapter != null) {
            try {
                Handler handler = new Handler(activity.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (address != null && !address.equalsIgnoreCase("0")) {
                            BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
                            if (mBluetoothDevice != null) {
                                mBluetoothGatt = mBluetoothDevice.connectGatt(activity, isAutoConnect, mGattCallback);
                            } else {
                                AppDialog.showAlertDialog(activity, "Unable to find the device.");
                            }

                        } else {
                            if (mBluetoothAdapter.isDiscovering()) {
                                mBluetoothAdapter.cancelDiscovery();
                            }
                            mBluetoothAdapter.startDiscovery();

                            // Discover new devices
                            mReceiver = new SingBroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    String action = intent.getAction();
                                    // When discovery finds a device
                                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                                        // Get the bluetoothDevice object from the Intent
                                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                        if (ssid.equalsIgnoreCase(device.getName())) {
                                            mBluetoothAdapter.cancelDiscovery();
                                            // Don't forget to unregister the ACTION_FOUND receiver.
                                            activity.unregisterReceiver(mReceiver);
                                            if (device != null) {

                                                if (iDeviceCallBack != null) {
                                                    iDeviceCallBack.onUpdateMacAddress(ssid, device.getAddress());
                                                }
                                                mBluetoothGatt = device.connectGatt(activity, isAutoConnect, mGattCallback);

                                            } else {
                                                AppDialog.showAlertDialog(activity, "Unable to find the device.");
                                            }
                                        } else {
                                            Logger.d("### onReceive ble not find");
                                        }
                                    }

                                    else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                                        mBluetoothAdapter.cancelDiscovery();
                                        // Don't forget to unregister the ACTION_FOUND receiver.
                                        activity.unregisterReceiver(mReceiver);
                                    }
                                }
                            };

                            //let's make a broadcast receiver to register our things
                            IntentFilter iFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                            iFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            activity.registerReceiver(mReceiver, iFilter);

                        }
                    }
                });


            } catch (Exception e) {
                iDeviceCallBack.onError("Bluetooth not connected.");
            }

        } else {
            AppDialog.showAlertDialog(activity, "Unable to find bluetooth device.");
        }
    }

    public boolean disConnectDevice() {

        Logger.d("disConnectDevice()");

        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt = null;
            return true;
        }

        return false;

    }

    private IServiceCallBack iService;

    public boolean connectService(String serviceId, IServiceCallBack callback) {

        Logger.d("connectService()");
        if (callback == null) return false;
        this.iService = callback;

        if (mBluetoothGatt != null) {

            BluetoothGattService service = mBluetoothGatt.getService(generateUUID(serviceId));

            if (service != null) {
                iService.onServiceConnected(service);
                return true;
            } else {
                iService.onServiceDisconnected();
                Logger.d("connectService()-> Service not found.");
            }

        }

        return false;
    }

    private ICharCallBack iCharCallBack;

    public void getBleData(BluetoothGattService service, String charId, ICharCallBack callback) {
        if (callback == null) return;
        if (mBluetoothGatt == null) {
            if (iDeviceCallBack != null)
                iDeviceCallBack.onDeviceDisconnected();
        }
        this.iCharCallBack = callback;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(generateUUID(charId));
        if (characteristic != null) {
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                mBluetoothGatt.readCharacteristic(characteristic);
            }
            /*if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            }*/
        } else {
            Logger.d("getBleData()-> Characteristic not found.");
        }
    }

    public void setBleData(BluetoothGattService service, String charId, String data, ICharCallBack callback) {
        if (callback == null) return;

        if (mBluetoothGatt == null) {
            if (iDeviceCallBack != null)
                iDeviceCallBack.onDeviceDisconnected();
        }

        this.iCharCallBack = callback;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(generateUUID(charId));

        if (characteristic != null) {

            final int charaProp = characteristic.getProperties();

            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {

                if (data != null) {

                    byte[] value = data.getBytes();
                    boolean status = characteristic.setValue(value);
                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                    Logger.d("Statussss ");
                    //characteristic.setValue(value);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    //characteristic.setValue(URLEncoder.encode(data, "utf-8"));
                    boolean val = mBluetoothGatt.writeCharacteristic(characteristic);
                    //Logger.d("writeCharacteristic "+val+"");
                }
            }
            /*if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            }*/
        } else {
            Logger.d("setBleData()-> Characteristic not found.");
        }
    }

    public void setBleHexData(BluetoothGattService service, String charId, String data, ICharCallBack callback) {

        if (callback == null) return;
        if (mBluetoothGatt == null) {
            if (iDeviceCallBack != null)
                iDeviceCallBack.onDeviceDisconnected();
        }
        this.iCharCallBack = callback;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(generateUUID(charId));

        if (characteristic != null) {
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                if (data != null) {

                    int len = data.length();
                    byte[] dataArray = new byte[len / 2];
                    for (int i = 0; i < len; i += 2) {
                        dataArray[i / 2] = (byte) ((Character.digit(data.charAt(i), 16) << 4)
                                + Character.digit(data.charAt(i + 1), 16));
                    }
                    characteristic.setValue(dataArray);

                    mBluetoothGatt.writeCharacteristic(characteristic);
                }
            }
            /*if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            }*/
        } else {
            Logger.d("setBleData()-> Characteristic not found.");
        }

    }

    public void setBleIntData(BluetoothGattService service, String charId, int data, ICharCallBack callback) {
        if (callback == null) return;
        this.iCharCallBack = callback;
        if (mBluetoothGatt == null) {
            if (iDeviceCallBack != null)
                iDeviceCallBack.onDeviceDisconnected();
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(generateUUID(charId));
        if (characteristic != null) {
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                BigInteger bigInt = BigInteger.valueOf(data);
                characteristic.setValue(bigInt.toByteArray());
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            /*if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            }*/
        } else {
            Logger.d("setBleData()-> Characteristic not found.");
        }
    }

    private String readCharacteristics(BluetoothGattCharacteristic characteristic) {
        final byte[] dataBytes = characteristic.getValue();
        if (dataBytes != null && dataBytes.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(dataBytes.length);
            for (byte byteChar : dataBytes)
                stringBuilder.append(String.format("%02X", byteChar));
            return stringBuilder.toString();
            //return new String(dataBytes) + "\n" + stringBuilder.toString();
        }
        return null;
    }

    private UUID generateUUID(String key) {
        return UUID.fromString("0000" + key + "-0000-1000-8000-00805f9b34fb");
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logger.d("onConnectionStateChange()");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                mBluetoothGatt = gatt;
                if (iDeviceCallBack != null) {
                    iDeviceCallBack.onDeviceConnected();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disConnectDevice();
                if (iDeviceCallBack != null) {
                    iDeviceCallBack.onDeviceDisconnected();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.d("onServicesDiscovered()");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGatt = gatt;
                if (iDeviceCallBack != null) {
                    iDeviceCallBack.onServiceDiscovered();
                }
            }
            else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (iDeviceCallBack != null) {
                    iDeviceCallBack.onAuthFailure();
                }
            }
            else if (status == BluetoothGatt.GATT_FAILURE) {
                if (iDeviceCallBack != null) {
                    iDeviceCallBack.onDeviceDisconnected();
                }
            }
            else {
                if (iDeviceCallBack != null) {
                    iDeviceCallBack.onDeviceDisconnected();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Logger.d("onCharacteristicRead()");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String data = readCharacteristics(characteristic);
                if (iCharCallBack != null) {
                    iCharCallBack.readCallBack(data);
                }
                Logger.d("onCharacteristicRead() Characteristic data->" + data);
            }
            else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (iCharCallBack != null) {
                    iCharCallBack.onAuthFailure();
                }
                Logger.d("onCharacteristicRead()-> Auth Failure.");
            }
            else if (status == BluetoothGatt.GATT_FAILURE) {
                if (iCharCallBack != null) {
                    iCharCallBack.onFailure();
                }
                Logger.d("onCharacteristicRead()-> Failed to read Characteristic.");
            }
            else {
                if (iCharCallBack != null) {
                    iCharCallBack.onFailure();
                }
                Logger.d("onCharacteristicRead()-> Unknown Characteristic status->" + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.d("onCharacteristicWrite()");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String data = readCharacteristics(characteristic);
                if (iCharCallBack != null) {
                    iCharCallBack.writeCallback(data);
                }
                Logger.d("onCharacteristicWrite() Characteristic data->" + data);
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (iCharCallBack != null) {
                    iCharCallBack.onAuthFailure();
                }
                Logger.d("onCharacteristicWrite()-> Auth Failure..");
            }
            else if (status == 8) {
                if (iCharCallBack != null) {
                    iCharCallBack.onAuthFailure();
                }
                Logger.d("onCharacteristicWrite()-> Auth Failure..");
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                if (iCharCallBack != null) {
                    iCharCallBack.onFailure();
                }
                Logger.d("onCharacteristicWrite()-> Failed to write Characteristic.");
            }
            else {
                if (iCharCallBack != null) {
                    iCharCallBack.onFailure();
                }
                Logger.d("onCharacteristicWrite() Unknown Characteristic status->" + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            String data = readCharacteristics(characteristic);
            Logger.d("onCharacteristicChanged() Characteristic data->" + data);
        }
    };

    private class SingBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
        }
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

        Logger.d("### updateManufactureCode ble ");

        if (mLock != null ) {

            if (!mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_6_0)) {
                BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
                brandInfo.setManufacturerCode(Constant.DEFAULT_MANUFACTURE_CODE);
                Logger.d("### updateManufactureCode Ble brandInfo " + brandInfo);
                SecuredStorageManager.getInstance().setBrandInfo(brandInfo);
                 MANUFACTURER_CODE = brandInfo.getManufacturerCode();
            }
        }
    }





}