package com.noisefit_commans.utils

import com.noisefit_commans.models.SportsModeList

object AppConstants {


    const val OTP_RESEND_TIMER = 60000L
    const val QUIZ_RELOAD_TIMER = 15000L
    const val CONTENT_SYNC_DATA_INTERVAL = 45000L

    const val URL_FAQ = "https://www.gonoise.com/pages/faq"
    const val URL_HOW_TO_VIDEOS =
        "https://www.youtube.com/playlist?list=PLFfb47orPjGR4o4ScUw3dDwwxio0q4DHu"
    const val URL_CONTACT_SUPPORT = "https://www.gonoise.com/pages/register-your-complaint"
    const val URL_PRIVACY_POLICY = "https://www.gonoise.com/pages/app-privacy-policy"
    const val URL_FEEDS_GUIDELINES =
        "https://www.gonoise.com/pages/community-guidelines-for-noise-fit"
    const val URL_TERMS_OF_USE = "https://www.gonoise.com/pages/terms-of-use"
    const val URL_NPL_TERMS_OF_USE =
        "https://www.gonoise.com/pages/terms-and-conditions-for-noise-premiere-league"
    const val URL_GOOGLE_FIT = "https://www.google.com/fit"
    const val URL_WARRANTY_REGISTRATION = "https://www.gonoise.com/pages/warranty-registration"
    const val NO_LUNA_RING = "https://www.gonoise.com/pages/luna-smart-ring"
    const val STEP_COUNT = "STEP COUNT"
    const val SLEEP_HOURS = "SLEEP HOURS"
    const val DISTANCE = "DISTANCE"
    const val HEART_RATE = "HEART RATE"
    const val STRESS_COUNT = "STRESS COUNT"
    const val BLOOD_OXYGEN = "BLOOD OXYGEN"
    const val BLOOD_PRESSURE = "BLOOD PRESSURE"
    const val BODY_TEMPERATURE = "BODY TEMPERATURE"

    //    const val METERS_IN_MILE = 1600
//    const val METERS_IN_KM = 1000
    const val KM_TO_MILE = 0.621


    const val SOCIAL_APP_PACKAGE_WHATSAPP_B = "com.whatsapp.w4b"
    const val SOCIAL_APP_PACKAGE_WHATSAPP = "com.whatsapp"
    const val SOCIAL_APP_PACKAGE_TELEGRAM = "org.telegram.messenger"
    const val SOCIAL_APP_PACKAGE_SNAPCHAT = "com.snapchat.android"
    const val SOCIAL_APP_PACKAGE_SLACK = "com.Slack"
    const val SOCIAL_APP_PACKAGE_YOUTUBE = "com.google.android.youtube"
    const val SOCIAL_APP_PACKAGE_OUTLOOK = "com.microsoft.office.outlook"
    const val SOCIAL_APP_PACKAGE_INSTAGRAM = "com.instagram.android"
    const val SOCIAL_APP_PACKAGE_LINKEDIN = "com.linkedin.android"
    const val SOCIAL_APP_PACKAGE_GMAIL = "com.google.android.gm"
    const val SOCIAL_APP_PACKAGE_VIBER = "com.viber.voip"
    const val SOCIAL_APP_PACKAGE_FACEBOOK = "com.facebook.katana"
    const val SOCIAL_APP_PACKAGE_FACEBOOK_M = "com.facebook.orca"
    const val SOCIAL_APP_PACKAGE_SKYPE = "com.skype.raider"
    const val SOCIAL_APP_PACKAGE_TWITTER = "com.twitter.android"
    const val SOCIAL_APP_PACKAGE_WECHAT = "com.tencent.mm"
    const val MISS_CALL_ALERTS = "com.android.phone"
    const val SMS_ALERTS = "com.android.mms"
    const val GOOGLE_MAPS = "com.google.android.apps.maps"
    const val PACKAGE_GOOGLE_CALENDAR = "com.google.android.calendar"
    const val PACKAGE_PINTEREST = "com.pinterest"
    const val PACKAGE_AMAZON = "in.amazon.mShop.android.shopping"
    const val PACKAGE_FLIPKART = "com.flipkart.android"
    const val PACKAGE_GOOGLE_NEWS = "com.google.android.apps.magazines"
    const val PACKAGE_GOOGLE_PAY = "com.google.android.apps.nbu.paisa.user"
    const val PACKAGE_HANGOUT = "com.google.android.talk"
    const val PACKAGE_OLA = "com.olacabs.customer"
    const val PACKAGE_PAYTM = "net.one97.paytm"
    const val PACKAGE_PHONE_PE = "com.phonepe.app"
    const val PACKAGE_UBER = "com.ubercab"
    const val PACKAGE_NAUKRI = "naukriApp.appModules.login"
    const val PACKAGE_INSHORTS = "com.nis.app"

    // Find Buddy Status...
    const val BUDDY_STATUS_ADD = "1"
    const val BUDDY_STATUS_INVITE = "2"
    const val BUDDY_STATUS_ACCEPT = "3"
    const val BUDDY_STATUS_REJECTED = "4"
    const val BUDDY_STATUS_WITHDRAW = "5"
    const val BUDDY_STATUS_REMOVE = "6"

    //Read more/less
    const val MAX_LINES = Int.MAX_VALUE
    const val MIN_LINES = 10
    const val MAX_LINES_COMMENT = Int.MAX_VALUE
    const val MIN_LINES_COMMENT = 2


