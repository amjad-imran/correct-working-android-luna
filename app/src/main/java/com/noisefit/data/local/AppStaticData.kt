package com.noisefit.data.local

import android.graphics.Color
import com.google.gson.Gson
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit_commans.data.model.CustomDaFitIcons
import com.noisefit_commans.data.model.DistanceKmMiMapper
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit_commans.data.model.WatchFaceWidgets
import com.noisefit_commans.data.enums.DaFitCustomListItem
import com.noisefit_commans.data.enums.GridType
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.TimeFormats
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.LOGS

object AppStaticData {

    const val DEFAULT_STEPS_PER_HOUR = 50

    const val NOTIFICATION_TITLE = "NoiseFit is running"
    const val FINDING_YOUR_PHONE = "Finding your phone..."

    fun getDefaultDistanceMapperValue(): List<DistanceKmMiMapper> {
        return arrayListOf(
            DistanceKmMiMapper(1, 1, 1000, 1609),
            DistanceKmMiMapper(2, 2, 2000, 3218),
            DistanceKmMiMapper(3, 3, 3000, 4827),
            DistanceKmMiMapper(4, 4, 4000, 6436),
            DistanceKmMiMapper(5, 5, 5000, 8045),
            DistanceKmMiMapper(6, 6, 6000, 9654),
            DistanceKmMiMapper(7, 7, 7000, 11263),
            DistanceKmMiMapper(8, 8, 8000, 12872),
            DistanceKmMiMapper(9, 9, 9000, 14481),
            DistanceKmMiMapper(10, 10, 10000, 16090),
            DistanceKmMiMapper(11, 11, 11000, 17699),
            DistanceKmMiMapper(12, 12, 12000, 19308),
            DistanceKmMiMapper(13, 13, 13000, 20917),
            DistanceKmMiMapper(14, 14, 14000, 22526),
            DistanceKmMiMapper(15, 15, 15000, 24135),
            DistanceKmMiMapper(16, 16, 16000, 25744),
            DistanceKmMiMapper(17, 17, 17000, 27353),
            DistanceKmMiMapper(18, 18, 18000, 28962),
            DistanceKmMiMapper(19, 19, 19000, 30571),
            DistanceKmMiMapper(20, 20, 20000, 32180),
            DistanceKmMiMapper(21, 21, 21000, 33789),
            DistanceKmMiMapper(22, 22, 22000, 35398),
            DistanceKmMiMapper(23, 23, 23000, 37007),
            DistanceKmMiMapper(24, 24, 24000, 38616),
            DistanceKmMiMapper(25, 25, 25000, 40225)
        )
    }

    fun getDefaultHeightValue(unit: Units?): Int {
        return when (unit) {
            Units.METRIC -> {
                159
            }
            Units.IMPERIAL -> {
                71
            }
            else -> {
                0
            }
        }

    }

    fun getDefaultWeightValue(unit: Units?): Int {
        return when (unit) {
            Units.METRIC -> {
                59
            }
            Units.IMPERIAL -> {
                129
            }
            else -> {
                0
            }
        }

    }

    fun getSleepHoursValues(): Array<String> {
        return Array(7) { "${it + 6} Hours" }
    }

    fun getReminderRepeatValues(): Array<String> {
        return arrayOf("Never", "Every Day", "Every Week", "Every Month", "Every Year")
    }

    fun getUnitsValues(): Array<String> {
        return arrayOf("metric", "imperial")
    }

    fun getGenderValues(): Array<String> {
        return arrayOf("Man", "Woman", "Non-binary", "Prefer not to say")
    }
    fun getIntensityValues(): Array<String> {
        return arrayOf("Easy", "Moderate", "Hard")
    }

    fun getDurationValues(): Array<String> {
        return arrayOf("Today", "Yesterday", "This week", "Previous Week", "Monthly")
    }

    fun getMenstrualLengthValues(): Array<String> {
        return Array(7) { "${it + 2} Days" }
    }

