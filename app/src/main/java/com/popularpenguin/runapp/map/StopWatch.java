package com.popularpenguin.runapp.map;

import android.os.Handler;
import android.os.SystemClock;

import java.util.Locale;

public class StopWatch {

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

            displayText = String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);

            listener.onUpdate(displayText);

            handler.postDelayed(this, 0);
        }
    };

    public StopWatch(long endTime) {

    }

    public void start() {
        startTime = SystemClock.uptimeMillis();
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

    public String getDisplayText() {
        return displayText;
    }

    private StopWatchListener listener;

    public void setStopWatchListener(StopWatchListener listener) {
        this.listener = listener;
    }

    public interface StopWatchListener {
        void onUpdate(String time);
    }
}
