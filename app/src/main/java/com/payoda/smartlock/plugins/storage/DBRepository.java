package com.payoda.smartlock.plugins.storage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DBRepository {

    private Context context;

    private static DBRepository instance;
    private String DB_NAME="smartlock_db";

    //our app database object
    private AppDatabase1 appDb;

    static Migration migration1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'Lock' ADD COLUMN 'enable_fp' TEXT DEFAULT '1'");
            database.execSQL("ALTER TABLE 'Lock' ADD COLUMN 'enable_pin' TEXT DEFAULT '1'");
        }
    };

    static Migration migration2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'LockUser' ADD COLUMN 'lock_user_country_code' TEXT DEFAULT 'IN'");

        }
    };

    static Migration migration3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("ALTER TABLE 'Lock' ADD COLUMN 'enable_passage' TEXT DEFAULT '1'");


        }
    };

    private DBRepository(Context context) {
        this.context = context;

        //creating the app database with Room database builder
        appDb = Room.databaseBuilder(context, AppDatabase1.class, DB_NAME).addMigrations(migration1_2, migration2_3,migration3_4).build();
    }

    public static synchronized DBRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DBRepository(context);
        }
        return instance;
    }

    public AppDatabase1 getAppDb() {
        return appDb;
    }
}