    fun getPeriodLengthValues(): Array<String> {
        return Array(11) { "${it + 25} Days" }
    }

    fun getMsReminderValues(): Array<String> {
        return Array(3) { "${it + 1} Days" }
    }

    fun getAutoHrFrequency(isEvolve2: Boolean, isUltra2LE: Boolean): Array<String> {
        if (isEvolve2) {
            return Array(12) { "${100 + (it * 10)} BPM" }
        }
        if (isUltra2LE) {
            return Array(11) { "${100 + (it * 5)} BPM" }
        }
        return Array(12) { "${140 + (it * 10)} BPM" }
    }

    fun getScreenInterval(): Array<String> {
        return Array(3) { "${((it + 1) * 5)} sec" }
    }

    fun getVibrationIntensity(): Array<String> {
        return arrayOf("Weak", "Medium", "Strong")
    }

    fun getWalkStepsPerHour(): Array<String> {
        return Array(12) { "${(it + 1) * 50}" }
    }

    fun getTemperatureValues(): Array<String> {
        return arrayOf("Celsius", "Fahrenheit")
    }

    fun getReminderFrequencyValues(): Array<String> {
        return Array(4) { "${it + 1} hr" }
    }

    fun getMedicineReminderFrequencyValues(): Array<String> {
        return arrayOf("4 hr", "6 hr", "8 hr", "12 hr")
    }

    fun getIdleAlertFrequencyValues(): Array<String> {
        return Array(8) { "${15 * (it + 1)} min" }
    }

    fun getHandWashFrequencyValues(): Array<String> {
        return Array(24) { "${5 * (it + 1)} min" }
    }

    fun getHandWashFrequencyValuesEvolve(): Array<String> {
        return Array(8) { "${15 * (it + 1)} min" }
    }

    fun getHandWashDurationValues(): Array<String> {
        return Array(12) { "${5 * (it + 1)} sec" }
    }

    fun getAutoHrTimeFrequency(): Array<String> {
        return Array(12) { "${(it + 1) * 5} Mins" }
    }

    fun getEvolveAutoHrTimeFrequency(): Array<String> {
        return arrayOf("5 Mins", "10 Mins", "20 Mins", "30 Mins")
    }

    fun getTimeFrequency(): Array<String> {
        return arrayOf("5 Mins", "10 Mins", "15 Mins", "20 Mins", "25 Mins", "30 Mins")
    }

    fun getLowHrValues(): Array<String> {
        return Array(7) { "${40 + (it * 10)} BPM" }
    }

    fun getTimeFormatValues(): Array<String> {
        return Array(2) { "$it" }.apply {
            this[0] = TimeFormats.HOURS_12.type
            this[1] = TimeFormats.HOURS_24.type
        }
    }


    fun getWatchFaceColorList(): ArrayList<Int> {
        return arrayListOf(
            Color.argb(255, 0, 0, 0),
            Color.argb(255, 255, 255, 255),
            Color.argb(255, 255, 73, 73),

            Color.argb(255, 255, 109, 0),
            Color.argb(255, 251, 230, 84),
            Color.argb(255, 137, 255, 0),

            Color.argb(255, 0, 255, 112),
            Color.argb(255, 77, 231, 111),
            Color.argb(255, 113, 229, 254),

            Color.argb(255, 109, 97, 255),
            Color.argb(255, 236, 81, 220),
            Color.argb(255, 0, 163, 255),
            Color.argb(255, 255, 138, 0),
            Color.argb(255, 122, 151, 255)
        )
    }


    fun getCustomWatchFaceNav(connectedDevice: ColorFitDevice?): List<CustomWatchFace> {
        return arrayListOf()

    }


    fun getDefaultCustomReply(): java.util.ArrayList<CustomReplyData.CustomReply> {
        return arrayListOf(
            CustomReplyData.CustomReply(
                content = "Let me get back to you.",
                index = 0,
            ), CustomReplyData.CustomReply(
                content = "Can I call you later?",
                index = 1,
            ), CustomReplyData.CustomReply(
                content = "Sorry, can not talk right now.",
                index = 2,
            ), CustomReplyData.CustomReply(
                content = "In a meeting right now.",
                index = 3,
            ), CustomReplyData.CustomReply(
                content = "No, Problem.",
                index = 4,
            )
        )
    }

