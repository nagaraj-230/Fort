package com.payoda.smartlock.plugins.storage.lock;



import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.payoda.smartlock.locks.model.Lock;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface LockDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void save(Lock lock);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void saveAll(List<Lock> lockList);

    @Query("Delete from Lock where sync!='0'")
    public void deleteAllLock();

    @Query("Delete from Lock where id=:id")
    public void deleteByLockId(String id);

    @Query("Select * from Lock")
    public List<Lock> getAll();

    @Query("Select * from Lock where sync='0'")
    public List<Lock> getOfflineLock();
}
