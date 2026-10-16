package com.payoda.smartlock.sensors;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.sensors.model.Sensor;
import com.payoda.smartlock.utils.AppDialog;

public class SensorDetailFragment extends BaseFragment {

    public static final String TAG = "### SensorDetailFragment";

    private Sensor mSensor;
    private TextView tvSensorTitle, tvSensorCodeDisplay, tvBattery, lblBatteryLabel, tvFactoryReset;
    private EditText etSensorTitle;
    private ImageView ivEdit, ivSave, ivSettings;
    private Button btnSensorHistory;

    public SensorDetailFragment() {
        // Required empty public constructor
    }

    public static SensorDetailFragment getInstance() {
        return new SensorDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(Constant.SCREEN_DATA)) {
            String data = bundle.getString(Constant.SCREEN_DATA);
            if (!TextUtils.isEmpty(data)) {
                mSensor = new Gson().fromJson(data, Sensor.class);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_detail, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.sensor_details));

        view.findViewById(R.id.iv_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        tvSensorTitle = view.findViewById(R.id.tv_sensor_title);
        etSensorTitle = view.findViewById(R.id.et_sensor_title);
        tvSensorCodeDisplay = view.findViewById(R.id.tv_sensor_code_display);
        tvBattery = view.findViewById(R.id.tv_sensor_battery);
        lblBatteryLabel = view.findViewById(R.id.lblBatteryLabel);
        tvFactoryReset = view.findViewById(R.id.tv_factory_reset);
        ivEdit = view.findViewById(R.id.iv_edit);
        ivSave = view.findViewById(R.id.iv_save);
        ivSettings = view.findViewById(R.id.iv_settings);
        btnSensorHistory = view.findViewById(R.id.btn_sensor_history);

        if (mSensor != null) {
            tvSensorTitle.setText(mSensor.getName());
            etSensorTitle.setText(mSensor.getName());
            tvSensorCodeDisplay.setText("Sensor Code: " + mSensor.getCode());
            tvBattery.setText(mSensor.getBattery() + "%");
        }

        if (ivEdit != null) {
            ivEdit.setVisibility(View.VISIBLE);
            ivEdit.setOnClickListener(v -> {
                ivEdit.setVisibility(View.GONE);
                if (ivSave != null) ivSave.setVisibility(View.VISIBLE);
                etSensorTitle.setVisibility(View.VISIBLE);
                tvSensorTitle.setVisibility(View.INVISIBLE);
            });
        }

        if (ivSave != null) {
            ivSave.setOnClickListener(v -> {
                String newName = etSensorTitle.getText() != null ? etSensorTitle.getText().toString().trim() : "";
                if (!TextUtils.isEmpty(newName) && mSensor != null) {
                    mSensor.setName(newName);
                    tvSensorTitle.setText(newName);
                    SecuredStorageManager.getInstance().addSensor(mSensor);
                    Toast.makeText(getContext(), "Sensor name updated", Toast.LENGTH_SHORT).show();
                }
                ivSave.setVisibility(View.GONE);
                if (ivEdit != null) ivEdit.setVisibility(View.VISIBLE);
                etSensorTitle.setVisibility(View.INVISIBLE);
                tvSensorTitle.setVisibility(View.VISIBLE);
            });
        }

        if (ivSettings != null) {
            ivSettings.setVisibility(View.VISIBLE);
            ivSettings.setOnClickListener(v -> {
                if (mSensor != null && getActivity() != null) {
                    App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.WIFI_MQTT_CONFIGURATION, null);
                }
            });
        }

        if (btnSensorHistory != null) {
            btnSensorHistory.setOnClickListener(v -> {
                if (getActivity() != null) {
                    App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.HISTORY, null);
                }
            });
        }

        if (tvFactoryReset != null) {
            tvFactoryReset.setOnClickListener(v -> AppDialog.showAlertDialog(getContext(), "Factory Reset", "Sensor reset successfully."));
        }
    }
}