    fun getMaxQuickRepliesCount(connectedDevice: ColorFitDevice?): Int {
        if (connectedDevice == null) return 5

        return when (connectedDevice.deviceType) {
            DeviceType.COLORFIT_VISION.deviceType -> {
                3
            }
            DeviceType.QUBE_2.deviceType,DeviceType.COLORFIT_MACRO.deviceType -> {
                4
            }
            else -> {
                5
            }
        }
    }

    fun getMaxQrCodes(): Int {
        return 8
    }


    fun getMaxAlarmCount(connectedDevice: ColorFitDevice?): Int {
        if (connectedDevice == null) return 5

        return when (connectedDevice.deviceType) {
            DeviceType.NOISE_ULTRA.deviceType,
            DeviceType.COLORFIT_ULTRA_2.deviceType,
            DeviceType.COLORFIT_ULTRA_BUZZ.deviceType,
            DeviceType.COLORFIT_VISION_BUZZ.deviceType -> {
                5
            }
            DeviceType.NOISEFIT_ACTIVE.deviceType,
            DeviceType.COLORFIT_PRO_3.deviceType,
            DeviceType.NOISE_EVOLVE_2.deviceType,
            DeviceType.NOISEFIT_AGILE.deviceType,
            DeviceType.COLORFIT_PULSE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType,
            DeviceType.NOISE_EVOLVE_2_PLAY.deviceType,
            -> {
                10
            }
            DeviceType.NOISE_QUBE_O2.deviceType,
            DeviceType.NOISE_ICON_BUZZ.deviceType,
            DeviceType.NOISE_ICON_PLUS.deviceType,
            DeviceType.ICON_MAX.deviceType,
            DeviceType.ICON_2.deviceType,
            DeviceType.NOISE_THRIVE.deviceType,
            DeviceType.COLORFIT_QUAD_CALL.deviceType,
            DeviceType.COLORFIT_VIVID_CALL.deviceType,
            DeviceType.ICON_3.deviceType,
            DeviceType.NOISE_BOUNCE.deviceType,
            DeviceType.NOISE_SPRINT.deviceType,
            DeviceType.COLORFIT_SPARK.deviceType,
            DeviceType.NOISEFIT_CANVAS.deviceType,
            DeviceType.FORCE.deviceType,
            DeviceType.COLORFIT_ICON_2_VISTA.deviceType,
            DeviceType.NOISE_QUBE.deviceType,
            DeviceType.QUBE_2.deviceType,
            DeviceType.NOISEFIT_TRIUMPH.deviceType,
            DeviceType.COLORFIT_THRILL.deviceType,
            DeviceType.COLORFIT_CALIBER3_PLUS.deviceType,
            DeviceType.COLORFIT_MACRO.deviceType,
            DeviceType.NOISEFIT_VENTURE.deviceType-> {
                3
            }
            else -> {
                5
            }
        }
    }


