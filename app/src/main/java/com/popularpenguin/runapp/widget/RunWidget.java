package com.popularpenguin.runapp.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.map.StopwatchService;

import java.util.Locale;

/**
 * The widget that displays the last session run after it is completed
 */
public class RunWidget extends AppWidgetProvider {

    private static final String WIDGET_GOAL_REACHED = "widgetGoalReached";
    private static final String WIDGET_DISTANCE_EXTRA = "widgetDistanceExtra";
    private static final String WIDGET_CURRENT_TIME_EXTRA = "widgetCurrentTimeExtra";
    private static final String WIDGET_FASTEST_TIME_EXTRA = "widgetFastestTimeExtra";

    private static boolean sGoalReached; // has the distance goal been met?
    private static long sCurrentDistance; // challenge distance total
    private static long sCurrentTime; // the session's time
    private static long sFastestTime; // the challenge's fastest time

    public static Intent getIntent(boolean goalReached, long time, Challenge challenge) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(WIDGET_GOAL_REACHED, goalReached);
        intent.putExtra(WIDGET_DISTANCE_EXTRA, challenge.getDistance());
        intent.putExtra(WIDGET_CURRENT_TIME_EXTRA, time);
        intent.putExtra(WIDGET_FASTEST_TIME_EXTRA, challenge.getFastestTime());

        sGoalReached = goalReached;
        sCurrentDistance = challenge.getDistance();
        sCurrentTime = time;
        sFastestTime = challenge.getFastestTime();

        return intent;
    }

    /**
     * Helper method to update the widget when requested
     * @param context the context
     * @param appWidgetManager the widget manager
     * @param appWidgetId unique id for a widget (more than one can be displayed at a time)
     */
    private static void updateAppWidget(Context context,
                                        AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.run_widget);

        int colorGreen = context.getResources().getColor(R.color.green);
        int colorRed = context.getResources().getColor(R.color.red);

        // set the widget's current distance run
        if (sCurrentDistance == 0) {
            String defaultText = context.getString(R.string.widget_default_distance);

            views.setTextViewText(R.id.tv_widget_distance, defaultText);
        }
        else if (!sGoalReached) {
            String challengeFailed = "Try again!";

            views.setTextViewText(R.id.tv_widget_distance, challengeFailed);
            views.setTextColor(R.id.tv_widget_distance, colorRed);
        }
        else {
            String distanceUnits = context.getResources().getString(R.string.run_units);

            views.setTextViewText(R.id.tv_widget_distance,
                    String.format(Locale.US,
                            "%.2f %s",
                            sCurrentDistance / 5280f,
                            distanceUnits));
            views.setTextColor(R.id.tv_widget_distance, colorGreen);
        }

        // set the current session's time
        String currentTimeString = StopwatchService.getTimeString(sCurrentTime);
        views.setTextViewText(R.id.tv_widget_current_time, currentTimeString);

        if (sGoalReached) {
            views.setTextColor(R.id.tv_widget_current_time, colorGreen);
        }
        else {
            views.setTextColor(R.id.tv_widget_current_time, colorRed);
        }

        // set the fastest time's text
        if (sFastestTime == 0) {
            views.setTextViewText(R.id.tv_widget_fastest_time, "-:--:--");
            views.setTextColor(R.id.tv_widget_fastest_time, colorRed);
        }
        else {
            String fastestTimeString = StopwatchService.getTimeString(sFastestTime);
            views.setTextViewText(R.id.tv_widget_fastest_time, fastestTimeString);
            views.setTextColor(R.id.tv_widget_fastest_time, colorGreen);
        }

        // update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Calls the helper method on each available app widget instance
     * @param context the context
     * @param appWidgetManager the widget manager
     * @param appWidgetIds ids of the widgets
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update all widgets since it's possible there is more than one active
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Process the intent
     * @param context the context
     * @param intent the passed intent from the app
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            sCurrentDistance = intent.getLongExtra(WIDGET_DISTANCE_EXTRA, 0L);
            sCurrentTime = intent.getLongExtra(WIDGET_CURRENT_TIME_EXTRA, 0L);
            sFastestTime = intent.getLongExtra(WIDGET_FASTEST_TIME_EXTRA, 0L);

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, RunWidget.class);
            int[] appWidgetIds = manager.getAppWidgetIds(provider);

            onUpdate(context, manager, appWidgetIds);
        }
    }

}
