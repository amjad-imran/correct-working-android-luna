package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName

data class DeviceFeatures(

    @SerializedName("sleep_data")
    val sleepData: Int = 0,
    @SerializedName("activity_recognise")
    val activityRecognise: Int = 0,
    @SerializedName("menstrual_data")
    var menstrualData: Int = 0,
    @SerializedName("language")
    val language: Int = 0, //use
    @SerializedName("blood_pressure")
    val bloodPressure: Int = 0,
    @SerializedName("google_fit")
    val googleFit: Int = 0,
    @SerializedName("steps_goal")
    val stepsGoal: Int = 0,
    @SerializedName("available_activities_for_challenges")
    val availableActivitiesForChallenges: String = "",
    @SerializedName("find_phone")
    val findPhone: Int = 0,
    @SerializedName("brightness_level")
    val brightnessLevel: Int = 0,
    @SerializedName("distance_goal")
    val distanceGoal: Int = 0,
    @SerializedName("wrist_lift_gesture")
    val wristLiftGesture: Int = 0,
    @SerializedName("quick_replies")
    val quickReplies: Int = 0,
    @SerializedName("agps_upload")
    val agpsUpload: Int = 0,
    @SerializedName("weather")
    val weather: Int = 0,
    @SerializedName("custom_watch_face")
    val customWatchFace: Int = 0,
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("available_activities")
    val availableActivities: String = "",
    @SerializedName("device_time")
    val deviceTime: Int = 0,

    @SerializedName("call_alert")
    val callAlert: Int = 0,
    @SerializedName("time_format")
    val timeFormat: Int = 0,
    @SerializedName("device_id")
    val deviceId: Int = 0,
    @SerializedName("available_notification_types")
    val availableNotificationTypes: String = "",
    @SerializedName("blood_oxygen")
    val bloodOxygen: Int = 0,
    @SerializedName("heart_rate")
    val heartRate: Int = 0,
    @SerializedName("screen_time")
    val screenTime: Int = 0,
    @SerializedName("active_time")
    val activeTime: Int = 0,
    @SerializedName("current_watch_face")
    val currentWatchFace: Int = 0,
    @SerializedName("heart_rate_history")
    val heartRateHistory: Int = 0,
    @SerializedName("sync_data")
    val syncData: Int = 0,
    @SerializedName("stress_count")
    val stressCount: Int = 0,
    @SerializedName("auto_stress")
    val autoStress: Int = 0,
    @SerializedName("device_restart")
    val deviceRestart: Int = 0,
    @SerializedName("watch_face")
    val watchFace: Int = 0,
    @SerializedName("factory_reset")
    val factoryReset: Int = 0,
    @SerializedName("walk_reminder")
    val walkReminder: Int = 0,
    @SerializedName("auto_heart_measure")
    val autoHeartMeasure: Int = 0,
    @SerializedName("hand_wash")
    val handWash: Int = 0,
    @SerializedName("sedentary")
    val sedentary: Int = 0,
    @SerializedName("platform")
    val platform: String = "",
    @SerializedName("stocks")
    val stocks: Int = 0,
    @SerializedName("camera_shutter")
    val cameraShutter: Int = 0,
    @SerializedName("music_control")
    val musicControl: Int = 0,
    @SerializedName("firmware")
    val firmware: Int = 0,
    @SerializedName("auto_sleep")
    val autoSleep: Int = 0,
    @SerializedName("drink_water")
    val drinkWater: Int = 0,
    @SerializedName("meal_reminder")
    val mealReminder: Int = 0,
    @SerializedName("medicine_reminder")
    val medicineReminder: Int = 0,
    @SerializedName("reminder")
    val reminder: Int = 0,
    @SerializedName("week_start_day")
    val weekStartDay: Int = 0,
    @SerializedName("alarms")
    val alarms: Int = 0,
    @SerializedName("quick_eye_movement")
    val quickEyeMovement: Int = 0,
    @SerializedName("sleep_goal")
    val sleepGoal: Int = 0,
    @SerializedName("find_device")
    val findDevice: Int = 0,
    @SerializedName("do_not_disturb")
    val doNotDisturb: Int = 0,
    @SerializedName("unit_system")
    val unitSystem: Int = 0,
    @SerializedName("sports_selection")
    val sportsSelection: Int = 0,
    @SerializedName("world_clock")
    val worldClock: Int = 0,
    @SerializedName("steps_data")
    val stepsData: Int = 0,
    @SerializedName("date_format")
    val dateFormat: Int = 0,
    @SerializedName("battery_percentage")
    val batteryPercentage: Int = 0,
    @SerializedName("calorie_goal")
    val calorieGoal: Int = 0,
    @SerializedName("spo2")
    val spo2: Int = 0,
    @SerializedName("auto_spo2")
    val autoSpo2: Int = 0,
    @SerializedName("password")
    val password: Int = 0,
    @SerializedName("body_temperature")
    val bodyTemperature: Int = 0,
    @SerializedName("body_temp_unit")
    val bodyTemperatureUnit: Int = 0,
    @SerializedName("contact")
    val contacts: Int = 0,
    @SerializedName("app_sort")
    val appSort: Int = 0,
    @SerializedName("sports_event")
    val sports_event: Int = 0,
    @SerializedName("user_manual1")
    val user_manual: Int = 0,
    @SerializedName("app_notification")
    val app_notification: Int = 1,
    @SerializedName("blecallingswitch")
    val ble_calling_alert: Int = 0,
    @SerializedName("calorie_data")
    var calorieData: Int = 0,
    @SerializedName("widget_sort")
    var widgetSort: Int = 0,
    @SerializedName("qr_payment")
    var qrPayment: Int = 0,
    @SerializedName("vibration_intensity")
    var vibrationIntensity: Int = 0,
    @SerializedName("width")
    var deviceWidth: Int? = 0,
    @SerializedName("height")
    var deviceHeight: Int? = 0,
    @SerializedName("bleName")
    var bleName: String = "",
    @SerializedName("screenType")
    var screenType: String = "",
    @SerializedName("category_wf")
    var hasWfCategory: Int = 0,
    @SerializedName("sos")
    var sos: Int = 0,
    @SerializedName("cw_gif")
    var cwGif: String = "",
    @SerializedName("sleep_reminder")
    var sleepReminder: Int = 0,


    @SerializedName("share_logs")
    var shareLogs: Int = 0,
)
