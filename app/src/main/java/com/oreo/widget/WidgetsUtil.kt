package com.oreo.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

class WidgetsUtil {

    fun updateAllWidgets(context: Context) {
        val intent = Intent(context, WidgetProviderOne::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        context.sendBroadcast(intent)

        val intent2 = Intent(context, WidgetProviderTwo::class.java)
        intent2.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        context.sendBroadcast(intent2)
    }
}