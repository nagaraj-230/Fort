package com.payoda.smartlock.managepins.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

public class OtpAdapter extends RecyclerView.Adapter<OtpAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    public static ArrayList<OtpModel> otpModelArrayList;
    private static Activity activity;

    public OtpAdapter(Activity activity, Context ctx, ArrayList<OtpModel> otpModelArrayList) {
        inflater = LayoutInflater.from(ctx);
        this.activity = activity;
        this.otpModelArrayList = otpModelArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_otp_item, parent, false);
        OtpAdapter.MyViewHolder holder = new OtpAdapter.MyViewHolder(view);

        return holder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        OtpModel otpModel = otpModelArrayList.get(holder.getAdapterPosition());

        if(otpModel.getRequestTime() != null ){
            holder.otpRequestTime.setText(DateTimeUtils.getLocalDateFromGMT(otpModel.getRequestTime()));
        }



        if(otpModel.getStatus() != null && otpModel.getStatus().equalsIgnoreCase("1")){
            holder.otpTextView.setText(otpModel.getOtp());
            holder.otpTextView.setTextColor(activity.getResources().getColor(R.color.black));
            holder.otpRequestTime.setTextColor(activity.getResources().getColor(R.color.black));
        }
        else if(otpModel.getStatus() != null && otpModel.getStatus().equalsIgnoreCase("2")){
            holder.otpTextView.setText(otpModel.getOtp());
            holder.otpTextView.setTextColor(activity.getResources().getColor(R.color.label_light_grey_color));
            holder.otpRequestTime.setTextColor(activity.getResources().getColor(R.color.label_light_grey_color));
        }

    }

    @Override
    public int getItemCount() {
        return otpModelArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        protected TextView otpTextView;
        protected TextView otpRequestTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            otpTextView = (TextView) itemView.findViewById(R.id.tv_otp);
            otpRequestTime = (TextView) itemView.findViewById(R.id.tv_otp_request_time);
        }
    }

}
