package com.payoda.smartlock.sensors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.DoorSensorServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.sensors.adapter.SensorListAdapter;
import com.payoda.smartlock.sensors.model.Sensor;
import com.payoda.smartlock.sensors.model.Sensors;
import com.payoda.smartlock.sensors.service.DoorSensorService;
import com.payoda.smartlock.utils.AppDialog;

import java.util.ArrayList;
import java.util.HashMap;
import android.text.TextUtils;

public class SensorListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SensorListAdapter.ISensorSelectCallback {

    public static final String TAG = "### SensorListFragment";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoSensorError;
    private FloatingActionButton fbAddSensor;
    private ArrayList<Sensor> sensorList = new ArrayList<>();
    private SensorListAdapter adapter;

    public SensorListFragment() {
        // Required empty public constructor
    }

    public static SensorListFragment getInstance() {
        return new SensorListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_list, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSensors();
    }

    private void initView(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        tvNoSensorError = view.findViewById(R.id.tv_no_sensor_error);
        mRecyclerView = view.findViewById(R.id.sensorListView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fbAddSensor = view.findViewById(R.id.fb_add_sensor);
        fbAddSensor.setOnClickListener(v -> {
            if (getActivity() != null) {
                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.SENSOR_ADD, null);
            }
        });
    }

    private void loadSensors() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "[TEST ALERT] Door Sensor Base URL:\n" + DoorSensorServiceUrl.DOOR_SENSOR_SERVICE_URL, Toast.LENGTH_LONG).show();
        }

        if (getContext() != null && ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(true);
            }
            DoorSensorService.getInstance().getSensorList(100, 0, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (getActivity() != null) {
                        String url = String.format(DoorSensorServiceUrl.WIFI_DOOR_SENSOR_LIST, 100, 0);
                        AppDialog.showAlertDialog(getActivity(), "Door Sensor API Check", "URL Hit:\n" + url + "\n\nResponse:\n" + (data != null ? data.toString() : "empty"));
                    }
                    if (data instanceof Sensors) {
                        Sensors sensors = (Sensors) data;
                        ArrayList<Sensor> newSensors = sensors.getSensorList();
                        ArrayList<Sensor> cachedSensors = SecuredStorageManager.getInstance().getSensorList();
                        HashMap<String, Sensor> cachedMap = new HashMap<>();
                        if (cachedSensors != null) {
                            for (Sensor s : cachedSensors) {
                                if (!TextUtils.isEmpty(s.getId())) cachedMap.put(s.getId(), s);
                                if (!TextUtils.isEmpty(s.getSerialNumber())) cachedMap.put(s.getSerialNumber(), s);
                                if (!TextUtils.isEmpty(s.getCode())) cachedMap.put(s.getCode(), s);
                            }
                        }

                        if (newSensors != null) {
                            ArrayList<Sensor> validSensors = new ArrayList<>();
                            for (Sensor s : newSensors) {
                                // Skip reset or deleted sensors
                                if (s.getStatus() != null && s.getStatus().equalsIgnoreCase(Constant.FACTORY_RESET)) {
                                    continue;
                                }
                                if (SecuredStorageManager.getInstance().isSensorDeleted(s.getId(), s.getSerialNumber(), s.getCode())) {
                                    continue;
                                }

                                Sensor cached = null;
                                if (!TextUtils.isEmpty(s.getId())) cached = cachedMap.get(s.getId());
                                if (cached == null && !TextUtils.isEmpty(s.getSerialNumber())) cached = cachedMap.get(s.getSerialNumber());
                                if (cached == null && !TextUtils.isEmpty(s.getCode())) cached = cachedMap.get(s.getCode());

                                String savedOwnerId = null;
                                if (cached != null && !TextUtils.isEmpty(cached.getOwnerId())) {
                                    savedOwnerId = cached.getOwnerId();
                                }
                                if (TextUtils.isEmpty(savedOwnerId) && !TextUtils.isEmpty(s.getId())) {
                                    savedOwnerId = SecuredStorageManager.getInstance().getSensorOwnerId(s.getId());
                                }
                                if (TextUtils.isEmpty(savedOwnerId) && !TextUtils.isEmpty(s.getSerialNumber())) {
                                    savedOwnerId = SecuredStorageManager.getInstance().getSensorOwnerId(s.getSerialNumber());
                                }
                                if (TextUtils.isEmpty(savedOwnerId) && !TextUtils.isEmpty(s.getCode())) {
                                    savedOwnerId = SecuredStorageManager.getInstance().getSensorOwnerId(s.getCode());
                                }

                                if (!TextUtils.isEmpty(savedOwnerId)) {
                                    s.setOwnerId(savedOwnerId);
                                    if (cached != null && !TextUtils.isEmpty(cached.getStatus())) {
                                        s.setStatus(cached.getStatus());
                                    } else {
                                        s.setStatus(Constant.ACTIVE);
                                    }
                                } else {
                                    // No owner-id on this device yet: sensor must undergo Pre-Activation
                                    s.setStatus(Constant.TRANSFER);
                                }

                                if (cached != null) {
                                    if (TextUtils.isEmpty(s.getScratchCode()) && !TextUtils.isEmpty(cached.getScratchCode())) {
                                        s.setScratchCode(cached.getScratchCode());
                                    }
                                }
                                validSensors.add(s);
                            }
                            sensorList = validSensors;
                            SecuredStorageManager.getInstance().saveSensorList(sensorList);
                        }
                    }
                    renderSensorList();
                }

                @Override
                public void onAuthError(String message) {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (getActivity() != null) {
                        AppDialog.showAlertDialog(getActivity(), "Door Sensor Auth Error", message);
                    }
                    fallbackToLocalStorage();
                }

                @Override
                public void onError(String message) {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (getActivity() != null) {
                        String url = String.format(DoorSensorServiceUrl.WIFI_DOOR_SENSOR_LIST, 100, 0);
                        AppDialog.showAlertDialog(getActivity(), "Door Sensor API Check", "URL Hit:\n" + url + "\n\nServer Error:\n" + message);
                    }
                    fallbackToLocalStorage();
                }
            });
        } else {
            fallbackToLocalStorage();
        }
    }

    private void fallbackToLocalStorage() {
        ArrayList<Sensor> stored = SecuredStorageManager.getInstance().getSensorList();
        sensorList = new ArrayList<>();
        if (stored != null) {
            for (Sensor s : stored) {
                if (s.getStatus() != null && s.getStatus().equalsIgnoreCase(Constant.FACTORY_RESET)) {
                    continue;
                }
                if (SecuredStorageManager.getInstance().isSensorDeleted(s.getId(), s.getSerialNumber(), s.getCode())) {
                    continue;
                }
                sensorList.add(s);
            }
        }
        renderSensorList();
    }

    private void renderSensorList() {
        if (sensorList == null) {
            sensorList = new ArrayList<>();
        }

        if (sensorList.isEmpty()) {
            tvNoSensorError.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            tvNoSensorError.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            adapter = new SensorListAdapter(sensorList, this);
            mRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onRefresh() {
        loadSensors();
    }

    @Override
    public void onSensorSelected(Sensor sensor) {
        if (sensor != null && getActivity() != null) {
            App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.SENSOR_DETAILS, new Gson().toJson(sensor));
        }
    }
}
