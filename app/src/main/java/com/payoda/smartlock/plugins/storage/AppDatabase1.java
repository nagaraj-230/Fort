package com.payoda.smartlock.plugins.storage;



import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.LockUser;
import com.payoda.smartlock.locks.model.RequestDetail;
import com.payoda.smartlock.plugins.storage.lock.LockDAO;

@Database(entities = {Lock.class, LockKeys.class, LockUser.class, RequestDetail.class}, version = 4,exportSchema = false)
public abstract class AppDatabase1 extends RoomDatabase {
    public abstract LockDAO lockDAO();
}
