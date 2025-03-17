package com.oreo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.noisefit.luna.R

class WidgetProviderOne : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidgetOne(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateWidgetOne(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_1)

            //val batteryLevel = watchDataStore.getBatteryPercentRing()

            views.setTextViewText(R.id.tvBatteryPercentage,"100")

            val intent = Intent(context, WidgetProviderOne::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            val pendingIntent = PendingIntent.getBroadcast(
                context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.refreshData, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
