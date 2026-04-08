package com.payoda.smartlock.managepins.adapter;

import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;

public class DigiPinModel {
    private String indexValue;
    private String editTextValue;
    private LinkedTreeMap<Integer, String> pinValue;

    public String getIndexValue() {
        return indexValue;
    }

    public void setIndexValue(String indexValue) {
        this.indexValue = indexValue;
    }

    public String getEditTextValue() {
        return editTextValue;
    }

    public void setEditTextValue(String editTextValue) {
        this.editTextValue = editTextValue;
    }

    public LinkedTreeMap<Integer, String> getPinValue() {
        return pinValue;
    }

    public void setPinValue(LinkedTreeMap<Integer, String> pinValue) {
        this.pinValue = pinValue;
    }
}
