package com.oreo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.noisefit.luna.R

class WidgetProviderTwo : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidgetTwo(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateWidgetTwo(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val sharedPreferences = context.getSharedPreferences("WidgetData", Context.MODE_PRIVATE)
            val text = sharedPreferences.getString("widget_data", "Default Text")

            val views = RemoteViews(context.packageName, R.layout.widget_layout_2)
            views.setTextViewText(R.id.widget_two_text, text)

            val intent = Intent(context, WidgetProviderTwo::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            val pendingIntent = PendingIntent.getBroadcast(
                context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_two_button, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