    fun getNotificationTypeName(appType: String): String {
        return when (appType) {
            "com.whatsapp.w4b" -> ApplicationType.WHATS_APP_BUSINESS.type
            "com.whatsapp" -> ApplicationType.WHATS_APP.type
            "org.telegram.messenger" -> ApplicationType.TELEGRAM.type
            "com.snapchat.android" -> ApplicationType.SNAPCHAT.type
            "com.Slack" -> ApplicationType.SLACK.type
            "com.google.android.youtube" -> ApplicationType.YOUTUBE.type
            "com.microsoft.office.outlook" -> ApplicationType.OUTLOOK.type
            "com.instagram.android" -> ApplicationType.INSTAGRAM.type
            "com.linkedin.android" -> ApplicationType.LINKED_IN.type
            "com.google.android.gm" -> ApplicationType.GMAIL.type
            "com.viber.voip" -> ApplicationType.VIBER.type
            "com.facebook.katana" -> ApplicationType.FACEBOOK.type
            "com.facebook.orca" -> ApplicationType.FB_MESSENGER.type
            "com.skype.raider" -> ApplicationType.SKYPE.type
            "com.twitter.android" -> ApplicationType.TWITTER.type
            "com.tencent.mm" -> ApplicationType.WE_CHAT.type
            "com.google.android.apps.maps" -> ApplicationType.GOOGLE_MAPS.type
            "com.google.android.calendar" -> ApplicationType.CALENDAR.type
            "com.pinterest" -> ApplicationType.PINTEREST.type
            "in.amazon.mShop.android.shopping" -> ApplicationType.AMAZON.type
            "com.flipkart.android" -> ApplicationType.FLIPKART.type
            "com.google.android.apps.magazines" -> ApplicationType.GOOGLE_NEWS.type
            "com.google.android.apps.nbu.paisa.user" -> ApplicationType.GOOGLE_PAY.type
            "com.google.android.talk" -> ApplicationType.HANGOUTS.type
            "com.olacabs.customer" -> ApplicationType.OLA.type
            "net.one97.paytm" -> ApplicationType.PAYTM.type
            "com.phonepe.app" -> ApplicationType.PHONEPE.type
            "com.ubercab" -> ApplicationType.UBER.type
            "naukriApp.appModules.login" -> ApplicationType.NAUKRI.type
            "com.nis.app" -> ApplicationType.INSHORTS.type
            BuildConfig.APPLICATION_ID -> ApplicationType.NOISEFIT.type
            else -> ApplicationType.OTHER.type
        }
    }


    fun getNotificationApps(appString: String?): List<NotificationApp> {
        val response = ArrayList<NotificationApp>()
        if (appString.isNullOrEmpty()) return ArrayList()
        var serverAppString = appString
        if(!serverAppString.contains("noisefit")){
            serverAppString += ";noisefit"
        }
        val appsArray = serverAppString.split(";")
        for (app in appsArray) {
            appsSet[app]?.let {
                response.add(it)
            }
        }

        return response
    }


