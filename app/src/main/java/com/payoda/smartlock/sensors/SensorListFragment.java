package com.payoda.smartlock.sensors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.sensors.adapter.SensorListAdapter;
import com.payoda.smartlock.sensors.model.Sensor;

import java.util.ArrayList;

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
        sensorList = SecuredStorageManager.getInstance().getSensorList();
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
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
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
