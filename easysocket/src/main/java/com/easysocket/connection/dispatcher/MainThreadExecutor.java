package com.easysocket.connection.dispatcher;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Author：Mapogo
 * Date：2020/4/8
 * Note：切到主线程
 */
public class MainThreadExecutor implements Executor {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(Runnable r) {
        handler.post(r);
    }
}
