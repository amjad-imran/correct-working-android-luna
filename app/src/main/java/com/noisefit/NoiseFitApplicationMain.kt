package com.noisefit

import android.app.Application
import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
//import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.github.anrwatchdog.ANRWatchDog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.noisefit.luna.BuildConfig
import com.noisefit.ui.SplashActivity
import com.noisefit.watch.ApplicationHandler
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.FileLogsUtils
import com.useinsider.insider.Insider
import com.useinsider.insider.InsiderCallbackType
import dagger.hilt.android.HiltAndroidApp
import java.util.*
import javax.inject.Inject

@HiltAndroidApp
class NoiseFitApplicationMain : NoisefitApplication(), Configuration.Provider {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var applicationHandler: ApplicationHandler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        var context: Application? = null
    }

    override fun onCreate() {
//        ActivityLifecycleCallback.register(this)
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        context = this
        FileLogsUtils.initLogs(applicationContext)
        AppLogs.initAppLogs(applicationContext)
        setDefaultLanguage(this)
        // TODO: Please change with your partner name.
        // Make sure that all the letters are lowercase.
        Insider.Instance.init(this, BuildConfig.INSIDER_PARTNER)


        Insider.Instance.registerInsiderCallback { data, callbackType ->
            when (callbackType) {
                InsiderCallbackType.NOTIFICATION_OPEN -> Log.d(
                    "[INSIDER]",
                    "[NOTIFICATION_OPEN]: $data"
                )
                InsiderCallbackType.TEMP_STORE_CUSTOM_ACTION -> Log.d(
                    "[INSIDER]",
                    "[TEMP_STORE_CUSTOM_ACTION]: $data"
                )
                else -> {}
            }
        }
        Insider.Instance.setSplashActivity(SplashActivity::class.java)
        Insider.Instance.enableIDFACollection(true)
        Insider.Instance.currentUser.setLocale("tr_TR")

        if (BuildConfig.DEBUG) {
            ANRWatchDog().setIgnoreDebugger(true)
                .setANRListener { error -> // Handle the error. For example, log it to HockeyApp:
                    error.message?.let { AppLogs.sendAppLogs(it) }
                    error?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                }.start();
        }

        NoisefitApplication.localDataStore = localDataStore
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    fun setDefaultLanguage(context: Context) {
        val locale = Locale(Locale.ENGLISH.language)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration()
        config.locale = locale
        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )
    }

}