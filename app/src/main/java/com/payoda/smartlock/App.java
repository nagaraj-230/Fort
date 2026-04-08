package com.payoda.smartlock;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.payoda.smartlock.authentication.LoginFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.fp.view.RFIDAddFragment;
import com.payoda.smartlock.fp.view.RFIDListFragment;
import com.payoda.smartlock.fp.view.RFIDSuccessFragment;
import com.payoda.smartlock.fp.view.UserListFragment;
import com.payoda.smartlock.history.HistoryFragment;
import com.payoda.smartlock.fp.view.FPAddFragment;
import com.payoda.smartlock.fp.view.FPListFragment;
import com.payoda.smartlock.locks.LockAddFragment;
import com.payoda.smartlock.locks.LockDetailFragment;
import com.payoda.smartlock.locks.LockListFragment;
import com.payoda.smartlock.locks.OwnersLockFragment;
import com.payoda.smartlock.locks.WifiMqttConfigurationFragment;
import com.payoda.smartlock.notification.NotificationFragment;
import com.payoda.smartlock.password.ForgotPasswordFragment;
import com.payoda.smartlock.password.SuccessfullyPasswordResetFragment;
import com.payoda.smartlock.pin.PinFragment;
import com.payoda.smartlock.pin.PinSuccessFragment;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageHelper;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.profile.ProfileFragment;
import com.payoda.smartlock.request.RequestFragment;
import com.payoda.smartlock.signup.PrivacyPolicyFragment;
import com.payoda.smartlock.signup.SignUpFragment;
import com.payoda.smartlock.signup.SuccessfullyRegisteredFragment;
import com.payoda.smartlock.signup.TermAndCondFragment;
import com.payoda.smartlock.timeoutpin.TimeOutPinFragment;
import com.payoda.smartlock.transfer.TransferOwnerFragment;
import com.payoda.smartlock.users.AssignUsersFragment;
import com.payoda.smartlock.users.ScheduleAccessFragment;

