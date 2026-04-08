package com.payoda.smartlock.utils;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;

public class AppUtils {


    private static AppUtils instance;

    public static AppUtils getInstance(){

        if (instance == null)
            instance = new AppUtils();

        return instance;

    }

    public  void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void printJavaObject(Object obj){

        Gson gson = new Gson();
        String json = gson.toJson(obj);

        if (obj != null){
            Logger.d(String.format("### ----------------- Starts %s------------------------- " , obj.getClass().getName()));
            // Print the JSON string
            Logger.d("###  Object Data  =  " + json);

            Logger.d("### ----------------- Ends ------------------------- ");
        }



    }


}
