package com.payoda.smartlock.request.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.request.callback.IRequestCallback;
import com.payoda.smartlock.request.model.Request;
import com.payoda.smartlock.request.model.RequestStatus;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

/**
 * Created by david on 6/16/2018.
 */

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String  TAG = "RequestAdapter";

    private Context context;
    private ArrayList<Request> itemList;
    private IRequestCallback iRequestCallback;

    public RequestAdapter(Context context, ArrayList<Request> requestList, IRequestCallback iRequestCallback) {
        this.context = context;
        this.itemList = requestList;
        this.iRequestCallback = iRequestCallback;
    }

    class RequestItemViewHolder extends RecyclerView.ViewHolder {
        private TextView lblRequestName, lblRequestStatus, lblRequestContent;
        private Button btnReject, btnAccept;
        private LinearLayout lvAcceptReject;

        public RequestItemViewHolder(View itemView) {
            super(itemView);
            lblRequestName = itemView.findViewById(R.id.lblRequestName);
            lblRequestStatus = itemView.findViewById(R.id.lblRequestStatus);
            lblRequestContent = itemView.findViewById(R.id.lblRequestContent);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            lvAcceptReject = itemView.findViewById(R.id.linAcceptReject);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        Logger.d(TAG, TAG);
        viewHolder = new RequestItemViewHolder(inflater.inflate(R.layout.layout_request_item, viewGroup, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RequestItemViewHolder requestItemViewHolder = (RequestItemViewHolder) holder;
        final Request mRequest = itemList.get(position);
        requestItemViewHolder.lblRequestName.setText(mRequest.getFromUser().getUsername());
        requestItemViewHolder.lblRequestContent.setText(String.format(this.context.getString(R.string.text_content), mRequest.getFromUser().getUsername(), mRequest.getLock().getName(), mRequest.getKey().getUserType().replace("ID","")));
        requestItemViewHolder.btnAccept.setOnClickListener(view -> {
            if (iRequestCallback != null) {
                iRequestCallback.onRequestAction(mRequest, true);
            }
        });
        requestItemViewHolder.btnReject.setOnClickListener(view -> {
            if (iRequestCallback != null) {
                iRequestCallback.onRequestAction(mRequest, false);
            }
        });

        requestItemViewHolder.lvAcceptReject.setVisibility(View.GONE);
        if (mRequest.getRequestStatus().equals(RequestStatus.APPROVED.getId())) {
            requestItemViewHolder.lblRequestStatus.setVisibility(View.VISIBLE);
            requestItemViewHolder.lblRequestStatus.setText(this.context.getString(R.string.text_accepted));
            requestItemViewHolder.lblRequestStatus.setTextColor(ContextCompat.getColor(this.context, R.color.green));
        }
        else if (mRequest.getRequestStatus().equals(RequestStatus.REJECTED.getId())) {
            requestItemViewHolder.lblRequestStatus.setVisibility(View.VISIBLE);
            requestItemViewHolder.lblRequestStatus.setText(this.context.getString(R.string.text_rejected));
            requestItemViewHolder.lblRequestStatus.setTextColor(ContextCompat.getColor(this.context, R.color.red));
        }
        else if (mRequest.getRequestStatus().equals(RequestStatus.NOTASSIGNED.getId())) {
            requestItemViewHolder.lvAcceptReject.setVisibility(View.VISIBLE);
            requestItemViewHolder.lblRequestStatus.setVisibility(View.INVISIBLE);
        }
        else {
            requestItemViewHolder.lblRequestStatus.setVisibility(View.VISIBLE);
            requestItemViewHolder.lblRequestStatus.setText(this.context.getString(R.string.withdrawn));
            requestItemViewHolder.lblRequestStatus.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimary));
        }


    }

    @Override
    public int getItemCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    public Request getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public ArrayList<Request> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<Request> itemList) {
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