public class App extends Application {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static App instance;

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }

    private void init() {
        StorageManager.getInstance().init(this);
        SecuredStorageManager.getInstance().init(this);
        ServiceManager.getInstance().init(this);
        StorageHelper.getInstance().migrateAll();
        //TestFairyManager.getInstance().init(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void showDashboard(Activity activity) {

        Intent intent = new Intent(activity, NavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();

    }

    public void showLogin(Activity activity) {
        SecuredStorageManager.getInstance().clearData();
        Intent intent = new Intent(activity, FullscreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    public void showFullScreen(Activity activity, Constant.SCREEN screenName, String dataObj) {
        Intent intent = new Intent(activity, FullscreenActivity.class);
        intent.putExtra(Constant.SCREEN_NAME, screenName.name());
        intent.putExtra(Constant.SCREEN_DATA, dataObj);
        activity.startActivity(intent);
    }

    public void showFullScreenForResult(Activity activity, Constant.SCREEN screenName, String dataObj,int reqCode) {
        Intent intent = new Intent(activity, FullscreenActivity.class);
        intent.putExtra(Constant.SCREEN_NAME, screenName.name());
        intent.putExtra(Constant.SCREEN_DATA, dataObj);
        activity.startActivityForResult(intent,reqCode);
    }

    public void showFullScreenForResult(Activity activity,
                                        Constant.SCREEN screenName,
                                        String dataObj,
                                        int reqCode,
                                        ActivityResultLauncher<Intent> launcher ) {

        Intent intent = new Intent(activity, FullscreenActivity.class);
        intent.putExtra(Constant.SCREEN_NAME, screenName.name());
        intent.putExtra(Constant.SCREEN_DATA, dataObj);
        launcher.launch(intent);

    }

    /**
     * Method to show the given screen.
     *
     * @param fragmentManager - Current fragment Manager.
     * @param screenName      - Name of the screen.
     */
    public void navigate(FragmentManager fragmentManager, Constant.SCREEN screenName) {
        switchFragment(fragmentManager, R.id.main_container, screenName, null);
    }

    public void navigate(FragmentManager fragmentManager, Constant.SCREEN screenName, String dataObj) {
        switchFragment(fragmentManager, R.id.main_container, screenName, dataObj);
    }

    public void navigateDetail(Activity activity, Constant.SCREEN screenName) {
        showFullScreen(activity, screenName, null);
    }

    public void navigateDetail(Activity activity, Constant.SCREEN screenName, String dataObj) {
        showFullScreen(activity, screenName, dataObj);
    }

    /**
     * Method to show the given screen.
     *
     * @param fragmentManager - Current fragment Manager.
     * @param screenName      - Name of the screen.
     */

    public void navigateDetail(FragmentManager fragmentManager, Constant.SCREEN screenName) {
        switchFragment(fragmentManager, R.id.full_screen_container, screenName, null);
    }

    public void navigateDetail(FragmentManager fragmentManager, Constant.SCREEN screenName, String dataObj) {
        switchFragment(fragmentManager, R.id.full_screen_container, screenName, dataObj);
    }

    /**
     * Method to show the given screen.
     *
     * @param fragmentManager - Current fragment Manager.
     * @param screenName      - Name of the screen.
     */
    public void switchFragment(FragmentManager fragmentManager, Constant.SCREEN screenName, String dataObj) {
        switchFragment(fragmentManager, R.id.main_container, screenName, dataObj);
    }

    public void switchFragment(FragmentManager fragmentManager, int container, Constant.SCREEN screenName, String dataObj) {
        Fragment mFragment;
        switch (screenName) {
            case LOGIN:
                mFragment = LoginFragment.getInstance();
                break;
            case SIGNUP:
                mFragment = SignUpFragment.getInstance();
                break;
            case FORGOT_PASSWORD:
                mFragment = ForgotPasswordFragment.getInstance();
                break;

            case FORGOT_PASSWORD_SUCCESS:
                mFragment = SuccessfullyPasswordResetFragment.getInstance();
                break;
            case SUCCESSFULLY_REGISTERED:
                mFragment = SuccessfullyRegisteredFragment.getInstance();
                break;
            case PIN:
                mFragment = PinFragment.getInstance();

                break;
            case PIN_SUCCESS:
                mFragment = PinSuccessFragment.getInstance();
                break;
            case PROFILE:
                mFragment = ProfileFragment.getInstance();
                break;
            case LOCK_ADD:
                mFragment = LockAddFragment.getInstance();
                break;
            case LOCK_DETAILS:
                mFragment = LockDetailFragment.getInstance();
                break;
            case WIFI_MQTT_CONFIGURATION:
                mFragment = WifiMqttConfigurationFragment.getInstance();
                break;
            case ASSIGN_USERS:
                mFragment = AssignUsersFragment.getInstance();
                break;
            case USERS_REQUEST:
                mFragment = RequestFragment.getInstance();
                break;
            case NOTIFICATIONS:
                mFragment = NotificationFragment.getInstance();
                break;
            case TRANSFER_OWNER:
                mFragment = TransferOwnerFragment.getInstance();
                break;
            case HISTORY:
                mFragment = HistoryFragment.getInstance();
                break;
            case TIME_OUT_PIN:
                mFragment = TimeOutPinFragment.getInstance();
                break;
            case TERMS:
                mFragment = TermAndCondFragment.getInstance();
                break;
            case PRIVACY_POLICY:
                mFragment = PrivacyPolicyFragment.getInstance();
                break;
            case LOCK_LIST:
                mFragment = LockListFragment.getInstance();
                break;
            case OWNER_LOCK_LIST:
                mFragment= OwnersLockFragment.getInstance();
                break;
            case SCHEDULE:
                mFragment= ScheduleAccessFragment.getInstance();
                break;
            case FINGER_PRINT_LIST:
                mFragment= FPListFragment.getInstance();
                break;
            case FINGER_PRINT_ADD:
                mFragment= FPAddFragment.getInstance();
                break;
            case RFID_LIST:
                mFragment= RFIDListFragment.getInstance();
                break;
            case RFID_ADD:
                mFragment= RFIDAddFragment.getInstance();
                break;
            case USER_LIST:
                mFragment= UserListFragment.getInstance();
                break;
            case RFID_SUCCESS:
                mFragment= RFIDSuccessFragment.getInstance();
                break;
            default:
                mFragment = null;
                break;
        }
        if (mFragment != null) {
            Bundle bundle = new Bundle();
            if (dataObj != null) {
                bundle.putString(Constant.SCREEN_DATA, dataObj);
                mFragment.setArguments(bundle);
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .addToBackStack(mFragment.getClass().getSimpleName())
                    .replace(container, mFragment, screenName.toString())
                    .commit();
        }
    }

}
