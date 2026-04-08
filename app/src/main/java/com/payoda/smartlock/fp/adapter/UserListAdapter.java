package com.payoda.smartlock.fp.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.users.model.User;

import java.util.ArrayList;

public abstract class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<User> users;

    public UserListAdapter(ArrayList<User> users) {
        this.users = users;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout parentRow;
        private TextView lblUserName;

        public UserViewHolder(View itemView) {
            super(itemView);
            lblUserName = itemView.findViewById(R.id.lblUserName);
            parentRow=itemView.findViewById(R.id.parentRow);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        viewHolder = new UserViewHolder(inflater.inflate(R.layout.layout_user_list, viewGroup, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserViewHolder viewHolder = (UserViewHolder) holder;
        User user = users.get(position);
        viewHolder.lblUserName.setText(user.getUsername());
        viewHolder.parentRow.setTag(position+"");
        viewHolder.parentRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index=Integer.parseInt(view.getTag().toString());
                onUserSelect(users.get(index));
            }
        });

    }

    @Override
    public int getItemCount() {
        if (users != null)
            return users.size();
        return 0;
    }

    public User getItem(int position) {
        if (users != null)
            return users.get(position);
        return null;
    }

    public abstract void onUserSelect(User user);



}
