package com.payoda.smartlock.history.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class HistoryList {

    @SerializedName("data")
    public ArrayList<History> history;

    public ArrayList<History> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<History> history) {
        this.history = history;
    }
}
