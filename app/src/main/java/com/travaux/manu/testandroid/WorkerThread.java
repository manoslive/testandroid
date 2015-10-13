package com.travaux.manu.testandroid;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


/**
 * Created by Manu on 2015-10-13.
 */
public class WorkerThread extends Thread {
    private Handler workerHandler;
    private Handler uiHandler;

    public WorkerThread(Handler h) {
        uiHandler = h;
    }

    @Override
    public void run() {
        Looper.prepare();
        workerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg){

            }
        };
    }
}
