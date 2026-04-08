package com.payoda.smartlock.plugins.storage.lock;

import android.app.Activity;
import android.content.Context;

import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.plugins.storage.DBRepository;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class LockDBClient {

    private static LockDBClient instance=null;
    public static final String SUCCESS="success";

    public static LockDBClient getInstance(){
        if(instance==null){
            instance=new LockDBClient();
        }
        return instance;
    }

    public void save(Lock lock, Context context) {
        if(lock!=null && lock.getId()!=null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Logger.d("#### LockDBClient save " );
                    Logger.d("#### lock.getId() " + lock.getId());
                    DBRepository.getInstance(context)
                            .getAppDb()
                            .lockDAO()
                            .save(lock);
                }
            }).start();
        }
    }

    public void saveAll(List<Lock> lockList, Context context) {
        if(lockList!=null && lockList.size()>0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBRepository.getInstance(context)
                            .getAppDb()
                            .lockDAO()
                            .saveAll(lockList);
                }
            }).start();
        }
    }

    public void getAll(Context context,DBCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Lock> lockList= DBRepository.getInstance(context)
                        .getAppDb()
                        .lockDAO()
                        .getAll();
                ArrayList<Lock> convertedList=new ArrayList<>(lockList);
                if(context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onLockList(convertedList);
                        }
                    });
                }else{
                    callback.onLockList(convertedList);
                }

            }
        }).start();
    }

    public void getOfflineLocks(Context context,DBCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Lock> lockList= DBRepository.getInstance(context)
                        .getAppDb()
                        .lockDAO()
                        .getOfflineLock();
                ArrayList<Lock> convertedList=new ArrayList<>(lockList);
                if(context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onLockList(convertedList);
                        }
                    });
                }else{
                    callback.onLockList(convertedList);
                }

            }
        }).start();
    }

    public void deleteLockById(Context context,Lock lock,DBCallback callback) {
        if(lock!=null && lock.getId()!=null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBRepository.getInstance(context)
                            .getAppDb()
                            .lockDAO()
                            .deleteByLockId(lock.getId());
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(SUCCESS);
                            }
                        });
                    } else {
                        callback.onSuccess(SUCCESS);
                    }

                }
            }).start();
        }
    }

    public void deleteAllLock(Context context, DBCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBRepository.getInstance(context)
                        .getAppDb()
                        .lockDAO()
                        .deleteAllLock();
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(SUCCESS);
                        }
                    });
                } else {
                    callback.onSuccess(SUCCESS);
                }
            }
        }).start();
    }

    public interface DBCallback{
        public void onLockList(ArrayList<Lock> lockList);
        public void onSuccess(String msg);
    }

}
