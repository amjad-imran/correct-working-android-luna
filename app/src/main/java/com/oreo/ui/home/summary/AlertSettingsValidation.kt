package com.oreo.ui.home.summary

import com.noisefit_commans.models.HeartRateAlertSettings

object AlertSettingsValidation {

    fun isHeartRateThresholdValid(value: Int): Boolean {
        return value in 1..250
    }

    fun isSpo2ThresholdValid(value: Int): Boolean {
        return value in 1..100
    }

    fun isStressThresholdValid(value: Int): Boolean {
        return value in 1..100
    }

    fun isSedentaryIntervalValid(value: Int): Boolean {
        return value > 0 && value % 60 == 0
    }

    fun isHeartRateSettingsValid(settings: HeartRateAlertSettings): Boolean {
        return (!settings.restingEnabled || isHeartRateThresholdValid(settings.restingThreshold)) &&
            (!settings.workoutEnabled || isHeartRateThresholdValid(settings.workoutThreshold)) &&
            (!settings.lowEnabled || isHeartRateThresholdValid(settings.lowThreshold))
    }
}
