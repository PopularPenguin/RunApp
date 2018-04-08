package com.popularpenguin.runapp.map;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.util.Locale;

public class StopWatchService extends Service {

    private static final String TAG = StopWatchService.class.getSimpleName();

    public static final String START_TIME_EXTRA = "startTime";

    private IBinder mBinder = new StopWatchBinder();

    private long startTime, millisTime, timeBuffer, updateTime = 0L;
    private int hours, minutes, seconds, millis;

    private String displayText;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            millisTime = SystemClock.uptimeMillis() - startTime;
            updateTime = timeBuffer + millisTime;
            millis = (int) (updateTime % 1000);
            seconds = (int) (updateTime / 1000);
            minutes = seconds / 60;
            hours = minutes / 60;

            seconds %= 60; // reset the seconds field to 0 when passing 60 seconds
            minutes %= 60; // reset the minutes field to 0 when passing 60 minutes

            displayText = String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);

            if (listener != null) {
                listener.onStopWatchUpdate(displayText);
            }

            handler.postDelayed(this, 0);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        handler.postDelayed(runnable, 0);

        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        handler.postDelayed(runnable, 0);

        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        handler.removeCallbacks(runnable);

        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long offset = intent.getLongExtra(START_TIME_EXTRA, 0L);
        start(offset);

        return START_STICKY;
    }

    public long getTime() {
        return millisTime;
    }

    public void start(long offset) {
        startTime = SystemClock.uptimeMillis() - offset;
        handler.postDelayed(runnable, 0);
    }

    public void pause() {
        timeBuffer += millisTime;
        handler.removeCallbacks(runnable);
    }

    public void reset() {
        millisTime = 0L;
        startTime = 0L;
        timeBuffer = 0L;
        updateTime = 0L;
        hours = 0;
        minutes = 0;
        seconds = 0;

        displayText = "0:00:00";
    }

    public class StopWatchBinder extends Binder {
        StopWatchService getService() {
            return StopWatchService.this;
        }
    }

    private StopWatchListener listener;

    public void setStopWatchListener(StopWatchListener listener) {
        this.listener = listener;
    }

    public interface StopWatchListener {
        void onStopWatchUpdate(String time);
    }
}