    val sportsModeList = arrayListOf(
        SportsModeList.SportsMode(
            index = 1,
            name = "outdoor_walking",
            type = 52,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 2,
            name = "outdoor_running",
            type = 48,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 3,
            name = "outdoor_cycling",
            type = 50,
            value = true
        ), SportsModeList.SportsMode(
            index = 4,
            name = "indoor_walking",
            type = 53,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 5,
            name = "indoor_running",
            type = 49,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 6,
            name = "indoor_cycling",
            type = 51,
            value = true
        ), SportsModeList.SportsMode(
            index = 7,
            name = "elliptical",
            type = 56,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 8,
            name = "rower",
            type = 57,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 9,
            name = "pool_swimming",
            type = 54,
            value = true
        ), SportsModeList.SportsMode(
            index = 10,
            name = "open_water_swimming",
            type = 55,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 11,
            name = "cricket",
            type = 75,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 12,
            name = "hiking",
            type = 4,
            value = true
        ), SportsModeList.SportsMode(
            index = 13,
            name = "yoga",
            type = 18,
            value = true
        ), SportsModeList.SportsMode(
            index = 14,
            name = "workout",
            type = 8,
            value = true
        )
    )

    val sportsModeListOxy = arrayListOf(
        SportsModeList.SportsMode(
            index = 1,
            name = "outdoor_walking",
            type = 52,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 2,
            name = "outdoor_running",
            type = 48,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 3,
            name = "outdoor_cycling",
            type = 50,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 4,
            name = "indoor_walking",
            type = 53,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 5,
            name = "indoor_running",
            type = 49,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 6,
            name = "indoor_cycling",
            type = 51,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 7,
            name = "elliptical",
            type = 56,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 8,
            name = "rower",
            type = 57,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 9,
            name = "pool_swimming",
            type = 54,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 10,
            name = "open_water_swimming",
            type = 55,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 11,
            name = "cricket",
            type = 75,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 12,
            name = "hiking",
            type = 4,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 13,
            name = "yoga",
            type = 18,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 14,
            name = "workout",
            type = 9,
            value = true
        )
    )


    val sportsModeListPro2 = arrayListOf(
        SportsModeList.SportsMode(
            index = 1,
            name = "running",
            type = 2,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 2,
            name = "walking",
            type = 1,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 3,
            name = "biking",
            type = 3,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 4,
            name = "hiking",
            type = 4,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 5,
            name = "climbing",
            type = 6,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 6,
            name = "treadmill",
            type = 12,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 7,
            name = "spinning",
            type = 58,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 8,
            name = "yoga",
            type = 18,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 9,
            name = "workout",
            type = 9,
            value = true
        )
    )
    val sportsModeListCF2 = arrayListOf(
        SportsModeList.SportsMode(
            index = 1,
            name = "outdoor_walking",
            type = 52,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 2,
            name = "outdoor_running",
            type = 48,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 3,
            name = "outdoor_cycling",
            type = 50,
            value = true
        ), SportsModeList.SportsMode(
            index = 4,
            name = "indoor_walking",
            type = 53,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 5,
            name = "indoor_running",
            type = 49,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 6,
            name = "indoor_cycling",
            type = 51,
            value = true
        ), SportsModeList.SportsMode(
            index = 7,
            name = "elliptical",
            type = 56,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 8,
            name = "rower",
            type = 57,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 9,
            name = "pool_swimming",
            type = 54,
            value = true
        ), SportsModeList.SportsMode(
            index = 10,
            name = "open_water_swimming",
            type = 55,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 11,
            name = "cricket",
            type = 75,
            value = true
        ),
        SportsModeList.SportsMode(
            index = 12,
            name = "hiking",
            type = 4,
            value = true
        ), SportsModeList.SportsMode(
            index = 13,
            name = "yoga",
            type = 18,
            value = true
        ), SportsModeList.SportsMode(
            index = 14,
            name = "workout",
            type = 8,
            value = true
        )
    )


    fun teamNameMapping(
        shortName: String?,
        fullName: String,
        returnFirstName: Boolean = true
    ): String {
        val name = if (!shortName.isNullOrEmpty()) {
            shortName
        } else if (!fullName.isNullOrEmpty()) {
            fullName
        } else {
            ""
        }

        if (name.isEmpty()) return ""

        return if (name.equals("Chennai Super Kings", true) || name.equals("CSK", true)) {
            "Chennai"
        } else if (name.equals("Delhi Capitals", true) || name.equals("DC", true)) {
            "Delhi"
        } else if (name.equals("Delhi", true) || name.equals("DC", true)) {
            "Delhi"
        } else if (name.equals("Gujarat Titans", true) || name.equals("GT", true)) {
            "Gujarat"
        } else if (name.equals("Kolkata Knight Riders", true) || name.equals(
                "KOL",
                true
            ) || name.equals("KKR", true)
        ) {
            "Kolkata"
        } else if (name.equals("Lucknow Super Giants", true) || name.equals("LSG", true)) {
            "Lucknow"
        } else if (name.equals("Mumbai Indians", true) || name.equals("MI", true)) {
            "Mumbai"
        } else if (name.equals("Punjab Kings", true) || name.equals("PBKS", true)) {
            "Punjab"
        } else if (name.equals("Rajasthan Royals", true) || name.equals("RR", true)) {
            "Rajasthan"
        } else if (name.equals("Royal Challengers Bangalore", true) || name.equals(
                "RCB",
                true
            ) || name.equals("BLR", true)
        ) {
            "Bangalore"
        } else if (name.equals("Sunrisers Hyderabad", true) || name.equals("SRH", true)) {
            "Hyderabad"
        } else {
            if (returnFirstName) {
                val firstName = fullName.split(" ")
                return firstName.firstOrNull() ?: ""
            } else {
                fullName
            }
        }
    }

}