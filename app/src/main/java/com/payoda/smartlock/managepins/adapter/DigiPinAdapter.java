package com.payoda.smartlock.managepins.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.internal.LinkedTreeMap;
import com.payoda.smartlock.R;
import com.payoda.smartlock.plugins.pinview.PinOnKeyListener;
import com.payoda.smartlock.plugins.pinview.PinTextWatcher;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

public class DigiPinAdapter extends RecyclerView.Adapter<DigiPinAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    public static ArrayList<DigiPinModel> editModelArrayList;
    private static Activity activity;
    private boolean isFirstLoad;

    public DigiPinAdapter(Activity activity, Context ctx, ArrayList<DigiPinModel> editModelArrayList, boolean isFirstLoad) {

        inflater = LayoutInflater.from(ctx);
        this.activity = activity;
        this.editModelArrayList = editModelArrayList;
        this.isFirstLoad = isFirstLoad;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_digi_pin_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

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

        DigiPinModel digiPinModel = editModelArrayList.get(holder.getAdapterPosition());

        holder.indexTextView.setText(digiPinModel.getIndexValue());

        if (digiPinModel.getEditTextValue() == null || digiPinModel.getEditTextValue().isEmpty()) {
            holder.pinNameEditText.setHint("Name");
            holder.pinNameEditText.setText("");
        } else {
            holder.pinNameEditText.setText(digiPinModel.getEditTextValue());
        }

        holder.setTextForPinTextView(digiPinModel.getPinValue(), position);
    }

    @Override
    public int getItemCount() {
        return editModelArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        protected TextView indexTextView;
        protected EditText pinNameEditText;

        protected AppCompatEditText pinBox1;
        protected AppCompatEditText pinBox2;
        protected AppCompatEditText pinBox3;
        protected AppCompatEditText pinBox4;

        protected ArrayList<AppCompatEditText> otpEditTextViews = new ArrayList<AppCompatEditText>();

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            indexTextView = (TextView) itemView.findViewById(R.id.tv_index);

            pinNameEditText = (EditText) itemView.findViewById(R.id.et_pin_name);

            pinBox1 = (AppCompatEditText) itemView.findViewById(R.id.pin1);
            pinBox2 = (AppCompatEditText) itemView.findViewById(R.id.pin2);
            pinBox3 = (AppCompatEditText) itemView.findViewById(R.id.pin3);
            pinBox4 = (AppCompatEditText) itemView.findViewById(R.id.pin4);

            otpEditTextViews.add(pinBox1);
            otpEditTextViews.add(pinBox2);
            otpEditTextViews.add(pinBox3);
            otpEditTextViews.add(pinBox4);

            pinBox1.addTextChangedListener(new PinTextWatcher(0, otpEditTextViews, activity, text -> editModelArrayList.get(getAdapterPosition()).setPinValue(getPinValue())));
            pinBox2.addTextChangedListener(new PinTextWatcher(1, otpEditTextViews, activity, text -> editModelArrayList.get(getAdapterPosition()).setPinValue(getPinValue())));
            pinBox3.addTextChangedListener(new PinTextWatcher(2, otpEditTextViews, activity, text -> editModelArrayList.get(getAdapterPosition()).setPinValue(getPinValue())));
            pinBox4.addTextChangedListener(new PinTextWatcher(3, otpEditTextViews, activity, text -> editModelArrayList.get(getAdapterPosition()).setPinValue(getPinValue())));

            pinBox1.setOnKeyListener(new PinOnKeyListener(0, otpEditTextViews));
            pinBox2.setOnKeyListener(new PinOnKeyListener(1, otpEditTextViews));
            pinBox3.setOnKeyListener(new PinOnKeyListener(2, otpEditTextViews));
            pinBox4.setOnKeyListener(new PinOnKeyListener(3, otpEditTextViews));

            disableSelectionMenu(pinBox1);
            disableSelectionMenu(pinBox2);
            disableSelectionMenu(pinBox3);
            disableSelectionMenu(pinBox4);

            pinNameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    editModelArrayList.get(getAdapterPosition()).setEditTextValue(pinNameEditText.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            /*pinNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        pinNameEditText.setHint("");
                    else
                        pinNameEditText.setHint("Name");
                }
            });*/

        }

        private void disableSelectionMenu(AppCompatEditText editText) {
            editText.setCustomSelectionActionModeCallback(new DigiPinAdapter.DefaultActionModeCallback());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                editText.setCustomInsertionActionModeCallback(new DigiPinAdapter.DefaultActionModeCallback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        menu.removeItem(android.R.id.autofill);
                        return true;
                    }
                });

            }
        }

        public LinkedTreeMap<Integer, String> getPinValue() {
            LinkedTreeMap<Integer, String> pinHash = new LinkedTreeMap<>();
            if (pinBox1 != null && !pinBox1.getText().toString().isEmpty()) {
                pinHash.put(0, pinBox1.getText().toString());
            } else {
                pinHash.put(0, "");
            }
            if (pinBox2 != null && !pinBox2.getText().toString().isEmpty()) {
                pinHash.put(1, pinBox2.getText().toString());
            } else {
                pinHash.put(1, "");
            }
            if (pinBox3 != null && !pinBox3.getText().toString().isEmpty()) {
                pinHash.put(2, pinBox3.getText().toString());
            } else {
                pinHash.put(2, "");
            }
            if (pinBox4 != null && !pinBox4.getText().toString().isEmpty()) {
                pinHash.put(3, pinBox4.getText().toString());
            } else {
                pinHash.put(3, "");
            }
            return pinHash;
        }

        public void setTextForPinTextView(LinkedTreeMap<Integer, String> pinValue, int position) {
            int i = 0;
            if (pinValue.isEmpty()) {
                while (i < 4) {
//                    otpEditTextViews.get(i).setText("");
                    i++;
                }
            } else {
                for (LinkedTreeMap.Entry<Integer, String> entry : pinValue.entrySet()) {
                    otpEditTextViews.get(i).setText(entry.getValue());
                    i++;
                }
            }
        }
    }

    public static class DefaultActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

}
