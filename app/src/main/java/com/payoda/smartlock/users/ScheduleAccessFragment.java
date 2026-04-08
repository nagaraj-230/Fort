package com.payoda.smartlock.users;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.users.model.Schedule;
import com.payoda.smartlock.users.model.ScheduleResponse;
import com.payoda.smartlock.users.model.ScheduleUserKeys;
import com.payoda.smartlock.users.service.ScheduleAccessService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.payoda.smartlock.utils.DateTimeUtils.YYYYMMDD_HHMMSS;

/**
 * Created by david on 10/12/18.
 */

public class ScheduleAccessFragment extends BaseFragment
        implements View.OnClickListener {

    LockKeys lockKeys = null;
    ScheduleUserKeys scheduleUserKeys = null;
    RelativeLayout layoutStartDate, layoutEndDate, layoutStartTime, layoutEndTime;
    TextView lblStartDate, lblEndDate, lblStartTime, lblEndTime;
    private ImageView img_edit, img_save;

    public ScheduleAccessFragment() {
        // Required empty public constructor
    }

    public static ScheduleAccessFragment getInstance() {
        return new ScheduleAccessFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            scheduleUserKeys = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), ScheduleUserKeys.class);
            lockKeys = scheduleUserKeys.getLockKeys();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d("### AssignUsersFragment");
        return initializeView(inflater.inflate(R.layout.fragment_schedule_access, container, false));
    }

    private View initializeView(View view) {
        layoutStartDate = view.findViewById(R.id.layoutStartDate);
        layoutEndDate = view.findViewById(R.id.layoutEndDate);
        layoutStartTime = view.findViewById(R.id.layoutStartTime);
        layoutEndTime = view.findViewById(R.id.layoutEndTime);
        lblStartDate = view.findViewById(R.id.lblStartDate);
        lblEndDate = view.findViewById(R.id.lblEndDate);
        lblStartTime = view.findViewById(R.id.lblStartTime);
        lblEndTime = view.findViewById(R.id.lblEndTime);
        img_edit = view.findViewById(R.id.iv_edit);
        img_save = view.findViewById(R.id.iv_save);
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.schedule));
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        layoutStartDate.setOnClickListener(this);
        layoutEndDate.setOnClickListener(this);
        layoutStartTime.setOnClickListener(this);
        layoutEndTime.setOnClickListener(this);
        img_save.setOnClickListener(this);
        img_edit.setOnClickListener(this);
        populateData();
        return view;
    }

    private void populateData() {
        if (lockKeys.getIs_schedule_access() != null &&
                lockKeys.getIs_schedule_access().equalsIgnoreCase("1")) {

            Logger.d("### Schedule_from_date = "+lockKeys.getSchedule_date_from());
            Logger.d("### Schedule_time_from = "+lockKeys.getSchedule_time_from());
            Logger.d("### Schedule_Date_to = "+lockKeys.getSchedule_date_to());
            Logger.d("### Schedule_time_to = "+lockKeys.getSchedule_time_to());

            String startDate = DateTimeUtils.getLocalDateFromGMT(lockKeys.getSchedule_date_from(),
                    lockKeys.getSchedule_time_from(), YYYYMMDD_HHMMSS);
            String endDate = DateTimeUtils.getLocalDateFromGMT(lockKeys.getSchedule_date_to(),
                    lockKeys.getSchedule_time_to(), YYYYMMDD_HHMMSS);

            String[] startDateAndTime = startDate.split(" ");
            String[] endDateAndTime = endDate.split(" ");

            lblStartDate.setText(startDateAndTime[0]);
            lblEndDate.setText(endDateAndTime[0]);
            lblStartTime.setTag(startDateAndTime[1]);
            lblEndTime.setTag(endDateAndTime[1]);

            String[] startTimeString = startDateAndTime[1].split(":");
            String[] endTimeString = endDateAndTime[1].split(":");

            lblStartTime.setText(getFormattedTime(Integer.parseInt(startTimeString[0]), Integer.parseInt(startTimeString[1])));
            lblEndTime.setText(getFormattedTime(Integer.parseInt(endTimeString[0]), Integer.parseInt(endTimeString[1])));

            img_edit.setVisibility(View.VISIBLE);
            img_save.setVisibility(View.INVISIBLE);
            layoutStartDate.setEnabled(false);
            layoutEndDate.setEnabled(false);
            layoutStartTime.setEnabled(false);
            layoutEndTime.setEnabled(false);

        } else {
            img_save.setVisibility(View.VISIBLE);
            img_edit.setVisibility(View.INVISIBLE);
        }
        if (!scheduleUserKeys.isEditable()) {
            img_save.setVisibility(View.INVISIBLE);
            img_edit.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        if (layoutStartDate == view) {
            if (startDatePickerDialog == null) {
                startDatePickerDialog = new DatePickerDialog(getContext(), startDate, startCalendar
                        .get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH));
            }
            startDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            startDatePickerDialog.show();
        }
        else if (layoutEndDate == view) {
            if (endDatePickerDialog == null) {
                endDatePickerDialog = new DatePickerDialog(getContext(), endDate, endCalendar
                        .get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH));
            }
            endDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            endDatePickerDialog.show();
        }
        else if (layoutStartTime == view) {
            showStartTime();
        }
        else if (layoutEndTime == view) {
            showEndTime();
        }
        else if (img_edit == view) {
            img_save.setVisibility(View.VISIBLE);
            img_edit.setVisibility(View.INVISIBLE);
            layoutStartDate.setEnabled(true);
            layoutEndDate.setEnabled(true);
            layoutStartTime.setEnabled(true);
            layoutEndTime.setEnabled(true);
        }
        else if (img_save == view) {
            if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                if (isValidDate()) {
                    img_save.setVisibility(View.INVISIBLE);
                    img_edit.setVisibility(View.VISIBLE);
                    layoutStartDate.setEnabled(false);
                    layoutEndDate.setEnabled(false);
                    layoutStartTime.setEnabled(false);
                    layoutEndTime.setEnabled(false);
                    saveSchedule();
                }
            } else {
                AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
            }
        }
    }

    private void saveSchedule() {

        Logger.d("### lblStartDate = " + lblStartDate.getText().toString());
        Logger.d("### lblStartTime = " + lblStartTime.getTag().toString());
        Logger.d("### lblEndDate = " + lblEndDate.getText().toString());
        Logger.d("### lblEndTime = " + lblEndTime.getTag().toString());

        String startDate = DateTimeUtils.getGMTTimeFromLocal(lblStartDate.getText().toString(), lblStartTime.getTag().toString());
        String endDate = DateTimeUtils.getGMTTimeFromLocal(lblEndDate.getText().toString(), lblEndTime.getTag().toString());

        Logger.d("### startDate = " + startDate);
        Logger.d("### endDate = " + endDate);

        String[] startDateAndTime = startDate.split(" ");
        String[] endDateAndTime = endDate.split(" ");

        Schedule schedule = new Schedule();
        schedule.setIs_schedule_access(1);

        schedule.setSchedule_date_from(startDateAndTime[0]);
        schedule.setSchedule_date_to(endDateAndTime[0]);

        schedule.setSchedule_time_from(startDateAndTime[1]);
        schedule.setSchedule_time_to(endDateAndTime[1]);

        Loader.getInstance().showLoader(getContext());

        ScheduleAccessService.getInstance().saveSchedule(lockKeys.getId(), schedule, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                ScheduleResponse response = (ScheduleResponse) data;
                if (response.getMessage() != null) {
                    AppDialog.showAlertDialog(getActivity(), response.getMessage(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    });
                } else {
                    AppDialog.showAlertDialog(getActivity(), "Please try again later.");
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(getContext(), message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.getInstance().showLogin(getActivity());
                    }
                });
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
            }
        });

    }

    private boolean isValidDate() {
        if (lblStartDate.getText().toString().contains("Start")) {
            AppDialog.showAlertDialog(getActivity(), "Please select start date.");
            return false;
        } else if (lblEndDate.getText().toString().contains("End")) {
            AppDialog.showAlertDialog(getActivity(), "Please select end date.");
            return false;
        } else if (lblStartTime.getText().toString().contains("Start")) {
            AppDialog.showAlertDialog(getActivity(), "Please select start time.");
            return false;
        } else if (lblEndTime.getText().toString().contains("End")) {
            AppDialog.showAlertDialog(getActivity(), "Please select end time.");
            return false;
        } else if (!isGreater(lblStartDate.getText().toString(),
                lblEndDate.getText().toString())) {
            AppDialog.showAlertDialog(getActivity(), "Please select valid date.");
            return false;
        }

        return true;
    }

    private boolean isGreater(String date1, String date2) {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date fromDate = dt.parse(date1);
            Date toDate = dt.parse(date2);
            return (fromDate.before(toDate) || date1.equalsIgnoreCase(date2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showStartTime() {

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
            lblStartTime.setTag(String.format("%02d", selectedHour) + ":" + String.format("%02d", selectedMinute) + ":00");
            lblStartTime.setText(getFormattedTime(selectedHour, selectedMinute));
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private String getFormattedTime(int hourOfDay, int minute) {

        String date = "", format;
        if (hourOfDay == 0) {
            hourOfDay += 12;
            format = " AM";
        }
        else if (hourOfDay == 12) {
            format = " PM";

        }
        else if (hourOfDay > 12) {
            hourOfDay -= 12;
            format = " PM";
        }
        else {
            format = " AM";
        }
        date = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + format;
        return date;

    }

    private void showEndTime() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
            lblEndTime.setTag(String.format("%02d", selectedHour) + ":" + String.format("%02d", selectedMinute) + ":00");
            lblEndTime.setText(getFormattedTime(selectedHour, selectedMinute));
        }, hour, minute, false);

        //Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    Calendar startCalendar = Calendar.getInstance();
    DatePickerDialog startDatePickerDialog = null;

    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, monthOfYear);
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "yyyy-MM-dd"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            lblStartDate.setText(sdf.format(startCalendar.getTime()));
        }
    };

    Calendar endCalendar = Calendar.getInstance();
    DatePickerDialog endDatePickerDialog = null;

    DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, monthOfYear);
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "yyyy-MM-dd"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            lblEndDate.setText(sdf.format(endCalendar.getTime()));
        }
    };

}
