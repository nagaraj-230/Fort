package com.payoda.smartlock.sensors;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.UUID;

public class SensorAddFragment extends BaseFragment {

    public static final String TAG = "### SensorAddFragment";

    private EditText etSensorName, etSensorCode;
    private Button btnAddSensor;

    public SensorAddFragment() {
        // Required empty public constructor
    }

    public static SensorAddFragment getInstance() {
        return new SensorAddFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_add, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.add_sensor));

        view.findViewById(R.id.iv_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        etSensorName = view.findViewById(R.id.et_sensor_name);
        etSensorCode = view.findViewById(R.id.et_sensor_code);
        btnAddSensor = view.findViewById(R.id.btn_add_sensor);

        btnAddSensor.setOnClickListener(v -> performAddSensor());
    }

    private void performAddSensor() {
        String name = etSensorName.getText() != null ? etSensorName.getText().toString().trim() : "";
        String code = etSensorCode.getText() != null ? etSensorCode.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            AppDialog.showAlertDialog(getContext(), "Please enter Sensor Name");
            return;
        }

        if (TextUtils.isEmpty(code)) {
            AppDialog.showAlertDialog(getContext(), "Please enter Sensor Code");
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setId(UUID.randomUUID().toString());
        sensor.setName(name);
        sensor.setCode(code);
        sensor.setBattery("100");
        sensor.setStatus("1");

        SecuredStorageManager.getInstance().addSensor(sensor);
        Toast.makeText(getContext(), "Sensor added successfully!", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.SENSOR_DETAILS, new Gson().toJson(sensor));
        }
    }
}
