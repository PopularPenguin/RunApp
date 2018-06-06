package com.popularpenguin.runapp.map;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.util.Locale;

/** Stopwatch to track time spent doing a challenge */
// https://www.android-examples.com/android-create-stopwatch-example-tutorial-in-android-studio/
@SuppressWarnings("WeakerAccess")
public class StopwatchService extends IntentService {

    public static final String START_TIME_EXTRA = "startTime";
    public static final String END_TIME_EXTRA = "endTime";

    public static Intent getIntent(Context context, long startTime, long endTime) {
        Intent intent = new Intent(context, StopwatchService.class);
        intent.putExtra(START_TIME_EXTRA, startTime);
        intent.putExtra(END_TIME_EXTRA, endTime);

        return intent;
    }

    private final IBinder mBinder = new StopWatchBinder();

    private long startTime, millisTime, timeBuffer, updateTime = 0L;
    private long endTime;

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            millisTime = SystemClock.uptimeMillis() - startTime;
            updateTime = timeBuffer + millisTime;

            String displayText = getTimeString(updateTime);

            if (listener != null) {
                listener.onStopwatchUpdate(displayText);
            }

            if (updateTime > endTime) {
                stop();
            }

            handler.postDelayed(this, 100L);
        }
    };

    /**
     * Get a time as a formatted String
     * @param time time in milliseconds
     * @return time formatted to hours, minutes, and seconds
     */
    public static String getTimeString(long time) {
        int hours, minutes, seconds;
        seconds = (int) (time / 1000);
        minutes = seconds / 60;
        hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
    }

    public StopwatchService() {
        super(StopwatchService.class.getSimpleName());
    }

    /**
     * Service starts here
     * @param intent Intent from getIntent()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        long offset = intent.getLongExtra(START_TIME_EXTRA, 0L);
        endTime = intent.getLongExtra(END_TIME_EXTRA, 1000L);

        start(offset);
    }


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

    public long getTime() {
        return updateTime;
    }

    /**
     * Start the stopwatch at a certain point (0 for a new timer)
     * @param offset time past 0 to set the stopwatch at
     */
    public void start(long offset) {
        startTime = SystemClock.uptimeMillis() - offset;
        handler.postDelayed(runnable, 0);
    }

    public void pause() {
        timeBuffer += millisTime;
        handler.removeCallbacks(runnable);
    }

    /**
     * Stop the stopwatch
     */
    public void stop() {
        handler.removeCallbacks(runnable);
        stopSelf();
    }

    /**
     * Class to bind the stopwatch service to the RunTracker
     */
    public class StopWatchBinder extends Binder {
        StopwatchService getService() {
            return StopwatchService.this;
        }
    }

    private StopWatchListener listener;

    public void setStopWatchListener(StopWatchListener listener) {
        this.listener = listener;
    }

    public interface StopWatchListener {
        void onStopwatchUpdate(String time);
    }
}
