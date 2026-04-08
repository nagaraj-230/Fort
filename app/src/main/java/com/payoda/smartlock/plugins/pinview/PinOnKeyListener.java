package com.payoda.smartlock.plugins.pinview;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.ArrayList;

public class PinOnKeyListener implements OnKeyListener {

    private int currentIndex;
    private ArrayList<AppCompatEditText> editTexts;

    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (keyCode == 67 && event.getAction() == 0) {
            String ediTextValue = String.valueOf(this.editTexts.get(this.currentIndex).getText());
            if (ediTextValue.length() == 0 && this.currentIndex != 0) {
                this.editTexts.get(this.currentIndex - 1).requestFocus();
            }
        }
        return false;
    }

    public PinOnKeyListener(int currentIndex, ArrayList<AppCompatEditText> editTexts) {
        this.currentIndex = currentIndex;
        this.editTexts = editTexts;
    }

}