    private val appsSet: HashMap<String, NotificationApp> by lazy {
        HashMap<String, NotificationApp>().apply {
            put(
                ApplicationType.WHATS_APP.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Whatsapp",
                    appCode = ApplicationType.WHATS_APP,
                    appPackageName = "com.whatsapp"
                )
            )
            put(
                ApplicationType.TELEGRAM.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Telegram",
                    appCode = ApplicationType.TELEGRAM,
                    appPackageName = "org.telegram.messenger"
                )
            )
            put(
                ApplicationType.SNAPCHAT.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Snapchat",
                    appCode = ApplicationType.SNAPCHAT,
                    appPackageName = "com.snapchat.android"
                )
            )
            put(
                ApplicationType.SLACK.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Slack",
                    appCode = ApplicationType.SLACK,
                    appPackageName = "com.Slack"
                )
            )
            put(
                ApplicationType.YOUTUBE.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Youtube",
                    appCode = ApplicationType.YOUTUBE,
                    appPackageName = "com.google.android.youtube"
                )
            )
            put(
                ApplicationType.OUTLOOK.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Outlook",
                    appCode = ApplicationType.OUTLOOK,
                    appPackageName = "com.microsoft.office.outlook"
                )
            )
            put(
                ApplicationType.INSTAGRAM.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Instagram",
                    appCode = ApplicationType.INSTAGRAM,
                    appPackageName = "com.instagram.android"
                )
            )
            put(
                ApplicationType.LINKED_IN.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "LinkedIn",
                    appCode = ApplicationType.LINKED_IN,
                    appPackageName = "com.linkedin.android"
                )
            )
            put(
                ApplicationType.VIBER.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Viber",
                    appCode = ApplicationType.VIBER,
                    appPackageName = "com.viber.voip"
                )
            )
            put(
                ApplicationType.FACEBOOK.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Facebook",
                    appCode = ApplicationType.FACEBOOK,
                    appPackageName = "com.facebook.katana"
                )
            )
            put(
                ApplicationType.SMS.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "SMS",
                    appCode = ApplicationType.SMS,
                    appPackageName = ""
                )
            )
            put(
                ApplicationType.CALL.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Call Alert",
                    appCode = ApplicationType.CALL,
                    appPackageName = ""
                )
            )

            put(
                ApplicationType.FB_MESSENGER.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "FB Messenger",
                    appCode = ApplicationType.FB_MESSENGER,
                    appPackageName = "com.facebook.orca"
                )
            )
            put(
                ApplicationType.GMAIL.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Gmail",
                    appCode = ApplicationType.GMAIL,
                    appPackageName = "com.google.android.gm"
                )
            )
            put(
                ApplicationType.SKYPE.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Skype",
                    appCode = ApplicationType.SKYPE,
                    appPackageName = "com.skype.raider"
                )
            )
            put(
                ApplicationType.TWITTER.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Twitter",
                    appCode = ApplicationType.TWITTER,
                    appPackageName = "com.twitter.android"
                )
            )

            put(
                ApplicationType.WE_CHAT.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Wechat",
                    appCode = ApplicationType.WE_CHAT,
                    appPackageName = "com.tencent.mm"
                )
            )

            put(
                ApplicationType.GOOGLE_MAPS.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Google Maps",
                    appCode = ApplicationType.GOOGLE_MAPS,
                    appPackageName = "com.google.android.apps.maps"
                )
            )
            put(
                ApplicationType.OTHER.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Other",
                    appCode = ApplicationType.OTHER,
                    appPackageName = ""
                )
            )

            put(
                ApplicationType.AMAZON.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Amazon",
                    appCode = ApplicationType.AMAZON,
                    appPackageName = "in.amazon.mShop.android.shopping"
                )
            )
            put(
                ApplicationType.FLIPKART.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Flipkart",
                    appCode = ApplicationType.FLIPKART,
                    appPackageName = "com.flipkart.android"
                )
            )
            put(
                ApplicationType.GOOGLE_NEWS.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Google News",
                    appCode = ApplicationType.GOOGLE_NEWS,
                    appPackageName = "com.google.android.apps.magazines"
                )
            )
            put(
                ApplicationType.GOOGLE_PAY.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Google Pay",
                    appCode = ApplicationType.GOOGLE_PAY,
                    appPackageName = "com.google.android.apps.nbu.paisa.user"
                )
            )
            put(
                ApplicationType.HANGOUTS.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Hangout",
                    appCode = ApplicationType.HANGOUTS,
                    appPackageName = "com.google.android.talk"
                )
            )
            put(
                ApplicationType.OLA.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Ola",
                    appCode = ApplicationType.OLA,
                    appPackageName = "com.olacabs.customer"
                )
            )
            put(
                ApplicationType.PAYTM.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Paytm",
                    appCode = ApplicationType.PAYTM,
                    appPackageName = "net.one97.paytm"
                )
            )
            put(
                ApplicationType.PHONEPE.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Phonepe",
                    appCode = ApplicationType.PHONEPE,
                    appPackageName = "com.phonepe.app"
                )
            )
            put(
                ApplicationType.PINTEREST.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Pinterest",
                    appCode = ApplicationType.PINTEREST,
                    appPackageName = "com.pinterest"
                )
            )
            put(
                ApplicationType.UBER.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Uber",
                    appCode = ApplicationType.UBER,
                    appPackageName = "com.ubercab"
                )
            )
            put(
                ApplicationType.WHATS_APP_BUSINESS.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Whatsapp Business",
                    appCode = ApplicationType.WHATS_APP_BUSINESS,
                    appPackageName = "com.whatsapp.w4b"
                )
            )
            put(
                ApplicationType.NAUKRI.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Naukri",
                    appCode = ApplicationType.NAUKRI,
                    appPackageName = "naukriApp.appModules.login"
                )
            )
            put(
                ApplicationType.INSHORTS.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Inshorts",
                    appCode = ApplicationType.INSHORTS,
                    appPackageName = "com.nis.app"
                )
            )
            put(
                ApplicationType.CALENDAR.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "Calendar",
                    appCode = ApplicationType.CALENDAR,
                    appPackageName = "com.google.android.calendar"
                )
            )
            put(
                ApplicationType.NOISEFIT.type,
                NotificationApp(
                    image = 1,
                    appDisplayName = "NoiseFit",
                    appCode = ApplicationType.NOISEFIT,
                    appPackageName = BuildConfig.APPLICATION_ID
                )
            )
        }
    }

    fun getGridColors(): Array<String> {
        return arrayOf(
            "faebd7",
            "00ffff",
            "0000ff",
            "800080",
            "ff0000",
            "4169e1",
            "2e8b57",
            "a0522d",
            "fffafa",
            "000000"
        )
    }

    fun getWatchFaceWidgets(): List<WatchFaceWidgets> {
        val list = ArrayList<WatchFaceWidgets>()
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_1,
                    GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3,
                    GridType.TYPE_2_X_1,
                    GridType.TYPE_2_X_2,
                    GridType.TYPE_2_X_3,
                    GridType.TYPE_3_X_1,
                    GridType.TYPE_3_X_2
                ),
                image = R.drawable.ic_wf_none,
                widgetName = "None",
                type = -1
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3,
                    GridType.TYPE_2_X_1,
                    GridType.TYPE_2_X_2,
                    GridType.TYPE_2_X_3,
                    GridType.TYPE_3_X_1,
                    GridType.TYPE_3_X_2
                ),
                image = R.drawable.ic_wf_time,
                widgetName = "Time",
                type = 13
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3,
                    GridType.TYPE_2_X_2,
                    GridType.TYPE_2_X_3,
                    GridType.TYPE_3_X_2
                ),
                image = R.drawable.ic_my_reminder,
                widgetName = "Calendar Reminder",
                type = 14
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_1,
                    GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3,
                    GridType.TYPE_2_X_1,
                    GridType.TYPE_2_X_2,
                    GridType.TYPE_2_X_3,
                    GridType.TYPE_3_X_2
                ),
                image = R.drawable.ic_wf_active_min,
                widgetName = "Active Minutes",
                type = 9
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(GridType.TYPE_1_X_1),
                image = R.drawable.ic_my_reminder,
                widgetName = "Active Graph",
                type = 12
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_1, GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3, GridType.TYPE_2_X_1, GridType.TYPE_2_X_2
                ),
                image = R.drawable.ic_wf_battery,
                widgetName = "Battery",
                type = 8
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_1,
                    GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3,
                    GridType.TYPE_2_X_1,
                    GridType.TYPE_2_X_2,
                    GridType.TYPE_2_X_3,
                    GridType.TYPE_3_X_2
                ),
                image = R.drawable.ic_wf_calorie,
                widgetName = "Calories",
                type = 2
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_1,
                    GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3,
                    GridType.TYPE_2_X_1,
                    GridType.TYPE_2_X_2,
                    GridType.TYPE_2_X_3,
                    GridType.TYPE_3_X_2
                ),
                image = R.drawable.ic_wf_date,
                widgetName = "Date",
                type = 4
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_1,
                    GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3,
                    GridType.TYPE_2_X_1,
                    GridType.TYPE_2_X_2,
                    GridType.TYPE_2_X_3,
                    GridType.TYPE_3_X_2
                ),
                image = R.drawable.ic_wf_distance,
                widgetName = "Distance",
                type = 3
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TYPE_1_X_1,
                    GridType.TYPE_1_X_2,
                    GridType.TYPE_1_X_3,
                    GridType.TYPE_2_X_1,
                    GridType.TYPE_2_X_2,
                    GridType.TYPE_2_X_3,
                    GridType.TYPE_3_X_2
                ),
                image = R.drawable.ic_wf_time,
                widgetName = "Heart Rate Monitor",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TIME
                ),
                image = R.drawable.icon_watchface_editor_time1,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TIME
                ),
                image = R.drawable.icon_watchface_editor_time2,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TIME
                ),
                image = R.drawable.icon_watchface_editor_time3,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TIME
                ),
                image = R.drawable.icon_watchface_editor_time4,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BATTERY
                ),
                image = R.drawable.icon_watchface_editor_battery1,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BATTERY
                ),
                image = R.drawable.icon_watchface_editor_battery2,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BATTERY
                ),
                image = R.drawable.icon_watchface_editor_battery3,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BATTERY
                ),
                image = R.drawable.icon_watchface_editor_battery4,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.HEART_RATE
                ),
                image = R.drawable.icon_watchface_editor_heart_rate1,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.HEART_RATE
                ),
                image = R.drawable.icon_watchface_editor_heart_rate2,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.HEART_RATE
                ),
                image = R.drawable.icon_watchface_editor_heart_rate3,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.HEART_RATE
                ),
                image = R.drawable.icon_watchface_editor_heart_rate4,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.WEATHER
                ),
                image = R.drawable.icon_watchface_editor_weather1,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.WEATHER
                ),
                image = R.drawable.icon_watchface_editor_weather2,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.WEATHER
                ),
                image = R.drawable.icon_watchface_editor_weather3,
                widgetName = "",
                type = 0
            )
        )
        list.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.WEATHER
                ),
                image = R.drawable.icon_watchface_editor_weather,
                widgetName = "",
                type = 0
            )
        )
        return list
    }


    fun getDatFitCustomItemsList(): ArrayList<WatchFaceWidgets> {
        val item = ArrayList<WatchFaceWidgets>()
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TIME_POSITION
                ),
                image = R.drawable.ic_time_buzz,
                widgetName = "Above",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.TIME_POSITION
                ),
                image = R.drawable.ic_time_buzz,
                widgetName = "Below",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.ABOVE_TIME
                ),
                image = R.drawable.ic_time_buzz,
                widgetName = "Close",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.ABOVE_TIME
                ),
                image = R.drawable.ic_date_buzz,
                widgetName = "Date",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.ABOVE_TIME
                ),
                image = R.drawable.ic_sleep_buzz,
                widgetName = "Sleep",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.ABOVE_TIME
                ),
                image = R.drawable.ic_hr_buzz,
                widgetName = "Heart rate",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.ABOVE_TIME
                ),
                image = R.drawable.ic_steps_buzz,
                widgetName = "Steps",
                type = -1
            )
        )

        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BELOW_TIME
                ),
                image = R.drawable.ic_wf_battery,
                widgetName = "Close",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BELOW_TIME
                ),
                image = R.drawable.ic_date_buzz,
                widgetName = "Date",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BELOW_TIME
                ),
                image = R.drawable.ic_sleep_buzz,
                widgetName = "Sleep",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BELOW_TIME
                ),
                image = R.drawable.ic_hr_buzz,
                widgetName = "Heart rate",
                type = -1
            )
        )
        item.add(
            WatchFaceWidgets(
                supportedGrid = arrayListOf(
                    GridType.BELOW_TIME
                ),
                image = R.drawable.ic_steps_buzz,
                widgetName = "Steps",
                type = -1
            )
        )
        return item
    }

    fun getDaFitCustomItems(): ArrayList<CustomDaFitIcons> {
        val items = ArrayList<CustomDaFitIcons>()
        items.add(
            CustomDaFitIcons(
                DaFitCustomListItem.TimePosition,
                "Time Position",
                "Above"
            )
        )
        items.add(
            CustomDaFitIcons(
                DaFitCustomListItem.AboveTime,
                "Above Time",
                "Close"
            )
        )
        items.add(
            CustomDaFitIcons(
                DaFitCustomListItem.BelowTime,
                "Below Time",
                "Date"
            )
        )
        return items
    }
}

