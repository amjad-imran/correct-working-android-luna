package com.noisefit_commans.data.enums

enum class DashInfoCard {
    WELCOME, CARE, SLEEP, ACTIVITY, READINESS
}

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


enum class GraphType {
    HR, HRV, SLEEP, ACTIVITY
}

enum class StressType {
    CALM, STRESS, FOCUSED
}