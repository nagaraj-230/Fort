package com.payoda.smartlock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.authentication.service.DeleteAccountService;
import com.payoda.smartlock.authentication.service.LogoutService;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.fp.model.FingerPrints;
import com.payoda.smartlock.history.model.DeviceLog;
import com.payoda.smartlock.locks.LockListFragment;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeyList;
import com.payoda.smartlock.managepins.model.OtpRequest;
import com.payoda.smartlock.managepins.model.PinRequest;
import com.payoda.smartlock.model.DeviceTokenModel;
import com.payoda.smartlock.model.LockVersionConfig;
import com.payoda.smartlock.model.MenuItems;
import com.payoda.smartlock.model.RemoteAccessModel;
import com.payoda.smartlock.model.VersionControlModel;
import com.payoda.smartlock.notification.NotificationFragment;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.network.SyncAll;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.request.RequestFragment;
import com.payoda.smartlock.request.model.Request;
import com.payoda.smartlock.request.model.RequestAccept;
import com.payoda.smartlock.request.service.RequestService;
import com.payoda.smartlock.service.VersionControlService;
import com.payoda.smartlock.users.model.RevokeUserList;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.AppUtils;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.InactiveTimeoutUtil;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.payoda.smartlock.constants.Constant.ANDROID_FORCE_UPDATE;
import static com.payoda.smartlock.constants.Constant.ANDROID_FORCE_UPDATE_MESSAGE;
import static com.payoda.smartlock.constants.Constant.ANDROID_VERSION_CODE;
import static com.payoda.smartlock.utils.InactiveTimeoutUtil.SESSION_TIMEOUT;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        InactiveTimeoutUtil.TimeOutListener {

    public static final String TAG = "### NavigationActivity";

    private ArrayList<MenuItems> tabItems;
    private TextView tvUserName;
    private RequestPermissionAction onPermissionCallBack;
    private final int REQUEST_POST_NOTIFICATION = 5;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private int requestCode;

    boolean isPermissionIsShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        OnBackPressedDispatcher();

//
//        // 1. Force the system to keep content below the status bar
//        getWindow().setDecorFitsSystemWindows(true);
//
//        // 2. Add this snippet to handle the "Safe Area" for modern Android versions
//        View mainView = findViewById(R.id.drawer_layout); // or your root layout ID
//        if (mainView != null) {
//            mainView.setOnApplyWindowInsetsListener((v, insets) -> {
//                // Get the height of the status bar (top inset)
//                int statusBarHeight = insets.getSystemWindowInsetTop();
//
//                // Apply it as padding to the top of your layout
//                // This prevents the header from 'hitting' the status bar
//                v.setPadding(0, statusBarHeight, 0, 0);
//
//                return insets.consumeSystemWindowInsets();
//            });
//        }

        Logger.d(TAG, TAG);
        permissionResultLauncher();
        Logger.d("### Nav Activity on create");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menuNav = navigationView.getMenu();
        menuNav.findItem(R.id.nav_factory_reset).setVisible(BuildConfig.FACTORY_RESET_VISIBILITY);
        navigationView.setNavigationItemSelectedListener(this);


        showDialogForRemoteAccess();
        initTabItems();

        final ViewPager2 mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(this));

        TabLayout tabLayout = findViewById(R.id.tabs);
        new TabLayoutMediator(tabLayout, mViewPager, (tab, position) -> {
            MenuItems menu = tabItems.get(position);
            tab.setText(menu.getTitle());
        }).attach();

        initTab(tabLayout, mViewPager);


        //Set Name for Navigation Header.
        try {
            View header = navigationView.getHeaderView(0);
            tvUserName = header.findViewById(R.id.tv_name);
            initVersion(header);
        } catch (Exception e) {
            Logger.e(e);
        }

        getVersionControl();

    }

    /*   private void showDialogForRemoteAccess() {

        if (!SecuredStorageManager.getInstance().isFreshLaunch()) {

            RemoteDataEvent remoteDataEvent = SecuredStorageManager.getInstance().getRemoteInfo();

            Logger.d("$$$$$ Nav Act remoteDataEvent " + remoteDataEvent);
            Logger.d("$$$$$ Nav Act remoteDataEvent command "+remoteDataEvent.getCommand().equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND));

            AppUtils.getInstance().printJavaObject(remoteDataEvent);

            if (remoteDataEvent != null && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND))) {

                Loader.getInstance().hideLoader();

                AppDialog.showAlertDialog(this, getString(R.string.app_name), remoteDataEvent.getBody(),

                        "Accept", (dialog, which) -> {

                            String input = remoteDataEvent.getStatus();

                            if (input != null) {

                                String[] parts = input.split(",");

                                if (parts.length == 2) {
                                    String requestId = parts[0];
                                    String lockSerialNo = parts[1];
                                    doAcceptOrRejectRequest(Constant.ACCEPT, lockSerialNo, requestId);
                                }

                            } else {
                                Toast.makeText(this, "Failed, Please try again", Toast.LENGTH_SHORT).show();
                            }

                            SecuredStorageManager.getInstance().setRemoteInfo(null);
                            dialog.dismiss();

                        },
                        "Reject", (dialog, which) -> {

                            String input = remoteDataEvent.getStatus();

                            if (input != null) {

                                String[] parts = input.split(",");

                                if (parts.length == 2) {
                                    String requestId = parts[0];
                                    String lockSerialNo = parts[1];
                                    doAcceptOrRejectRequest(Constant.REJECT, lockSerialNo, requestId);
                                }

                            } else {
                                Toast.makeText(this, "Failed, Please try again", Toast.LENGTH_SHORT).show();

                            }
                            SecuredStorageManager.getInstance().setRemoteInfo(null);
                            dialog.dismiss();
                        },
                        "", (dialog, which) -> {
                            dialog.dismiss();
                        }

                        *//*, 30000*//*
                );

                // SecuredStorageManager.getInstance().setRemoteInfo(null);
                //Logger.d("### Nav Act remote data is cleared ");

            }

        }
    }*/

    private void showDialogForRemoteAccess() {
        if (!SecuredStorageManager.getInstance().isFreshLaunch()) {
            RemoteDataEvent remoteDataEvent = SecuredStorageManager.getInstance().getRemoteInfo();

            Logger.d("$$$$$ Nav Act remoteDataEvent " + remoteDataEvent);
            if (remoteDataEvent == null) {
                Logger.d("$$$$$ Nav Act remoteDataEvent is null");
                return;
            }
            Logger.d("$$$$$ Nav Act remoteDataEvent command " + remoteDataEvent.getCommand().equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND));

            AppUtils.getInstance().printJavaObject(remoteDataEvent);

            if (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND)) {
                Loader.getInstance().hideLoader();

                AppDialog.showAlertDialog(
                        this,
                        getString(R.string.app_name),
                        remoteDataEvent.getBody(),
                        "Accept", (dialog, which) -> {
                            handleRemoteAccessResponse(remoteDataEvent, Constant.ACCEPT, dialog);
                        },
                        "Reject", (dialog, which) -> {
                            handleRemoteAccessResponse(remoteDataEvent, Constant.REJECT, dialog);
                        },
                        "", (dialog, which) -> {
                            dialog.dismiss();
                        }
                );
            }
        }
    }

    private void handleRemoteAccessResponse(RemoteDataEvent remoteDataEvent,
                                            String action,
                                            DialogInterface dialog) {
        String input = remoteDataEvent.getStatus();
        if (input != null) {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                String requestId = parts[0];
                String lockSerialNo = parts[1];
                doAcceptOrRejectRequest(action, lockSerialNo, requestId);
            }
        } else {
            Toast.makeText(this, "Failed, Please try again", Toast.LENGTH_SHORT).show();
        }
        SecuredStorageManager.getInstance().setRemoteInfo(null);
        dialog.dismiss();
        Logger.d("### Nav Act remote data is cleared after handling response");
    }

    private void permissionResultLauncher() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.containsValue(false)) {
                // Permission denied
                if (REQUEST_POST_NOTIFICATION == requestCode) {
                    Logger.d("REQUEST_POST_NOTIFICATION Permission Denied");
                }
                if (onPermissionCallBack != null) {
                    onPermissionCallBack.permissionDenied();
                }

            } else {
                if (REQUEST_POST_NOTIFICATION == requestCode) {
                    Logger.d("REQUEST_POST_NOTIFICATION Permission Denied");
                }
                if (onPermissionCallBack != null) {
                    onPermissionCallBack.permissionGranted();
                }
            }
        });
    }

    private void initVersion(View view) {
        /*try {
            if(BuildConfig.APP_VERSION_VISIBILITY){
                TextView lblVersion = view.findViewById(R.id.lblVersion);
                lblVersion.setVisibility(View.VISIBLE);
                lblVersion.setText(BuildConfig.VERSION_NAME + "#" + BuildConfig.VERSION_CODE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/

    }

    private void getLockVersionsConfig() {

        ServiceManager.getInstance().get(ServiceUrl.GET_LOCK_CONFIG, null, new ResponseHandler() {

            @Override
            public void onSuccess(Object data) {

                if (data != null) {

                    LockVersionConfig lockVersionConfig = new Gson().fromJson(data.toString(), LockVersionConfig.class);

                    if (!lockVersionConfig.getStatus().equals("failure")) {
                        SecuredStorageManager.getInstance().saveLockVersionConfig(lockVersionConfig);
                    }
                }
            }

            @Override
            public void onAuthError(String message) {
                Logger.d("GET_LOCK_CONFIG AuthError", message);
            }

            @Override
            public void onError(String message) {
                Logger.d("GET_LOCK_CONFIG onError", message);
            }
        });
    }

    private void updateDeviceToken() {

        if (SecuredStorageManager.getInstance().getUserData() != null) {
            DeviceTokenModel deviceTokenModel = new DeviceTokenModel();
            deviceTokenModel.setDeviceToken(SecuredStorageManager.getInstance().getDeviceToken());
            deviceTokenModel.setAppid("1");

            ServiceManager.getInstance().post(ServiceUrl.UPDATE_TOKEN, deviceTokenModel, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Logger.d("Update Token Success", data.toString());
                }

                @Override
                public void onAuthError(String message) {
                    Logger.d("Update Token AuthError", message);
                }

                @Override
                public void onError(String message) {
                    Logger.d("Update Token onError", message);
                }
            });
        }
    }

    private void checkFirebaseConfig() {
        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetch(5).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activate();
                            long versionCode = mFirebaseRemoteConfig.getLong(ANDROID_VERSION_CODE);
                            boolean forceUpdate = mFirebaseRemoteConfig.getBoolean(ANDROID_FORCE_UPDATE);
                            String msg = mFirebaseRemoteConfig.getString(ANDROID_FORCE_UPDATE_MESSAGE);
                            long buildVersionCode = BuildConfig.VERSION_CODE;
                            try {
                                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                buildVersionCode = pInfo.versionCode;
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            if (versionCode > buildVersionCode) {
                                if (forceUpdate) {
                                    AppDialog.showAlertDialog(NavigationActivity.this, "Update", msg, "Update", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (!BuildConfig.PLAYSTORE_URL.isEmpty()) {
                                                sendToPlayStore();
                                            }
                                        }
                                    }, null, null);
                                } else {
                                    String showedDate = SecuredStorageManager.getInstance().getUpdateDate();
                                    String todayDate = DateTimeUtils.getTodayDate();
                                    if (todayDate.equalsIgnoreCase(showedDate)) {
                                        SecuredStorageManager.getInstance().setUpdateDate();
                                        AppDialog.showAlertDialog(NavigationActivity.this, "Update", msg, "Update", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (!BuildConfig.PLAYSTORE_URL.isEmpty()) {
                                                    sendToPlayStore();
                                                }
                                            }
                                        }, "Later", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                    }
                                }

                            }
                        }
                    }
                });
    }

    private void sendToPlayStore() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(BuildConfig.PLAYSTORE_URL));
        startActivity(i);
    }

    private void startTimer() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                InactiveTimeoutUtil.startTimer(NavigationActivity.this, NavigationActivity.this);
            }
        }, 1200);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        InactiveTimeoutUtil.startTimer(NavigationActivity.this, this);
        SecuredStorageManager.getInstance().setTimeOutSeconds();
    }

    private void validateTimeout() {
        long curSeconds = System.currentTimeMillis() / 1000;
        long timeOutSeconds = SecuredStorageManager.getInstance().getTimeOutSeconds();

        if ((curSeconds - timeOutSeconds) < SESSION_TIMEOUT) {
            if (SecuredStorageManager.getInstance().isFreshLaunch()) {
                showDialog();
                SecuredStorageManager.getInstance().setFreshLaunch(false);
            }
        } else {
            showDialog();
        }
    }

    private void showDialog() {
        Logger.d("### Nav Showed Pin dialog ");
        App.getInstance().showFullScreen(this, Constant.SCREEN.TIME_OUT_PIN, null);
    }

    private void initSyncService() {
        try {
            //SyncScheduler.getInstance().schedule(this, Constant.JOBS.ALL);
            SyncAll.getInstance().pushAll(NavigationActivity.this);
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void initTabItems() {
        tabItems = new ArrayList<>();
        tabItems.add(new MenuItems(LockListFragment.getInstance(), "Locks", R.drawable.ic_lock_selector));
        tabItems.add(new MenuItems(NotificationFragment.getInstance(), "Notification", R.drawable.ic_notification_selector, 0));
        tabItems.add(new MenuItems(RequestFragment.getInstance(), "Request", R.drawable.ic_user_selector, 0));
    }

    private void initTab(TabLayout tabLayout, final ViewPager2 viewPager) {

        for (int i = 0; i < tabItems.size(); i++) {

            MenuItems menu = tabItems.get(i);
            RelativeLayout tabView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            Button button = tabView.findViewById(R.id.tab_title);
            button.setText(menu.getTitle());
            button.setCompoundDrawablesWithIntrinsicBounds(0, menu.getIcon(), 0, 0);

            if (menu.getCount() > 0) {
                ((Button) tabView.findViewById(R.id.tab_notification_count)).setText(String.valueOf(menu.getCount()));
            } else {
                tabView.findViewById(R.id.tab_notification_count).setVisibility(View.GONE);
            }

            final int finalI = i;

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewPager.setCurrentItem(finalI);
                }
            });

            tabLayout.getTabAt(i).setCustomView(tabView);
        }
    }

    long long_current_time = 0;

    private void OnBackPressedDispatcher() {

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                        getSupportFragmentManager().popBackStack();
                    } else {

//                        finish();

                        if (long_current_time + 2000 > System.currentTimeMillis()) {
                            finish();
                        } else {
                            Toast.makeText(NavigationActivity.this, "Please click back again to exit", Toast.LENGTH_SHORT).show();
                        }
                        long_current_time = System.currentTimeMillis();
                    }
                }
            }
        };

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle activity_bottom_navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_profile:
                App.getInstance().showFullScreen(this, Constant.SCREEN.PROFILE, null);
                break;
            case R.id.nav_reset_pin:
                App.getInstance().showFullScreen(this, Constant.SCREEN.PIN, Constant.VALIDATE_PIN_FLOW);
                break;
            case R.id.nav_transfer_owner:
                if (ServiceManager.getInstance().isNetworkAvailable(NavigationActivity.this)) {
                    App.getInstance().showFullScreen(this, Constant.SCREEN.OWNER_LOCK_LIST, Constant.NAVIGATION_LOCK_TRANSFER_OWNER);
                } else {
                    AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.no_internet));
                }
                break;
            case R.id.nav_factory_reset:
                if (ServiceManager.getInstance().isNetworkAvailable(NavigationActivity.this)) {
                    App.getInstance().showFullScreen(this, Constant.SCREEN.OWNER_LOCK_LIST, Constant.NAVIGATION_LOCK_FACTORY_RESET);
                } else {
                    AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.no_internet));
                }
                break;
            case R.id.nav_force_sync:
                if (ServiceManager.getInstance().isNetworkAvailable(NavigationActivity.this)) {
                    Loader.getInstance().showLoader(this);
                    initSyncService();
                    checkForceSyncData();
                } else {
                    AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.no_internet));
                }
                break;
            case R.id.nav_delete_account:
                if (ServiceManager.getInstance().isNetworkAvailable(NavigationActivity.this)) {
                    doDeleteAccount();
                } else {
                    AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.no_internet));
                }
                break;
            /*case R.id.nav_logout:
                if (ServiceManager.getInstance().isNetworkAvailable(NavigationActivity.this)) {
                    doLogout();
                } else {
                    AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.no_internet));
                }
                break;*/

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkForceSyncData() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.manual_sync_completed));

            }
        }, 10000);
    }

    private void checkLocalStorage() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Loader.getInstance().hideLoader();
                if (isLocalDataAvailable()) {
                    AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.oops_error));
                } else {
                    //doLogout();
                    doDeleteAccount();
                }
            }
        }, 10000);
    }

    private boolean isLocalDataAvailable() {
        LockDBClient.getInstance().getOfflineLocks(this, new LockDBClient.DBCallback() {
            @Override
            public void onLockList(ArrayList<Lock> lockList) {
                if (lockList != null && lockList.size() > 0) {
                    return;
                }
            }

            @Override
            public void onSuccess(String msg) {

            }
        });
        DeviceLog deviceLog = SecuredStorageManager.getInstance().getEntireDeviceLog();
        if (deviceLog != null && deviceLog.getLogs() != null &&
                deviceLog.getLogs().keySet().size() > 0) {
            return true;
        }
        LockKeyList mLockKeyList = SecuredStorageManager.getInstance().getOfflineKeys();
        if (mLockKeyList != null && mLockKeyList.getLockKeys() != null && mLockKeyList.getLockKeys().size() > 0) {
            return true;
        }
        RevokeUserList revokeUserList = SecuredStorageManager.getInstance().getRevokeUserList();
        if (revokeUserList != null && revokeUserList.getRequestUserHashMap() != null
                && revokeUserList.getRequestUserHashMap().keySet().size() > 0) {
            return true;
        }

        FingerPrints fingerPrints = SecuredStorageManager.getInstance().getOfflineFPs();
        if (fingerPrints != null && fingerPrints.getFpUsers() != null && fingerPrints.getFpUsers().size() > 0) {
            return true;
        }

        PinRequest pinRequest = SecuredStorageManager.getInstance().getOfflinePin();
        if (pinRequest != null && pinRequest.getLockPins() != null && pinRequest.getLockPins().size() > 0) {
            return true;
        }

        OtpRequest otpRequest = SecuredStorageManager.getInstance().getOfflineOtp();
        if (otpRequest != null && otpRequest.getLockOtps() != null && otpRequest.getLockOtps().size() > 0) {
            return true;
        }

        return false;
    }

    private void doDeleteAccount() {
        AppDialog.showAlertDialog(NavigationActivity.this, "Delete Account", "Are you sure you want to delete the account?", "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Loader.getInstance().showLoader(NavigationActivity.this);
                if (isLocalDataAvailable()) {
                    initSyncService();
                    checkLocalStorage();
                } else {
                    DeleteAccountService.getInstance().serviceRequest(new ResponseHandler() {
                        @Override
                        public void onSuccess(Object data) {
                            Loader.getInstance().hideLoader();
                            ResponseModel response = (ResponseModel) data;
                            if (response != null && response.getStatus() != null &&
                                    response.getStatus().equalsIgnoreCase("success")) {
                                NavigationActivity.this.finish();
                                SecuredStorageManager.getInstance().clearData();
                                App.getInstance().showLogin(NavigationActivity.this);
                            } else {
                                AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.oops_error));
                            }
                        }

                        @Override
                        public void onAuthError(String message) {
                            Loader.getInstance().hideLoader();
                            AppDialog.showAlertDialog(NavigationActivity.this, message);
                        }

                        @Override
                        public void onError(String message) {
                            Loader.getInstance().hideLoader();
                            AppDialog.showAlertDialog(NavigationActivity.this, message);
                        }
                    });
                }
            }
        }, "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

    }

    @Override
    public void onInactiveTimeOut() {
        //Show timeout pin screen"
        NavigationActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDialog();
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RemoteDataEvent remoteDataEvent) {

        // Remote Access for V6.0
        if (remoteDataEvent != null && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND))) {

            Logger.d("### Fullscreen OnMsg Event");


            Logger.d("### Fullscreen OnMsg command id =  " + remoteDataEvent.getStatus());

            try {
                Loader.getInstance().hideLoader();

                if (!NavigationActivity.this.isFinishing() && AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
                    AppDialog.alertDialog.dismiss();
                    Logger.d("### Alert showing dismissed onMessageEvent ");
                }

                AppDialog.showAlertDialog(this, getString(R.string.app_name), remoteDataEvent.getBody(),

                        "Accept", (dialog, which) -> {


                            String input = remoteDataEvent.getStatus();

                            if (input != null) {

                                String[] parts = input.split(",");

                                if (parts.length == 2) {
                                    String requestId = parts[0];
                                    String lockSerialNo = parts[1];
                                    doAcceptOrRejectRequest(Constant.ACCEPT, lockSerialNo, requestId);
                                }

                            } else {
                                Toast.makeText(this, "Failed, Please try again", Toast.LENGTH_SHORT).show();

                            }


                            dialog.dismiss();

                        },
                        "Reject", (dialog, which) -> {

                            String input = remoteDataEvent.getStatus();

                            if (input != null) {

                                String[] parts = input.split(",");

                                if (parts.length == 2) {
                                    String requestId = parts[0];
                                    String lockSerialNo = parts[1];
                                    doAcceptOrRejectRequest(Constant.REJECT, lockSerialNo, requestId);
                                }

                            } else {
                                Toast.makeText(this, "Failed, Please try again", Toast.LENGTH_SHORT).show();

                            }

                            dialog.dismiss();
                        },
                        "", (dialog, which) -> {
                        }/*,
                        Constant.DIALOG_DISMISS_SECS*/
                );
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    // Remote Access for V6.0
    private void doAcceptOrRejectRequest(String status,
                                         String lockSerialNo,
                                         String requestId) {

        Loader.getInstance().showLoader(this);

        try {

            ResponseHandler handler = new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    if (data != null) {
                        ResponseModel responseModel = (ResponseModel) data;
                        if (responseModel.getStatus().equalsIgnoreCase("success")) {

                            Logger.d(TAG, "success");
                            Loader.getInstance().hideLoader();

                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(1);

                            Toast.makeText(NavigationActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();


                        } else {
                            Logger.d(TAG, "Failed");
                            Loader.getInstance().hideLoader();
                        }
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Logger.d(TAG, "onAuthError");
                    Loader.getInstance().hideLoader();
                }

                @Override
                public void onError(String message) {
                    Logger.d(TAG, "onError");
                    Loader.getInstance().hideLoader();
                }

            };

            RemoteAccessModel mData = new RemoteAccessModel();

            if (status.equalsIgnoreCase(Constant.ACCEPT)) { // accept
                mData.setLockStatus(Constant.ACCEPT);
            } else { // reject
                mData.setLockStatus(Constant.REJECT);
            }

            AppUtils.getInstance().printJavaObject("### doAcceptOrRejectRequest Payloads" + mData);

            mData.setLockSerialNo(lockSerialNo);
            mData.setRequestId(requestId);
            RequestService.getInstance().remoteAccess(mData, handler);

        } catch (Exception e) {
            Logger.e(e);
        }

    }

    private void doNotificationPermissionRequest() {
        getPostNotificationPermission(new RequestPermissionAction() {
            @Override
            public void permissionDenied() {

                isPermissionIsShowing = true;
                AppDialog.showAlertDialog(NavigationActivity.this, getString(R.string.app_name),
                        "Please accept this permission to get notification", "Take me to settings",
                        (dialog, which) -> {
                            isPermissionIsShowing = false;
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            dialog.dismiss();
                        });
            }

            @Override
            public void permissionGranted() {

            }

        });
    }

    /**
     * Check Notification Callback
     *
     * @param onPermissionCallBack
     */
    public void getPostNotificationPermission(RequestPermissionAction
                                                      onPermissionCallBack) {

        this.onPermissionCallBack = onPermissionCallBack;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission(android.Manifest.permission.POST_NOTIFICATIONS)) {
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

    public boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        initSyncService();
        validateTimeout();
        if (tvUserName != null) {
            if (SecuredStorageManager.getInstance() != null && SecuredStorageManager.getInstance().getUserData() != null) {
                tvUserName.setText(SecuredStorageManager.getInstance().getUserData().getName());
            }
        }
        startTimer();
        checkFirebaseConfig();
        getLockVersionsConfig();
        updateDeviceToken();

        if (!Loader.getInstance().isShowing() && !isPermissionIsShowing)
            doNotificationPermissionRequest();

    }

    @Override
    protected void onStart() {

        super.onStart();
        InactiveTimeoutUtil.startTimer(NavigationActivity.this, this);
        Logger.d("### Nav Activity onStart");
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
            AppDialog.alertDialog.dismiss();
        }

        Logger.d("### Nav Activity onStop");
        EventBus.getDefault().unregister(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        SecuredStorageManager.getInstance().setTimeOutSeconds();
        if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
            AppDialog.alertDialog.dismiss();
        }

        Loader.getInstance().dismissLoader();
        InactiveTimeoutUtil.stopTimer();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        InactiveTimeoutUtil.stopTimer();

    }

    /**
     * A {@link FragmentStateAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public Fragment createFragment(int position) {
            return tabItems.get(position).getFragment();
        }

        @Override
        public int getItemCount() {
            return tabItems.size();
        }
    }

    private void getVersionControl() {

        Loader.getInstance().showLoader(this);

        try {

            ResponseHandler handler = new ResponseHandler() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Object data) {

                    Loader.getInstance().hideLoader();

                    if (data != null) {

                        VersionControlModel responseModel = (VersionControlModel) data;

                        if (responseModel.getStatus().equalsIgnoreCase("success")) {


                            String resVersion = responseModel.getFortAndroidVersion();

                            String appVersionName = BuildConfig.VERSION_NAME;


//                            if (!resVersion.equals(appVersionName)) {
//
//                                showAppUpdateDialog();
//                            }


                        } else {
                            Logger.d(TAG, "Failed");
                        }
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Logger.d(TAG, "onAuthError");
                    Loader.getInstance().hideLoader();
                }

                @Override
                public void onError(String message) {
                    Logger.d(TAG, "onError");
                    Loader.getInstance().hideLoader();
                }

            };


            VersionControlService.getInstance().getVersionControl(handler);

        } catch (Exception e) {
            Logger.e(e);
        }

    }

    private void showAppUpdateDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder
                .setMessage("Please update your Fort app for best experience")
                .setCancelable(false)
                .setPositiveButton("Update",
                        (dialog, id) -> {

                            //TODO change play store url

                            final String appPackageName = BuildConfig.APPLICATION_ID; // getPackageName() from NewDashboardActivity.this or Activity object
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                            startActivity(intent);
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));

    }

}
