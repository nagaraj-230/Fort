package com.payoda.smartlock.history;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import com.google.gson.Gson;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.history.model.History;
import com.payoda.smartlock.history.model.HistoryList;
import com.payoda.smartlock.managepins.model.OtpResponse;
import com.payoda.smartlock.managepins.service.OtpKeyService;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.service.AESEncryption;
import android.app.NotificationChannel;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payoda.smartlock.App;
import com.payoda.smartlock.FullscreenActivity;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.managepins.TabManagePinsActivity;
import com.payoda.smartlock.model.RemoteAccessModel;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.request.service.RequestService;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.InactiveTimeoutUtil;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import static com.payoda.smartlock.utils.InactiveTimeoutUtil.SESSION_TIMEOUT;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class TabHistoryActivity extends AppCompatActivity implements InactiveTimeoutUtil.TimeOutListener{

    public static final String  TAG = "### TabHistoryActivity";

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private HistoryFragment historyFragment;
    private LockNotificationFragment notificationFragment;

    private final android.os.Handler mHistoryHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable mHistoryRunnable;
    private static final int POLLING_INTERVAL_MS = 5000;
    private String mLatestLogId = null;
    private boolean mIsFirstPoll = true;
    private Lock mLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_history);
        Logger.d(TAG, TAG);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lock History");
