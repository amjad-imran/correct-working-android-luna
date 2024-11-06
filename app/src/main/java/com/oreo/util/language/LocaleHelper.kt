package com.oreo.util.language

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import com.noisefit.NoiseFitApplicationMain
import java.util.Locale


object LocaleHelper {

    fun setLocale(context: Context, language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLayoutDirection(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            configuration.locale = locale
            configuration.setLocale(locale)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }

    fun onAttach(context: Context): Context? {
        return setLocale(context, NoiseFitApplicationMain.appLanguage.languageCode)
    }

    fun getSystemDefaultLanguage(): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0).language
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale.language
        }

        return locale
    }

}