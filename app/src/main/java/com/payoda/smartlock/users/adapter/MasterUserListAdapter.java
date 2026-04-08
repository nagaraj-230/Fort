package com.payoda.smartlock.users.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.LockUser;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.payoda.smartlock.utils.DateTimeUtils.YYYYMMDD_HHMMSS;

/**
 * Created by david on 06/09/18.
 */

public abstract class MasterUserListAdapter extends BaseExpandableListAdapter {

    private Context context;

    private List<LockKeys> listMaster;
    private HashMap<String, ArrayList<LockKeys>> listUser;

    public static final int ACTION_ADD = 0;
    public static final int ACTION_WITHDRAW = 1;
    public static final int ACTION_REVOKE = 2;

    private String privilege;
    private String lockVersion;

    public MasterUserListAdapter(Context context, List<LockKeys> listMaster,
                                 HashMap<String, ArrayList<LockKeys>> listUser, String privilege,String lockVersion) {
        this.context = context;
        this.listMaster = listMaster;
        this.listUser = listUser;
        this.privilege = privilege;
        this.lockVersion=lockVersion;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listUser.get(listMaster.get(groupPosition).getId())
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @SuppressLint("NewApi")
    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View view, ViewGroup parent) {


        final LockKeys lockKey = (LockKeys) getChild(groupPosition, childPosition);
        LockKeys groupLockKey = (LockKeys) getGroup(groupPosition);

        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.layout_user_row, null);
        }

        TextView lblUser = view.findViewById(R.id.lblUser);
        TextView lblAdd = view.findViewById(R.id.lblAdd);
        ImageView imgInfo = view.findViewById(R.id.imgInfo);
        ImageView imgSchedule = view.findViewById(R.id.imgSchedule);

        int actionType = getActionType(lockKey.getStatus(), lockKey.getLockUser());
        String label;
        String prefix = "";
        if (groupLockKey.getId().equalsIgnoreCase("OWNER") || groupLockKey.getId().equalsIgnoreCase("Master")) {
            lblAdd.setVisibility(View.VISIBLE);
        } else {
            lblAdd.setVisibility(View.INVISIBLE);
        }

        if (actionType == ACTION_ADD) {
            imgInfo.setVisibility(View.INVISIBLE);
            label = context.getResources().getString(R.string.add_text);
            prefix = "Add ";
        } else if (actionType == ACTION_WITHDRAW) {
            imgInfo.setVisibility(View.VISIBLE);
            label = context.getResources().getString(R.string.withdraw_text);
        } else {
            lblAdd.setVisibility(View.VISIBLE);
            imgInfo.setVisibility(View.VISIBLE);
            label = context.getResources().getString(R.string.revoke_text);
        }

        if (label.equalsIgnoreCase(context.getResources().getString(R.string.revoke_text))) {

            imgSchedule.setVisibility(View.VISIBLE);
            imgSchedule.setImageResource(R.drawable.ic_calendar_disable);
            if (lockKey.getIs_schedule_access() != null &&
                    lockKey.getIs_schedule_access().equalsIgnoreCase("1")) {

                String startDate = lockKey.getSchedule_date_from();
                String startTime = lockKey.getSchedule_time_from();

                String endDate = lockKey.getSchedule_date_to();
                String endTime = lockKey.getSchedule_time_to();

                String startDateAndTime = DateTimeUtils.getLocalDateFromGMT(startDate, startTime, YYYYMMDD_HHMMSS);
                String endDateAndTime = DateTimeUtils.getLocalDateFromGMT(endDate, endTime, YYYYMMDD_HHMMSS);

                String startDateAlone = startDateAndTime.split(" ")[0];
                String endDateAlone = endDateAndTime.split(" ")[0];

                if (DateTimeUtils.isBetweenDate(startDateAlone, endDateAlone)) {
                    imgSchedule.setImageResource(R.drawable.ic_calendar_enable);
                }
            } else {
                if (groupPosition != 3 && listMaster.size() == 4) {
                    imgSchedule.setVisibility(View.INVISIBLE);
                }
            }

        } else {
            imgSchedule.setVisibility(View.INVISIBLE);
        }


        lblUser.setText(prefix + lockKey.getName());
        lblAdd.setText(label);
        lblAdd.setTag(lockKey);
        imgInfo.setTag(lockKey);
        imgSchedule.setTag(lockKey);
        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockKeys selectedKey = (LockKeys) view.getTag();
                onInfoIconClick(selectedKey);
            }
        });

        lblAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockKeys selectedKey = (LockKeys) view.getTag();
                RequestUser selectedUser = getRequestUser(selectedKey);
                TextView selectedText = (TextView) view;

                Logger.d("@@@@@ selectedKey : "+selectedKey);
                Logger.d("@@@@@ selectedUser : "+selectedUser);
                Logger.d("@@@@@ selectedText : "+selectedText);
                Logger.d("@@@@@ selectedText.getText().toString() : "+selectedText.getText().toString());

                onActionButtonClick(selectedText.getText().toString(), selectedKey, selectedUser);
            }
        });

        imgSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockKeys selectedKey = (LockKeys) view.getTag();
                onScheduleIconClick(selectedKey);
            }
        });

        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String id = listMaster.get(groupPosition).getId();
        return listUser.get(id) != null ? listUser.get(id).size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listMaster.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listMaster.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View view, ViewGroup parent) {

        LockKeys lockKey = (LockKeys) getGroup(groupPosition);

        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.layout_master_row, null);
        }


        TextView lblUser = view.findViewById(R.id.lblUser);
        TextView lblAdd = view.findViewById(R.id.lblAdd);
        ImageView imgInfo = view.findViewById(R.id.imgInfo);
        ImageView imgIndicator = view.findViewById(R.id.imgIndicator);
        ImageView imgSchedule = view.findViewById(R.id.imgSchedule);
        ImageView imgFPAccess = view.findViewById(R.id.imgFPAccess);


        if (isExpanded) {
            imgIndicator.setImageResource(R.drawable.ic_minus);
        } else {
            imgIndicator.setImageResource(R.drawable.ic_plus);
        }

        if (groupPosition == (listMaster.size() - 1)) {
            imgIndicator.setVisibility(View.GONE);
        } else {
            imgIndicator.setVisibility(View.VISIBLE);
        }
        String prefix = "";
        if (lockKey.getId().equalsIgnoreCase("OWNER") || lockKey.getId().equalsIgnoreCase("Master")) {
            lblAdd.setVisibility(View.INVISIBLE);
            imgInfo.setVisibility(View.INVISIBLE);
        } else {
            lblAdd.setVisibility(View.VISIBLE);
            int actionType = getActionType(lockKey.getStatus(), lockKey.getLockUser());
            String label;
            if (actionType == ACTION_ADD) {
                imgInfo.setVisibility(View.INVISIBLE);
                label = context.getResources().getString(R.string.add_text);
                prefix = "Add ";
            } else if (actionType == ACTION_WITHDRAW) {
                imgInfo.setVisibility(View.VISIBLE);
                label = context.getResources().getString(R.string.withdraw_text);
            } else {
                imgInfo.setVisibility(View.VISIBLE);
                label = context.getResources().getString(R.string.revoke_text);
            }
            lblAdd.setText(label);
        }

        if (lblAdd.getText().toString().equalsIgnoreCase(context.getResources().getString(R.string.revoke_text))) {

            imgSchedule.setVisibility(View.VISIBLE);
            imgSchedule.setImageResource(R.drawable.ic_calendar_disable);

            if (lockKey.getIs_schedule_access() != null &&
                    lockKey.getIs_schedule_access().equalsIgnoreCase("1")) {
                if (DateTimeUtils.isBetweenDate(lockKey.getSchedule_date_from(), lockKey.getSchedule_date_to())) {
                    imgSchedule.setImageResource(R.drawable.ic_calendar_enable);
                }
            }




            if(lockVersion!=null && (lockVersion.equalsIgnoreCase(Constant.HW_VERSION_2)
                    || lockVersion.equalsIgnoreCase(Constant.HW_VERSION_2_1)
                    || lockVersion.equalsIgnoreCase(Constant.HW_VERSION_3)
                    || lockVersion.equalsIgnoreCase(Constant.HW_VERSION_3_1)
                    || lockVersion.equalsIgnoreCase(Constant.HW_VERSION_3_2)
                    || lockVersion.equalsIgnoreCase(Constant.HW_VERSION_4_0)
                    || lockVersion.equalsIgnoreCase(Constant.HW_VERSION_6_0))) {


                if (groupPosition == (listMaster.size() - 1)) {

                    imgFPAccess.setVisibility(View.GONE);

                } else if (privilege != null) {

                    imgFPAccess.setVisibility(View.VISIBLE);

                    if (privilege.equalsIgnoreCase(lockKey.getUserId())) {

                        imgFPAccess.setImageResource(R.drawable.ic_fp_access_granted);

                    }else{

                        imgFPAccess.setImageResource(R.drawable.ic_fp);

                    }

                } else {


                    imgFPAccess.setVisibility(View.VISIBLE);


                    imgFPAccess.setImageResource(R.drawable.ic_fp);
                }
            }

            else{
                imgFPAccess.setVisibility(View.GONE);
            }





        } else {
            imgSchedule.setVisibility(View.INVISIBLE);
            imgFPAccess.setVisibility(View.GONE);
        }



        lblUser.setText(prefix + lockKey.getName());
        lblAdd.setTag(lockKey);
        imgInfo.setTag(lockKey);
        imgFPAccess.setTag(lockKey);
        imgSchedule.setTag(lockKey);

        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockKeys selectedKey = (LockKeys) view.getTag();
                onInfoIconClick(selectedKey);
            }
        });

        imgFPAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockKeys selectedKey = (LockKeys) view.getTag();
                onFPAccessButtonClick(selectedKey);
            }
        });

        lblAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockKeys selectedKey = (LockKeys) view.getTag();
                RequestUser selectedUser = getRequestUser(selectedKey);
                TextView selectedText = (TextView) view;

                Logger.d("@@@@@ selectedKey : "+selectedKey);
                Logger.d("@@@@@ selectedUser : "+selectedUser);
                Logger.d("@@@@@ selectedText : "+selectedText);
                Logger.d("@@@@@ selectedText.getText().toString() : "+selectedText.getText().toString());



                onActionButtonClick(selectedText.getText().toString(), selectedKey, selectedUser);
            }
        });

        imgSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockKeys selectedKey = (LockKeys) view.getTag();
                onScheduleIconClick(selectedKey);
            }
        });

        return view;

    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public abstract void onInfoIconClick(LockKeys lockKeys);

    public abstract void onScheduleIconClick(LockKeys lockKeys);

    public abstract void onActionButtonClick(String action, LockKeys lockKeys, RequestUser requestUser);

    public abstract void onFPAccessButtonClick(LockKeys lockKeys);

    private RequestUser getRequestUser(LockKeys lockKeys) {
        RequestUser requestUser = new RequestUser();
        requestUser.setKeyId(lockKeys.getId());
        requestUser.setSlotNumber(lockKeys.getSlotNumber());
        requestUser.setKey(lockKeys.getKey());
        requestUser.setUserId(lockKeys.getUserId());
        if (lockKeys.getStatus() != null && lockKeys.getLockUser() != null) {
            if (getActionType(lockKeys.getStatus(), lockKeys.getLockUser()) == ACTION_WITHDRAW) {
                if (lockKeys.getRequestDetail() != null) {
                    requestUser.setRequestId(lockKeys.getRequestDetail().getId());
                }
            } else {
                requestUser.setRequestId(lockKeys.getId());
            }
        }
        return requestUser;
    }

    private int getActionType(String status, LockUser lockUser) {
        switch (status) {
            case Constant.INACTIVE:
                if (lockUser == null) {
                    return ACTION_ADD;
                } else {
                    return ACTION_WITHDRAW;
                }
            case Constant.ACTIVE:
                if (lockUser != null) {
                    return ACTION_REVOKE;
                }

            default:
                break;
        }
        return ACTION_ADD;
    }

    public void setValue(List<LockKeys> listMaster,
                         HashMap<String, ArrayList<LockKeys>> listUser) {
        this.listMaster = listMaster;
        this.listUser = listUser;
        notifyDataSetChanged();
    }

    public void setPrivilege(String privilege){
        this.privilege=privilege;
        notifyDataSetChanged();
    }

}

