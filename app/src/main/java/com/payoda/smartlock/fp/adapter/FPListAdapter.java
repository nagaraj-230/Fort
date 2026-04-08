package com.payoda.smartlock.fp.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.LockUser;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class FPListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private HashMap<String,ArrayList<LockKeys>> fpUsers;
    private ArrayList<String> ids;
    public FPListAdapter(HashMap<String,ArrayList<LockKeys>> fpUsers, ArrayList<String> ids) {
        this.fpUsers = fpUsers;
        this.ids=ids;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView lblFPName,lblAdd,lblRevoke,lblEdit,lblFPCount;

        public UserViewHolder(View itemView) {
            super(itemView);
            lblFPName = itemView.findViewById(R.id.lblFPName);
            lblFPCount=itemView.findViewById(R.id.lblFPCount);
            lblAdd=itemView.findViewById(R.id.lblAdd);
            lblRevoke=itemView.findViewById(R.id.lblRevoke);
            lblEdit=itemView.findViewById(R.id.lblEdit);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        viewHolder = new UserViewHolder(inflater.inflate(R.layout.layout_fp, viewGroup, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserViewHolder viewHolder = (UserViewHolder) holder;
        LockKeys user = fpUsers.get(ids.get(position)).get(0);


        viewHolder.lblAdd.setTag(""+position);
        viewHolder.lblRevoke.setTag(""+position);
        viewHolder.lblEdit.setTag(""+position);

        viewHolder.lblFPCount.setText("("+user.getOriginalFPIds().size()+")");

        if(user.getLockUser()!=null){
            LockUser registerUser=user.getLockUser();
            viewHolder.lblFPName.setText(registerUser.getUsername());
        }else{
            LockUser registerUser=user.getRegistrationDetails();
            viewHolder.lblFPName.setText(registerUser.getName());
        }

        if(user.getUserId()!=null){
            viewHolder.lblEdit.setVisibility(View.GONE);
        }else{
            viewHolder.lblEdit.setVisibility(View.VISIBLE);
        }
        viewHolder.lblAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index=Integer.parseInt(view.getTag().toString());
                onUserAdd(fpUsers.get(ids.get(position)));
            }
        });
        viewHolder.lblRevoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index=Integer.parseInt(view.getTag().toString());
                onUserRevoke(fpUsers.get(ids.get(position)));
            }
        });

        viewHolder.lblEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index=Integer.parseInt(view.getTag().toString());
                onUserEdit(fpUsers.get(ids.get(position)));
            }
        });

    }

    @Override
    public int getItemCount() {
        if (ids != null)
            return ids.size();
        return 0;
    }

    public ArrayList<LockKeys> getItem(int position) {
        if (fpUsers != null)
            return fpUsers.get(position);
        return null;
    }

    public abstract void onUserAdd(ArrayList<LockKeys> users);
    public abstract void onUserRevoke(ArrayList<LockKeys> users);
    public abstract void onUserEdit(ArrayList<LockKeys> users);


}
