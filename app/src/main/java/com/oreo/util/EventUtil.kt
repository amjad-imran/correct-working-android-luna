package com.oreo.util

import com.oreo.ui.sleep2.internal.SleepInternalLaunchState

object EventUtil {
    fun getEventName(state: SleepInternalLaunchState): String {
        return when (state) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> "restorative_sleep"
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> "sleep_performance"
            SleepInternalLaunchState.HOUR_VS_NEED -> "hour_vs_need"
            SleepInternalLaunchState.SLEEP_TIME -> "sleep_time"
            SleepInternalLaunchState.TIMING -> "cicadian_mid_point"
            SleepInternalLaunchState.EFFICIENCY -> "efficiency"
            SleepInternalLaunchState.REM_SLEEP -> "rem_sleep"
            SleepInternalLaunchState.DEEP_SLEEP -> "deep_sleep"
            SleepInternalLaunchState.SLEEP_DURATION -> "sleep_duration"
            SleepInternalLaunchState.LATENCY -> "latency"
            SleepInternalLaunchState.RESTFULNESS -> "restfulness"
            SleepInternalLaunchState.RESPIRATORY_RATE -> "respiratory_rate"
            SleepInternalLaunchState.RESTING_HEART_RATE -> "resting_heart_rate"
            SleepInternalLaunchState.HRV -> "hrv"
            SleepInternalLaunchState.SKIN_TEMPERATURE -> "skin_temperature"//initial
            SleepInternalLaunchState.BLOOD_OXYGEN -> "blood_oxygen"
        }
    }
}