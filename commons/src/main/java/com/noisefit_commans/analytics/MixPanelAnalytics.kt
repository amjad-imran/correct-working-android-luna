package com.noisefit_commans.analytics

import android.content.Context
import android.util.Log
import org.json.JSONObject
import com.mixpanel.android.mpmetrics.MixpanelAPI

object MixPanelAnalytics {
    private const val TAG = "MixPanelAnalytics"
    @Volatile
    private var mixpanel: MixpanelAPI? = null
    private var isInitialized = false

    fun initialize(context: Context, token: String) {
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
            log("Mixpanel not initialized")
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
        mixpanel?.identify(userId)
        userProperties?.forEach { (key, value) ->
            mixpanel?.people?.set(key, value)
        }
    }

    fun registerSuperProperties(properties: Map<String, Any>) {
        if (!isInitialized) return
        mixpanel?.registerSuperProperties(properties.toJson())
    }

    fun flush() {
        mixpanel?.flush()
    }

    fun reset() {
        mixpanel?.reset()
    }

    fun optOutTracking() {
        mixpanel?.optOutTracking()
    }

    fun optInTracking() {
        mixpanel?.optInTracking()
    }

    fun destroy() {
        mixpanel = null
        isInitialized = false
    }

    private fun Map<String, Any>?.toJson(): JSONObject? {
        if (this.isNullOrEmpty()) return null
        val json = JSONObject()
        this.forEach { (k, v) ->
            json.put(k, v)
        }
        return json
    }

    private fun log(msg: String) {
        Log.d(TAG, msg)
    }
}
