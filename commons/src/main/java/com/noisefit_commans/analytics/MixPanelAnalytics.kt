package com.noisefit_commans.analytics

import android.content.Context
import org.json.JSONObject
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.noisefit_commans.BuildConfig

object MixPanelAnalytics {
    @Volatile
    private var mixpanel: MixpanelAPI? = null
    private var isInitialized = false

    fun initialize(context: Context, token: String) {
        if(BuildConfig.DEBUG) return
        if (isInitialized) return
        synchronized(this) {
            if (!isInitialized) {
                mixpanel = MixpanelAPI.getInstance(
                    context.applicationContext,
                    token,
                    true
                )
                mixpanel?.setEnableLogging(true)
                mixpanel?.setServerURL("https://api-eu.mixpanel.com")
                isInitialized = true
            }
        }
    }

    fun trackEvent(
        eventName: String,
        properties: Map<String, Any>? = null
    ) {
        if (!isInitialized) {
            return
        }
        val json = properties.toJson()
        mixpanel?.track(eventName, json)
        mixpanel?.flush()
    }

    fun identifyUser(
        userId: String,
        userProperties: Map<String, Any>? = null
    ) {
        if (!isInitialized) return
        mixpanel?.identify(userId, true)
        userProperties?.forEach { (key, value) ->
            mixpanel?.people?.set(key, value)
        }
    }

    private fun Map<String, Any>?.toJson(): JSONObject? {
        if (this.isNullOrEmpty()) return null
        val json = JSONObject()
        this.forEach { (k, v) ->
            json.put(k, v)
        }
        return json
    }
}
