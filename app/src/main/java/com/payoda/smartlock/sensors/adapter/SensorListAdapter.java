package com.payoda.smartlock.sensors.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.payoda.smartlock.R;
import com.payoda.smartlock.sensors.model.Sensor;

import java.util.ArrayList;

public class SensorListAdapter extends RecyclerView.Adapter<SensorListAdapter.SensorViewHolder> {

    public interface ISensorSelectCallback {
        void onSensorSelected(Sensor sensor);
    }

    private final ArrayList<Sensor> sensorList;
    private final ISensorSelectCallback callback;

    public SensorListAdapter(ArrayList<Sensor> sensorList, ISensorSelectCallback callback) {
        this.sensorList = sensorList != null ? sensorList : new ArrayList<>();
        this.callback = callback;
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sensor_list_item, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        Sensor sensor = sensorList.get(position);
        if (sensor != null) {
            holder.lblSensorItem.setText(sensor.getName());
            holder.lblSensorCode.setText(sensor.getCode() != null ? "Code: " + sensor.getCode() : "");
            holder.itemView.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onSensorSelected(sensor);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    public static class SensorViewHolder extends RecyclerView.ViewHolder {
        TextView lblSensorItem, lblSensorCode;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            lblSensorItem = itemView.findViewById(R.id.lblSensorItem);
            lblSensorCode = itemView.findViewById(R.id.lblSensorCode);
        }
    }
}
