package com.popularpenguin.runapp.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.popularpenguin.runapp.R;

public class RunWidget extends AppWidgetProvider {

    private static final String WIDGET_TIME_STRING_EXTRA = "widgetTimeStringExtra";
    private static final String WIDGET_TIME_EXTRA = "widgetTimeExtra";
    private static final String WIDGET_GOAL_EXTRA = "widgetGoalExtra";

    private static String sTimerText; // the formatted time
    private static long sTime; // the current time
    private static long sChallengeGoal; // the time to stop at

    public static Intent getIntent(String timeString, long time, long challengeGoalTime) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(WIDGET_TIME_STRING_EXTRA, timeString);
        intent.putExtra(WIDGET_TIME_EXTRA, time);
        intent.putExtra(WIDGET_GOAL_EXTRA, challengeGoalTime);

        return intent;
    }

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.run_widget);

        // set the widget timer's text
        if (sTimerText == null) {
            String defaultTimerText = context.getString(R.string.widget_default_text);
            views.setTextViewText(R.id.tv_widget_timer, defaultTimerText);
        }
        else {
            views.setTextViewText(R.id.tv_widget_timer, sTimerText);
            if (sTime >= sChallengeGoal) {
                int colorRed = context.getResources().getColor(R.color.red);
                views.setTextColor(R.id.tv_widget_timer, colorRed);
            }
            else if (sTime >= sChallengeGoal * 0.66) {
                int colorYellow = context.getResources().getColor(R.color.yellow);
                views.setTextColor(R.id.tv_widget_timer, colorYellow);
            }
        }

        // update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update all widgets since it's possible there is more than one active
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            sTimerText = intent.getStringExtra(WIDGET_TIME_STRING_EXTRA);
            sTime = intent.getLongExtra(WIDGET_TIME_EXTRA, 0L);
            sChallengeGoal = intent.getLongExtra(WIDGET_GOAL_EXTRA, 1000L);

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, RunWidget.class);
            int[] appWidgetIds = manager.getAppWidgetIds(provider);

            onUpdate(context, manager, appWidgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
