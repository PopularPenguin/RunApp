package com.popularpenguin.runapp.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.popularpenguin.runapp.R;

// https://medium.com/exploring-android/exploring-android-o-notification-channels-94cd274f604c
public class RunNotification {

    public static final String CHANNEL_ID = "run_channel";
    public static final String CHANNEL_NAME = "Run Challenge Tracker";

    private Context mContext;
    private NotificationManager mNotificationManager;
    private int mNotificationId = 1;

    public RunNotification(Context context) {
        mContext = context;
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

    }

    /** Create a notification to show that the location service is running */
    public void createNotification() {
        Resources resources = mContext.getResources();
        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(mContext, CHANNEL_ID);
        }
        else {
            builder = new Notification.Builder(mContext);
        }

        Notification notification = builder
                .setContentTitle(resources.getString(R.string.notification_title))
                .setContentText(resources.getString(R.string.notification_body))
                .setSmallIcon(R.mipmap.ic_notification_round)
                .build();

        mNotificationManager.notify(mNotificationId, notification);
    }

    /** Cancel the notification created in createNotification() */
    public void cancelNotification() {
        mNotificationManager.cancel(mNotificationId);
    }

    /** Create a notification channel for android versions 26+ */
    @TargetApi(26)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setSound(null, null);

        mNotificationManager.createNotificationChannel(notificationChannel);
    }
}
