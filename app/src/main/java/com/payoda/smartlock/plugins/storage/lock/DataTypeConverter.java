package com.payoda.smartlock.plugins.storage.lock;


import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.payoda.smartlock.locks.model.LockKeys;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DataTypeConverter {
    private static Gson gson = new Gson();
    @TypeConverter
    public static ArrayList<LockKeys> stringToList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<LockKeys>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String ListToString(ArrayList<LockKeys> someObjects) {
        return gson.toJson(someObjects);
    }
}
