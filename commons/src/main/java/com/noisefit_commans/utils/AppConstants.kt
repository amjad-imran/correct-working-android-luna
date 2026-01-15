package com.noisefit_commans.utils

object AppConstants {


    const val OTP_RESEND_TIMER = 60000L
    const val OTP_RESEND_SECONDS = (OTP_RESEND_TIMER /1000).toInt()


    const val URL_CONTACT_SUPPORT = "https://www.gonoise.com/pages/register-your-complaint"
    const val URL_PRIVACY_POLICY = "https://www.gonoise.com/pages/luna-ring-privacy-policy"
    const val URL_TERMS_OF_USE = "https://www.gonoise.com/pages/terms-of-use"
    const val URL_GOOGLE_FIT = "https://www.google.com/fit"
    const val URL_WARRANTY_REGISTRATION = "https://www.gonoise.com/pages/warranty-registration"
    const val NO_LUNA_RING = "https://www.gonoise.com/pages/luna-smart-ring"

    const val KM_TO_MILE = 0.621

}

object GraphType {

    object Day {
        const val REM_SLEEP = "rem_sleep_day"
        const val DEEP_SLEEP = "deep_sleep_day"
        const val SLEEP_EFFICIENCY = "sleep_efficiency_day"
        const val TOTAL_DURATION = "total_duration_day"
        const val LATENCY = "latency_day"
        const val RESTFULLNESS = "restfulness_day"
        const val HRV = "hrv_day"
        const val RHR = "rhr_day"
        const val AVG_SKIN_TEMP = "avg_skin_temp_day"
        const val AVG_OXY = "avg_oxy_day"
        const val AVG_RESPIRATION = "avg_respiration_day"
        const val CIRCADIAN_MID_POINT = "circadian_mid_point_day"
    }

    object Week {
        const val REM_SLEEP = "rem_sleep_week"
        const val DEEP_SLEEP = "deep_sleep_week"
        const val SLEEP_EFFICIENCY = "sleep_efficiency_week"
        const val TOTAL_DURATION = "total_duration_week"
        const val LATENCY = "latency_week"
        const val RESTFULLNESS = "restfulness_week"
        const val HRV = "hrv_week"
        const val RHR = "rhr_week"
        const val AVG_SKIN_TEMP = "avg_skin_temp_week"
        const val AVG_OXY = "avg_oxy_week"
        const val AVG_RESPIRATION = "avg_respiration_week"
        const val CIRCADIAN_MID_POINT = "circadian_mid_point_week"
    }

    object Month {
        const val REM_SLEEP = "rem_sleep_month"
        const val DEEP_SLEEP = "deep_sleep_month"
        const val SLEEP_EFFICIENCY = "sleep_efficiency_month"
        const val TOTAL_DURATION = "total_duration_month"
        const val LATENCY = "latency_month"
        const val RESTFULLNESS = "restfulness_month"
        const val HRV = "hrv_month"
        const val RHR = "rhr_month"
        const val AVG_SKIN_TEMP = "avg_skin_temp_month"
        const val AVG_OXY = "avg_oxy_month"
        const val AVG_RESPIRATION = "avg_respiration_month"
        const val CIRCADIAN_MID_POINT = "circadian_mid_point_month"
    }
}
