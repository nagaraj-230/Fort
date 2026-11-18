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
import com.payoda.smartlock.constants.DoorSensorServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.sensors.model.Sensor;
import com.payoda.smartlock.sensors.service.DoorSensorService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import org.json.JSONObject;

import java.util.UUID;

public class SensorAddFragment extends BaseFragment {

    public static final String TAG = "### SensorAddFragment";

    private EditText etSensorName, etScratchCode;
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
        etScratchCode = view.findViewById(R.id.et_scratch_code);
        btnAddSensor = view.findViewById(R.id.btn_add_sensor);

        btnAddSensor.setOnClickListener(v -> performAddSensor());
    }

    private void performAddSensor() {
        String name = etSensorName.getText() != null ? etSensorName.getText().toString().trim() : "";
        String scratchCode = etScratchCode.getText() != null ? etScratchCode.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            AppDialog.showAlertDialog(getContext(), "Please enter Sensor Name");
            return;
        }

        if (TextUtils.isEmpty(scratchCode)) {
            AppDialog.showAlertDialog(getContext(), "Please enter Scratch Code");
            return;
        }

        if (getContext() == null || !ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
            return;
        }

        Loader.getInstance().showLoader(getContext());

        try {
            JSONObject payload = new JSONObject();
            payload.put("name", name);
            payload.put("scratch_code", scratchCode);

            DoorSensorService.getInstance().addSensor(payload, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    Logger.d(TAG, "addSensor response: " + data);

                    Sensor sensor = new Sensor();
                    sensor.setName(name);
                    sensor.setScratchCode(scratchCode);
                    sensor.setBattery("100");
                    sensor.setStatus(Constant.TRANSFER);

                    try {
                        if (data != null) {
                            JSONObject res = new JSONObject(data.toString());
                            JSONObject dataObj = res.has("data") ? res.optJSONObject("data") : res;
                            if (dataObj != null) {
                                sensor.setId(dataObj.optString("id", UUID.randomUUID().toString()));
                                String serial = dataObj.optString("serial_number", dataObj.optString("serialNumber", ""));
                                if (!TextUtils.isEmpty(serial)) {
                                    sensor.setSerialNumber(serial);
                                    sensor.setCode(serial);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.e(e);
                        sensor.setId(UUID.randomUUID().toString());
                    }

                    SecuredStorageManager.getInstance().addSensor(sensor);
                    Toast.makeText(getContext(), "Sensor added successfully!", Toast.LENGTH_SHORT).show();

                    if (getActivity() != null) {
                        String alertMsg = "TARGET URL:\n" + DoorSensorServiceUrl.ADD_DOOR_SENSOR + "\n\n"
                                + "PAYLOAD SENT:\n" + payload.toString() + "\n\n"
                                + "SERVER RESPONSE:\n" + (data != null ? data.toString() : "empty");

                        AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Add Sensor API Check", alertMsg, (dialog, which) -> {
                            if (getActivity() != null) {
                                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.SENSOR_DETAILS, new Gson().toJson(sensor));
                            }
                        });
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                    AppDialog.showAlertDialog(getContext(), "Auth Error", message);
                }

                @Override
                public void onError(String message) {
                    Loader.getInstance().hideLoader();
                    String alertMsg = "TARGET URL:\n" + DoorSensorServiceUrl.ADD_DOOR_SENSOR + "\n\n"
                            + "PAYLOAD SENT:\n" + payload.toString() + "\n\n"
                            + "SERVER ERROR:\n" + message;
                    AppDialog.showAlertDialog(getContext(), "[TEST ALERT] Add Sensor Error", alertMsg);
                }
            });
        } catch (Exception e) {
            Loader.getInstance().hideLoader();
            Logger.e(e);
            AppDialog.showAlertDialog(getContext(), "Failed to create request: " + e.getMessage());
        }
    }
}
