package com.payoda.smartlock.splash;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import com.payoda.rootchecker.SourceIDE;
import com.scottyab.rootbeer.RootBeer;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.FullscreenActivity;
import com.payoda.smartlock.NavigationActivity;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.model.Login;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.splash.service.BrandService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Logger;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    public static final String TAG = "### SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Logger.d(TAG, TAG);

        // Initialize SDK

        if (!BuildConfig.APPCENTER_KEY.equals("")) {
            // Use APPCENTER_APP_SECRET environment variable if it exists
            if (!AppCenter.isConfigured()) {
                AppCenter.start(getApplication(), BuildConfig.APPCENTER_KEY,
                        Analytics.class, Crashes.class);
            }
        } else {
            Logger.d("APPCENTER_APP_SECRET is not available...");
        }

        if (BuildConfig.DEBUG) {
            AppCenter.setLogLevel(Log.VERBOSE);
        }

        // For Remote access
        String remoteAccessString = getIntent().getStringExtra(Constant.REMOTE_ACCESS_DATA);

        Logger.d("$$$$ Splash remoteAccessString = " + remoteAccessString);

        if (remoteAccessString != null && !remoteAccessString.isEmpty())
            SecuredStorageManager.getInstance().setRemoteInfo(remoteAccessString);  //save in session
        else
            SecuredStorageManager.getInstance().setRemoteInfo(null);  // clear session

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        BrandService.getInstance().serviceRequest(new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {

                Logger.d("### branding res = " + data);

                try {
                    BrandInfoResponse brandInfoResponse = (BrandInfoResponse) data;

                    if (brandInfoResponse != null && brandInfoResponse.getBrandInfo() != null) {

                        BrandInfoResponse.BrandInfo brandInfo = brandInfoResponse.getBrandInfo();

                        //FORT_ - ASTRIX_
                        // brandInfo.setManufacturerCode("ASTRIX_"); // TODO delete this ManufacturerCode

                        SecuredStorageManager.getInstance().setBrandInfo(brandInfo);

                    }

                } catch (Exception e) {

                }
            }

            @Override
            public void onAuthError(String message) {

                Logger.d("### branding onAuthError = " + message);
            }

            @Override
            public void onError(String message) {

                Logger.d("### branding onError = " + message);
            }

        });
        InvokeMainActivity();
    }

    private void InvokeMainActivity() {

//        SourceIDE sourceIDE = new SourceIDE(SplashActivity.this);
//
//        boolean isRoot3 = sourceIDE.isSource() && sourceIDE.isRootedWithoutBusyBoxCheck();

        // New RootBeer instance
        RootBeer rootBeer = new RootBeer(SplashActivity.this);
        // This performs a comprehensive check (similar to your old isRootedWithoutBusyBoxCheck)
        boolean isRooted = rootBeer.isRooted();

//        if (isRoot3) {
        if (isRooted) {
            SplashActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AppDialog.showAlertDialog(SplashActivity.this, "Error", "The app is not supported on rooted device.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SplashActivity.this.finish();
                                }
                            });
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                SecuredStorageManager.getInstance().setFreshLaunch(true);

                if (SecuredStorageManager.getInstance().getUserData() != null) {



                    if (SecuredStorageManager.getInstance().getPin() != null) {

                        App.getInstance().showDashboard(SplashActivity.this);

                    } else {
                        App.getInstance().showFullScreen(SplashActivity.this, Constant.SCREEN.PIN, null);
                        finish();
                    }

                } else {
                    App.getInstance().showLogin(SplashActivity.this);
                }
            }, 3000);
        }
    }

}
