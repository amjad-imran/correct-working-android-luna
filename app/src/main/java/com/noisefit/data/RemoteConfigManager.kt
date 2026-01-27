package com.noisefit.data

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig

object RemoteConfigManager {

    const val WHATS_NEW_HOME = "whats_new_home"
    private val remoteConfig by lazy {
        Firebase.remoteConfig
    }

    fun init(isDebug: Boolean) {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds =
                if (isDebug) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun fetchAndActivate(
        onComplete: (Boolean) -> Unit = {}
    ) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun getBoolean(key: String): Boolean =
        remoteConfig.getBoolean(key)

    fun getString(key: String): String =
        remoteConfig.getString(key)
}