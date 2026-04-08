package com.payoda.smartlock.history;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
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
        /*if (bundle != null) {
            Lock mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
        }*/
        historyFragment = HistoryFragment.getInstance();
        historyFragment.setArguments(bundle);

        notificationFragment= LockNotificationFragment.getInstance();
        notificationFragment.setArguments(bundle);
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
