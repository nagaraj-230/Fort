package com.payoda.smartlock.plugins.pinview;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatEditText;

import com.payoda.smartlock.R;

import java.util.ArrayList;


public class PinTextWatcher implements TextWatcher {

    private boolean isFirst;
    private boolean isLast;
    private String newTypedString;
    private int currentIndex;
    private ArrayList<AppCompatEditText> editTexts;
    private Activity activity;
    private CustomPinTextListener customPinTextListener;

    public interface CustomPinTextListener {
        public void onTextChanged(String text);
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public PinTextWatcher(int currentIndex, ArrayList<AppCompatEditText> editTexts,
                          Activity activity, CustomPinTextListener customPinTextListener) {

        this.currentIndex = currentIndex;
        this.editTexts = editTexts;
        this.activity = activity;
        this.customPinTextListener = customPinTextListener;
        this.newTypedString = "";

        if (this.currentIndex == 0) {
            this.isFirst = true;
            this.editTexts.get(this.currentIndex).setCursorVisible(true);
        } else if (this.currentIndex == this.editTexts.size() - 1) {
            this.isLast = true;
            this.editTexts.get(this.currentIndex).setCursorVisible(false);
            this.editTexts.get(this.currentIndex).setImeOptions(6);
        } else {
            this.editTexts.get(this.currentIndex).setCursorVisible(false);
        }

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String trim = s.subSequence(start, start + count).toString();
        int startIndex = 0;
        int endIndex = trim.length() - 1;
        boolean startFound = false;

        while(startIndex <= endIndex) {
            int index = !startFound ? startIndex : endIndex;
            char it = trim.charAt(index);
            boolean match = (it < 32? -1: it == 32 ? 0: 1) <= 0;
            if (!startFound) {
                if (!match) {
                    startFound = true;
                } else {
                    ++startIndex;
                }
            } else {
                if (!match) {
                    break;
                }
                --endIndex;
            }
        }
        this.newTypedString = trim.subSequence(startIndex, endIndex + 1).toString();
        customPinTextListener.onTextChanged(this.newTypedString);
    }

    public void afterTextChanged(Editable s) {
        if (this.newTypedString.length() > 1) {
            this.newTypedString = String.valueOf(this.newTypedString.charAt(0));
        }

        this.editTexts.get(this.currentIndex).setOnClickListener((OnClickListener)(new OnClickListener() {
            public final void onClick(View v) {
                getEditTexts().get(getCurrentIndex()).setCursorVisible(true);
            }
        }));
        this.editTexts.get(this.currentIndex).removeTextChangedListener(this);
        this.editTexts.get(this.currentIndex).setText(this.newTypedString);
        this.editTexts.get(this.currentIndex).setSelection(this.newTypedString.length());
        this.editTexts.get(this.currentIndex).addTextChangedListener(this);
        if (this.newTypedString.length() == 1) {
            this.editTexts.get(this.currentIndex).setBackgroundResource(R.drawable.selection_otp_bg);
            this.moveToNext();
        } else if (this.newTypedString.length() == 0) {
            this.editTexts.get(this.currentIndex).setBackgroundResource(R.drawable.otp_square_bg);
            this.moveToPrevious();
        }

    }

    private void moveToNext() {
        if (!this.isLast && !this.isAllEditTextsFilled()) {
            this.editTexts.get(this.currentIndex + 1).requestFocus();
            this.editTexts.get(this.currentIndex).setCursorVisible(true);
        } else {
            this.editTexts.get(this.currentIndex).setImeOptions(6);
            this.editTexts.get(this.currentIndex).setCursorVisible(false);
            this.hideKeyboard();
        }

    }

    private void moveToPrevious() {
        if (!this.isFirst) {
            this.changePreviousViewBackGround();
        }

    }

    private boolean isAllEditTextsFilled() {
        int editTxtLength = this.editTexts.size();

        for(int i = 0; i < editTxtLength; ++i) {
            AppCompatEditText editText = this.editTexts.get(i);
            String trim = String.valueOf(editText.getText());
            int startIndex = 0;
            int endIndex = trim.length() - 1;
            boolean startFound = false;

            while(startIndex <= endIndex) {
                int index = !startFound ? startIndex : endIndex;
                char it = trim.charAt(index);
                boolean match = (it < 32? -1: it == 32 ? 0: 1) <= 0;
                if (!startFound) {
                    if (!match) {
                        startFound = true;
                    } else {
                        ++startIndex;
                    }
                } else {
                    if (!match) {
                        break;
                    }

                    --endIndex;
                }
            }

            if (trim.subSequence(startIndex, endIndex + 1).toString().length() == 0) {
                return false;
            }
        }

        return true;
    }

    private void hideKeyboard() {
        if (this.activity.getCurrentFocus() != null) {
            Object var10000 = this.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            InputMethodManager inputMethodManager = (InputMethodManager)var10000;
            View var10001 = this.activity.getCurrentFocus();
            inputMethodManager.hideSoftInputFromWindow(var10001.getWindowToken(), 0);
        }

    }

    private void changePreviousViewBackGround() {
        Handler handler = new Handler();
        handler.postDelayed((Runnable)(new Runnable() {
            public final void run() {
                if (getCurrentIndex() != 0) {
                    getEditTexts().get(getCurrentIndex()).setBackgroundResource(R.drawable.otp_square_bg);
                    getEditTexts().get(getCurrentIndex() - 1).requestFocus();
                } else {
                    getEditTexts().get(getCurrentIndex()).setBackgroundResource(R.drawable.otp_square_bg);
                }

            }
        }), 50L);
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public ArrayList<AppCompatEditText> getEditTexts() {
        return this.editTexts;
    }

    public Activity getActivity() {
        return this.activity;
    }
}