*/
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        LinearLayout header = findViewById(R.id.header);
        ((TextView) findViewById(R.id.tv_title)).setText(getString(R.string.history));
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabHistoryActivity.this.finish();
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
        }
        historyFragment = HistoryFragment.getInstance();
        historyFragment.setArguments(bundle);

        notificationFragment= LockNotificationFragment.getInstance();
        notificationFragment.setArguments(bundle);
    }

    private void startHistoryPolling() {
        if (mLock == null || mLock.getId() == null) {
            return;
        }
        stopHistoryPolling();
        mIsFirstPoll = true;
        mLatestLogId = null;
        mHistoryRunnable = new Runnable() {
            @Override
            public void run() {
                if (TabHistoryActivity.this.isFinishing()) {
                    return;
                }
                if (!ServiceManager.getInstance().isNetworkAvailable(TabHistoryActivity.this)) {
                    mHistoryHandler.postDelayed(this, POLLING_INTERVAL_MS);
                    return;
                }
                String url = String.format(java.util.Locale.ENGLISH, com.payoda.smartlock.constants.ServiceUrl.GET_HISTORY, mLock.getId(), "5", "0") + "&t=" + System.currentTimeMillis();
                ServiceManager.getInstance().get(url, null, new ResponseHandler() {
                    @Override
                    public void onSuccess(Object data) {
                        if (TabHistoryActivity.this.isFinishing()) {
                            return;
                        }
                        if (data != null) {
                            try {
                                HistoryList historyList = new Gson().fromJson(data.toString(), HistoryList.class);
                                if (historyList != null && historyList.getHistory() != null) {
                                    if (historyList.getHistory().isEmpty()) {
                                        if (mIsFirstPoll) {
                                            mLatestLogId = "";
                                            mIsFirstPoll = false;
                                            Logger.d(TAG, "First poll. History list is empty. Setting baseline to empty.");
                                        }
                                    } else {
                                        if (mIsFirstPoll) {
                                            mLatestLogId = historyList.getHistory().get(0).getId();
                                            mIsFirstPoll = false;
                                            Logger.d(TAG, "First poll. Baseline log ID: " + mLatestLogId);
                                            
                                            History latestLog = historyList.getHistory().get(0);
                                            String logType = latestLog.getType() != null ? latestLog.getType().trim() : "";
                                            if (logType.equalsIgnoreCase("F") || logType.equalsIgnoreCase("FP")) {
                                                String lastNotifiedId = SecuredStorageManager.getInstance().getLastNotifiedFpLogId();
                                                if (latestLog.getId() != null && !latestLog.getId().equals(lastNotifiedId)) {
                                                    handleNewLogEvent(latestLog);
                                                }
                                            }
                                        } else {
                                             ArrayList<History> newLogs = new ArrayList<>();
                                             for (History log : historyList.getHistory()) {
                                                 if (log.getId() != null && log.getId().equals(mLatestLogId)) {
                                                     break;
                                                 }
                                                 newLogs.add(log);
                                             }
                                             String newestId = historyList.getHistory().get(0).getId();
                                             if (newestId != null) {
                                                 mLatestLogId = newestId;
                                             }
                                             
                                             if (!newLogs.isEmpty()) {
                                                 if (historyFragment != null) {
                                                     historyFragment.onNewLogsDetected(newLogs);
                                                 }
                                                 if (notificationFragment != null) {
                                                     notificationFragment.onNewLogsDetected();
                                                 }
                                             }
                                             
                                             for (int i = newLogs.size() - 1; i >= 0; i--) {
                                                 History log = newLogs.get(i);
                                                 Logger.d(TAG, "New log detected! ID: " + log.getId() + ", Type: " + log.getType());
                                                 handleNewLogEvent(log);
                                             }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Logger.e(e);
                            }
                        }
                        mHistoryHandler.postDelayed(mHistoryRunnable, POLLING_INTERVAL_MS);
                    }

                    @Override
                    public void onAuthError(String message) {
                        Logger.d(TAG, "History poll Auth Error: " + message);
                        mHistoryHandler.postDelayed(mHistoryRunnable, POLLING_INTERVAL_MS);
                    }

                    @Override
                    public void onError(String message) {
                        Logger.d(TAG, "History poll Error: " + message);
                        mHistoryHandler.postDelayed(mHistoryRunnable, POLLING_INTERVAL_MS);
                    }
                });
            }
        };
        mHistoryHandler.post(mHistoryRunnable);
    }

    private void stopHistoryPolling() {
        if (mHistoryRunnable != null) {
            mHistoryHandler.removeCallbacks(mHistoryRunnable);
            mHistoryRunnable = null;
        }
    }

    private void handleNewLogEvent(History log) {
        String logType = log.getType() != null ? log.getType().trim() : "";
        if (logType.isEmpty()) return;

        if (logType.equalsIgnoreCase("F") || logType.equalsIgnoreCase("FP")) {
            Logger.d(TAG, "Fingerprint log detected. Fetching OTP keys.");
            if (mLock != null && mLock.getId() != null) {
                OtpKeyService.getInstance().getOtp(mLock.getId(), "1", new ResponseHandler() {
                    @Override
                    public void onSuccess(Object data) {
                        if (TabHistoryActivity.this.isFinishing()) return;
                        if (data != null) {
                            try {
                                Logger.d(TAG, "getOtp onSuccess payload: " + data.toString());
                                OtpResponse otpResponse = new Gson().fromJson(data.toString(), OtpResponse.class);
                                if (otpResponse != null && otpResponse.getOtpData() != null) {
                                    ArrayList<LockKeys> lockKeysList = otpResponse.getOtpData().getLockKeyList();
                                    if (lockKeysList != null && !lockKeysList.isEmpty()) {
                                         LockKeys targetOtpKey = null;
                                         // Step 1: Scan for an active key that is not yet notified
                                         for (LockKeys lk : lockKeysList) {
                                             Logger.d(TAG, "Key ID: " + lk.getId() + " | Status: " + lk.getStatus() + " | Type: " + lk.getUserType());
                                             if (lk.getStatus() != null && lk.getStatus().equals("1")) {
                                                 if (!SecuredStorageManager.getInstance().isOtpIdNotified(lk.getId())) {
                                                     targetOtpKey = lk;
                                                     break;
                                                 }
                                             }
                                         }
                                         
                                         // Step 2: If target is null, it means a new batch was generated and slot IDs are reused.
                                         // Clear cache and search again.
                                         if (targetOtpKey == null) {
                                             Logger.d(TAG, "No active non-notified key found. Clearing cache (new batch detected).");
                                             SecuredStorageManager.getInstance().clearAllNotifiedOtpIds();
                                             for (LockKeys lk : lockKeysList) {
                                                 if (lk.getStatus() != null && lk.getStatus().equals("1")) {
                                                     if (!SecuredStorageManager.getInstance().isOtpIdNotified(lk.getId())) {
                                                         targetOtpKey = lk;
                                                         break;
                                                     }
                                                 }
                                             }
                                         }
                                         
                                         if (targetOtpKey != null) {
                                             String encryptedOtp = targetOtpKey.getKey();
                                             String decryptedOtp = AESEncryption.getInstance().decrypt(encryptedOtp, true);
                                             Logger.d(TAG, "Decrypted OTP: " + decryptedOtp);
                                             SecuredStorageManager.getInstance().addNotifiedOtpId(targetOtpKey.getId());
                                             SecuredStorageManager.getInstance().setLastNotifiedFpLogId(log.getId());
                                             showOtpNotification(decryptedOtp);
                                         } else {
                                             Logger.d(TAG, "No active key found even after clearing cache.");
                                         }
                                    } else {
                                         Logger.d(TAG, "lockKeysList is empty.");
                                    }
                                } else {
                                    Logger.d(TAG, "OtpResponse or OtpData is null");
                                }
                            } catch (Exception e) {
                                Logger.e(e);
                            }
                        }
                    }

                    @Override
                    public void onAuthError(String message) {
                        Logger.d(TAG, "getOtp onAuthError: " + message);
                    }

                    @Override
                    public void onError(String message) {
                        Logger.d(TAG, "getOtp onError: " + message);
                    }
                });
            }
        } else if (logType.equalsIgnoreCase("O") || logType.equalsIgnoreCase("TO")) {
            Logger.d(TAG, "OTP usage log detected. Posting RefreshOtpEvent.");
            EventBus.getDefault().post(new com.payoda.smartlock.locks.LockDetailFragment.RefreshOtpEvent());
        }
    }

    private void showOtpNotification(String otp) {
        if (TabHistoryActivity.this.isFinishing()) return;

        String title = "OTP Notification";
        String message = "Your OTP is: " + otp;

        AppDialog.showAlertDialog(TabHistoryActivity.this, title, message);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "otp_notification_channel";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "OTP Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for showing OTP notifications on fingerprint authentication");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(TabHistoryActivity.this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(99, builder.build());
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInactiveTimeOut() {
        super.onUserInteraction();
        TabHistoryActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDialog();
            }
        });
    }

    private void showDialog() {
        App.getInstance().showFullScreen(this, Constant.SCREEN.TIME_OUT_PIN, null);
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                return notificationFragment;
            } else {
                return historyFragment;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SyncScheduler.getInstance().schedule(TabHistoryActivity.this, Constant.JOBS.ALL);
        validateTimeout();
        InactiveTimeoutUtil.startTimer(TabHistoryActivity.this, TabHistoryActivity.this);
        startHistoryPolling();
    }

    @Override
    protected void onStart() {
        super.onStart();
        InactiveTimeoutUtil.startTimer(TabHistoryActivity.this, this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        stopHistoryPolling();
        super.onPause();
        if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
            AppDialog.alertDialog.dismiss();
            Logger.d("### Alert showing dismissed onDestroy ");
        }
        Loader.getInstance().dismissLoader();
        InactiveTimeoutUtil.stopTimer();
    }

    @Override
    protected void onDestroy() {
        if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
            AppDialog.alertDialog.dismiss();
            Logger.d("### Alert showing dismissed onDestroy ");
        }
        super.onDestroy();
        InactiveTimeoutUtil.stopTimer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RemoteDataEvent remoteDataEvent) {

        // Remote Access for V6.0
        if (remoteDataEvent != null && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND))) {

            Logger.d("### Fullscreen OnMsg Event");
            Logger.d("### Fullscreen OnMsg command id =  " + remoteDataEvent.getStatus());

            Loader.getInstance().hideLoader();
            if (!TabHistoryActivity.this.isFinishing() &&AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
                AppDialog.alertDialog.dismiss();
                Logger.d("### Alert showing dismissed");
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
                    "" , (dialog,which) ->{
                    }
                   /* ,Constant.DIALOG_DISMISS_SECS*/

            );
        }
    }

    // Remote Access for V6.0
    private void doAcceptOrRejectRequest(String status, String lockSerialNo, String requestId) {

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

                            Toast.makeText(TabHistoryActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();


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

            mData.setLockSerialNo(lockSerialNo);
            mData.setRequestId(requestId);

            RequestService.getInstance().remoteAccess(mData, handler);

        } catch (Exception e) {
            Logger.e(e);
        }

    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        InactiveTimeoutUtil.startTimer(TabHistoryActivity.this, this);
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
}
