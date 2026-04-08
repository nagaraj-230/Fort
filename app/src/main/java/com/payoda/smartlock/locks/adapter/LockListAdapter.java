package com.payoda.smartlock.locks.adapter;

import static com.payoda.smartlock.utils.DateTimeUtils.YYYYMMDD_HHMMSS;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.callback.ILockSelectCallback;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by david.
 */

public class LockListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Lock> itemList;
    private ILockSelectCallback iLockSelectCallback;
    private Context context=null;
    Integer i;

    public LockListAdapter(Context context,ArrayList<Lock> lockList, ILockSelectCallback callback) {
        this.itemList = lockList;
        this.iLockSelectCallback = callback;
        this.context=context;

    }

    class LockItemViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relRow,parentRow;
        private TextView lblLockItem;

        public LockItemViewHolder(View itemView) {
            super(itemView);
            relRow = itemView.findViewById(R.id.relRow);
            lblLockItem = itemView.findViewById(R.id.lblLockItem);
            parentRow=itemView.findViewById(R.id.parentRow);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        viewHolder = new LockItemViewHolder(inflater.inflate(R.layout.layout_lock_list_item, viewGroup, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LockItemViewHolder lockItemViewHolder = (LockItemViewHolder) holder;

        final Lock lock = itemList.get(position);
        lockItemViewHolder.lblLockItem.setText(lock.getName());

        if(lock.getLockKeys()!=null && lock.getLockKeys().size() > 1 && lock.getLockKeys().get(1).getIs_schedule_access()!=null &&
                lock.getLockKeys().get(1).getIs_schedule_access().equalsIgnoreCase("1")) {

            String startDate=lock.getLockKeys().get(1).getSchedule_date_from();
            String startTime=lock.getLockKeys().get(1).getSchedule_time_from();

            String endDate=lock.getLockKeys().get(1).getSchedule_date_to();
            String endTime=lock.getLockKeys().get(1).getSchedule_time_to();

            String startDateAndTime= DateTimeUtils.getLocalDateFromGMT(startDate,startTime,YYYYMMDD_HHMMSS);
            String endDateAndTime= DateTimeUtils.getLocalDateFromGMT(endDate,endTime,YYYYMMDD_HHMMSS);

            String startTimeAlone=startDateAndTime.split(" ")[1];
            String endTimeAlone=endDateAndTime.split(" ")[1];

            boolean isAccess = DateTimeUtils.isBetweenTime(startTimeAlone, endTimeAlone);

            if (isAccess) {
                lockItemViewHolder.parentRow.setBackgroundColor(context.getResources().getColor(R.color.white));
            } else {
                lockItemViewHolder.parentRow.setBackgroundColor(context.getResources().getColor(R.color.cancle_button_hover_color));
            }

        }
        else{
            lockItemViewHolder.parentRow.setBackgroundColor(context.getResources().getColor(R.color.white));
        }

        lockItemViewHolder.relRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iLockSelectCallback != null) {
                    iLockSelectCallback.onLockItemSelect(lock);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    public Lock getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public ArrayList<Lock> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<Lock> itemList) {
        if(this.itemList!=null){
            this.itemList.addAll(itemList);
        }else{
            this.itemList=itemList;
        }
        notifyDataSetChanged();
    }

    public void clearList(){
        if(itemList!=null){
            itemList.clear();
        }
    }

}
