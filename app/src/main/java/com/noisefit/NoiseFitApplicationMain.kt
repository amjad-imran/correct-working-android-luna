package com.noisefit

//import com.clevertap.android.sdk.ActivityLifecycleCallback
import android.app.Application
import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.github.anrwatchdog.ANRWatchDog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.moengage.core.DataCenter
import com.moengage.core.LogLevel
import com.moengage.core.MoEngage
import com.moengage.core.config.FcmConfig
import com.moengage.core.config.GeofenceConfig
import com.moengage.core.config.LogConfig
import com.moengage.core.config.NotificationConfig
import com.moengage.core.config.PushKitConfig
import com.moengage.core.ktx.MoEngageBuilderKtx
import com.moengage.pushbase.MoEPushHelper
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.ui.SplashActivity
import com.noisefit.watch.ApplicationHandler
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.FileLogsUtils
import com.oreo.util.MyActivityLifecycleCallbacks

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

    @Inject
    lateinit var sessionManager: SessionManager

    companion object {
        var context: Application? = null
    }

    override fun onCreate() {
//        ActivityLifecycleCallback.register(this)
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        context = this
        registerActivityLifecycleCallbacks(MyActivityLifecycleCallbacks(sessionManager))
        FileLogsUtils.initLogs(applicationContext)
        AppLogs.initAppLogs(applicationContext)
        setDefaultLanguage(this)
        initMoEngage()
        // TODO: Please change with your partner name.
        // Make sure that all the letters are lowercase.



        if (BuildConfig.DEBUG) {
            ANRWatchDog().setIgnoreDebugger(true)
                .setANRListener { error -> // Handle the error. For example, log it to HockeyApp:
                    error.message?.let { AppLogs.sendAppLogs(it) }
                    error?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                }.start();
        }
    }

    private fun initMoEngage() {
        MoEngage.initialiseDefaultInstance(
            MoEngageBuilderKtx(
                application = this,
                appId = "VWZYK0ZFPRQZM6G2WG9YBV47",
                dataCenter= DataCenter.DATA_CENTER_3,
                notificationConfig = NotificationConfig(
                    smallIcon = R.drawable.icon_transparent,
                    largeIcon =
                    R.drawable.icon_transparent,
                    notificationColor = R.color.black,
                    isMultipleNotificationInDrawerEnabled = true,
                    isBuildingBackStackEnabled = true,
                    isLargeIconDisplayEnabled = true
                ),
                fcmConfig = FcmConfig(true),
                pushKitConfig = PushKitConfig(true),
                geofenceConfig = GeofenceConfig(true),
                logConfig = LogConfig(LogLevel.DEBUG,true)
            ).build()
        )

        MoEPushHelper.getInstance().pushPermissionResponse(applicationContext, true)
        MoEPushHelper.getInstance().setUpNotificationChannels(applicationContext)
//        // register for application background listener
//        MoECoreHelper.addAppBackgroundListener(ApplicationBackgroundListener())
//        // register for logout complete listener
//        MoECoreHelper.addLogoutCompleteListener(LogoutCompleteListener())
//        setupPushCallbacks()
//        setupInAppCallbacks()
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