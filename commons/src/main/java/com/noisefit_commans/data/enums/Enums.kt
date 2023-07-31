package com.noisefit_commans.data.enums

import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats

enum class SleepExtraType {
    HeartRate,
    StressLevel,
    BloodOxygen,
    RespiratoryQuality,
    NONE
}

enum class GridType {
    TYPE_1_X_1, TYPE_1_X_2, TYPE_1_X_3, TYPE_2_X_1, TYPE_2_X_2, TYPE_2_X_3, TYPE_3_X_1, TYPE_3_X_2, ACTIVITY, BATTERY, DATE, TIME, WEATHER, POINTER, HEART_RATE, TIME_POSITION, ABOVE_TIME, BELOW_TIME
}

enum class ServiceState {
    STARTED,
    STOPPED,
}

enum class DaFitCustomListItem {
    TimePosition, AboveTime, BelowTime
}

enum class HealthOverViewHistoryType {
    Steps,
    Calories,
    Distance
}

enum class CustomWatchFaceViewState {
    WALLPAPER, GRID
}

enum class Actions {
    START,
    INIT_DEFAULT,
    STOP
}

enum class WidgetDimensions {
    _50x66,
    _86x36,
    _89x91,
    _145x56,
    _168x32,
    _100x32,
    _236x32,
    _151x32,
    _14x29,
    _29x14,
    _59x33,
    _33x59,
    _148x35,
    _248x45,
    _203x45,
    _173x43
}

enum class Device {
    SMARTWATCH, RING;

    companion object {
        fun getValueFromStrings(value: String?): Device? {
            return try {
                if (value.isNullOrEmpty()) return null

                valueOf(value.uppercase(DateFormats.defaultLocale))
            } catch (ex: Exception) {
                null
            }
        }
    }
}

enum class GraphType{
    HR,HRV,SLEEP,ACTIVITY
}