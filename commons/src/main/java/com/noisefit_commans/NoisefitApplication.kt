package com.noisefit_commans


import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.LocaleHelper
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject


open class NoisefitApplication : MultiDexApplication() {

    @Inject
    lateinit var gson: Gson

    companion object {
        var context: Application? = null

        fun setLocale(languageCode: String) {
            if (context != null) {
                LocaleHelper.setLocale(context!!, languageCode)
                LOGS.d("sdfljkshdkfjhskdjf locale set")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        if (BuildConfig.DEBUG) {
            Timber.plant(LOGS)
        }


    }


}