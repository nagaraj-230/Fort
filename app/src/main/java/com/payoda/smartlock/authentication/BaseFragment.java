package com.payoda.smartlock.authentication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.payoda.smartlock.utils.Logger;

 /**
 * Created by david on 14/09/18.
 */

public class BaseFragment extends Fragment {

    private RequestPermissionAction onPermissionCallBack;
    private final int REQUEST_LOCATION_PERMISSION=1;
    private final int REQUEST_BATTERY_OPT_PERMISSION=2;
    private final int REQUEST_READ_CONTACT_PERMISSION=3;
    private final int REQUEST_BLUETOOTH_SCAN_CONNECT_PERMISSION=4;
    private final int REQUEST_POST_NOTIFICATION=5;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private int requestCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(new
                ActivityResultContracts.RequestMultiplePermissions(), result -> {

            if (result.containsValue(false)) {
                // Permission denied
                if (REQUEST_LOCATION_PERMISSION == requestCode) {
                    Logger.d("REQUEST_LOCATION_PERMISSION Permission Denied");
                }
                else if (REQUEST_BLUETOOTH_SCAN_CONNECT_PERMISSION == requestCode) {
                    Logger.d("REQUEST_BLUETOOTH_SCAN_PERMISSION Permission Denied");
                }
                else if (REQUEST_POST_NOTIFICATION == requestCode) {
                    Logger.d("REQUEST_POST_NOTIFICATION Permission Denied");
                }
                if (onPermissionCallBack != null) {
                    onPermissionCallBack.permissionDenied();
                }

            } else {

                // All permissions granted
                if (REQUEST_LOCATION_PERMISSION == requestCode) {
                    Logger.d("REQUEST_LOCATION_PERMISSION Permission Granted");
                }
                else if (REQUEST_BLUETOOTH_SCAN_CONNECT_PERMISSION == requestCode) {
                        Logger.d("REQUEST_BLUETOOTH_SCAN_PERMISSION Permission Granted");
                }
                else if (REQUEST_POST_NOTIFICATION == requestCode) {
                    Logger.d("REQUEST_POST_NOTIFICATION Permission Denied");
                }
                if (onPermissionCallBack != null) {
                    onPermissionCallBack.permissionGranted();
                }

            }
        });


    }

    public boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Check Read Contact Permission is enabled
     *
     * @param onPermissionCallBack
     */
    public void getReadContactPermission(RequestPermissionAction onPermissionCallBack) {

        this.onPermissionCallBack = onPermissionCallBack;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(Manifest.permission.READ_CONTACTS)) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_CONTACTS});
                requestCode = REQUEST_READ_CONTACT_PERMISSION;
                return;
            }
        }
        if (onPermissionCallBack != null)
            onPermissionCallBack.permissionGranted();
    }

    /**
     * Check Read SMS Permission is enabled
     *
     * @param onPermissionCallBack
     */
    public void getLocationPermission(RequestPermissionAction onPermissionCallBack) {

        this.onPermissionCallBack = onPermissionCallBack;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                requestCode = REQUEST_LOCATION_PERMISSION;
                return;
            }
        }

        if (onPermissionCallBack != null) {
            onPermissionCallBack.permissionGranted();
        }

    }

    /**
     * Check Bluetooth Scan and Connect Permission
     *
     * @param onPermissionCallBack
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void getBluetoothScanConnectPermission(RequestPermissionAction onPermissionCallBack) {
        this.onPermissionCallBack = onPermissionCallBack;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(Manifest.permission.BLUETOOTH_SCAN) || !checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT});
                requestCode = REQUEST_BLUETOOTH_SCAN_CONNECT_PERMISSION;
                return;
            }
        }
        if (onPermissionCallBack != null)
            onPermissionCallBack.permissionGranted();
    }

    /**
     * Check Battery Optimization Callback
     *
     * @param onPermissionCallBack
     */
    public void getBatteryOptPermission(RequestPermissionAction onPermissionCallBack) {
        this.onPermissionCallBack = onPermissionCallBack;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS});
                requestCode = REQUEST_BATTERY_OPT_PERMISSION;
                return;
            }
        }
        if (onPermissionCallBack != null)
            onPermissionCallBack.permissionGranted();
    }

    /**
     * Check Notification Callback
     *
     * @param onPermissionCallBack
     */
    public void getPostNotificationPermission(RequestPermissionAction onPermissionCallBack) {
        this.onPermissionCallBack = onPermissionCallBack;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.POST_NOTIFICATIONS});
                requestCode = REQUEST_POST_NOTIFICATION;
                return;
            }
        }
        if (onPermissionCallBack != null)
            onPermissionCallBack.permissionGranted();
    }

    public interface RequestPermissionAction {
        void permissionDenied();

        void permissionGranted();
    }


}
