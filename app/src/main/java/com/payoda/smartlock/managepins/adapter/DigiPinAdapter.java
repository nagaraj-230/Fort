package com.payoda.smartlock.managepins.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;

public class DigiPinAdapter extends RecyclerView.Adapter<DigiPinAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<DigiPinModel> editModelArrayList;
    private Activity activity;
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
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DigiPinModel digiPinModel = editModelArrayList.get(position);

        // Remove listeners before setting programmatically to avoid recursive updates
        holder.removeListeners();

        holder.indexTextView.setText(digiPinModel.getIndexValue());

        if (digiPinModel.getEditTextValue() == null || digiPinModel.getEditTextValue().isEmpty()) {
            holder.pinNameEditText.setHint("Name");
            holder.pinNameEditText.setText("");
        } else {
            holder.pinNameEditText.setText(digiPinModel.getEditTextValue());
        }

        holder.setTextForPinTextView(digiPinModel.getPinValue());

        // Re-attach listeners after UI is updated
        holder.attachListeners(position);
    }

    @Override
    public int getItemCount() {
        return editModelArrayList != null ? editModelArrayList.size() : 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        protected TextView indexTextView;
        protected EditText pinNameEditText;
        protected AppCompatEditText pinBox1, pinBox2, pinBox3, pinBox4;
        protected ArrayList<AppCompatEditText> otpEditTextViews = new ArrayList<>();

        private TextWatcher nameWatcher;
        private TextWatcher pinWatcher1, pinWatcher2, pinWatcher3, pinWatcher4;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            indexTextView = itemView.findViewById(R.id.tv_index);
            pinNameEditText = itemView.findViewById(R.id.et_pin_name);
            pinBox1 = itemView.findViewById(R.id.pin1);
            pinBox2 = itemView.findViewById(R.id.pin2);
            pinBox3 = itemView.findViewById(R.id.pin3);
            pinBox4 = itemView.findViewById(R.id.pin4);

            otpEditTextViews.add(pinBox1);
            otpEditTextViews.add(pinBox2);
            otpEditTextViews.add(pinBox3);
            otpEditTextViews.add(pinBox4);

            pinBox1.setOnKeyListener(new PinOnKeyListener(0, otpEditTextViews));
            pinBox2.setOnKeyListener(new PinOnKeyListener(1, otpEditTextViews));
            pinBox3.setOnKeyListener(new PinOnKeyListener(2, otpEditTextViews));
            pinBox4.setOnKeyListener(new PinOnKeyListener(3, otpEditTextViews));

            disableSelectionMenu(pinBox1);
            disableSelectionMenu(pinBox2);
            disableSelectionMenu(pinBox3);
            disableSelectionMenu(pinBox4);

            pinNameEditText.setOnFocusChangeListener(focusChangeListener);
            pinBox1.setOnFocusChangeListener(focusChangeListener);
            pinBox2.setOnFocusChangeListener(focusChangeListener);
            pinBox3.setOnFocusChangeListener(focusChangeListener);
            pinBox4.setOnFocusChangeListener(focusChangeListener);
        }

        private final View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.postDelayed(() -> {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            RecyclerView recyclerView = (RecyclerView) itemView.getParent();
                            if (recyclerView != null) {
                                recyclerView.smoothScrollToPosition(pos);
                            }
                        }
                    }, 500);
                }
            }
        };

        public void removeListeners() {
            if (nameWatcher != null) pinNameEditText.removeTextChangedListener(nameWatcher);
            if (pinWatcher1 != null) pinBox1.removeTextChangedListener(pinWatcher1);
            if (pinWatcher2 != null) pinBox2.removeTextChangedListener(pinWatcher2);
            if (pinWatcher3 != null) pinBox3.removeTextChangedListener(pinWatcher3);
            if (pinWatcher4 != null) pinBox4.removeTextChangedListener(pinWatcher4);
        }

        public void attachListeners(final int position) {
            nameWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        editModelArrayList.get(pos).setEditTextValue(s.toString());
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            };
            pinNameEditText.addTextChangedListener(nameWatcher);

            pinWatcher1 = new PinTextWatcher(0, otpEditTextViews, activity, text -> updatePin(getAdapterPosition()));
            pinWatcher2 = new PinTextWatcher(1, otpEditTextViews, activity, text -> updatePin(getAdapterPosition()));
            pinWatcher3 = new PinTextWatcher(2, otpEditTextViews, activity, text -> updatePin(getAdapterPosition()));
            pinWatcher4 = new PinTextWatcher(3, otpEditTextViews, activity, text -> updatePin(getAdapterPosition()));

            pinBox1.addTextChangedListener(pinWatcher1);
            pinBox2.addTextChangedListener(pinWatcher2);
            pinBox3.addTextChangedListener(pinWatcher3);
            pinBox4.addTextChangedListener(pinWatcher4);
        }

        private void updatePin(int pos) {
            if (pos != RecyclerView.NO_POSITION) {
                editModelArrayList.get(pos).setPinValue(getPinValue());
            }
        }

        private void disableSelectionMenu(AppCompatEditText editText) {
            editText.setCustomSelectionActionModeCallback(new DefaultActionModeCallback());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                editText.setCustomInsertionActionModeCallback(new DefaultActionModeCallback() {
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
            pinHash.put(0, pinBox1.getText().toString());
            pinHash.put(1, pinBox2.getText().toString());
            pinHash.put(2, pinBox3.getText().toString());
            pinHash.put(3, pinBox4.getText().toString());
            return pinHash;
        }

        public void setTextForPinTextView(LinkedTreeMap<Integer, String> pinValue) {
            for (int i = 0; i < 4; i++) {
                String val = (pinValue != null && pinValue.containsKey(i)) ? pinValue.get(i) : "";
                otpEditTextViews.get(i).setText(val);
            }
        }
    }

    public static class DefaultActionModeCallback implements ActionMode.Callback {
        @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) { return false; }
        @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }
        @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) { return false; }
        @Override public void onDestroyActionMode(ActionMode mode) {}
    }
}
