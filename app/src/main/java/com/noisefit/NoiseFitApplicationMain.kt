package com.noisefit

//import com.clevertap.android.sdk.ActivityLifecycleCallback
import android.app.Application
import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.freshchat.consumer.sdk.Freshchat
import com.freshchat.consumer.sdk.FreshchatConfig
import com.github.anrwatchdog.ANRWatchDog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.moengage.core.DataCenter
import com.moengage.core.LogLevel
import com.moengage.core.MoECoreHelper
import com.moengage.core.MoEngage
import com.moengage.core.config.FcmConfig
import com.moengage.core.config.LogConfig
import com.moengage.core.config.NotificationConfig
import com.moengage.core.config.PushKitConfig
import com.moengage.core.ktx.MoEngageBuilderKtx
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.inapp.MoEInAppHelper
import com.moengage.pushbase.MoEPushHelper
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit.watch.ApplicationHandler
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.FileLogsUtils
import com.oreo.util.MyActivityLifecycleCallbacks
import com.oreo.util.language.LocaleHelper
import com.oreo.util.moengage.callbacks.ApplicationBackgroundListener
import com.oreo.util.moengage.callbacks.LogoutCompleteListener
import com.oreo.util.moengage.inapp.ClickActionCallback
import com.oreo.util.moengage.inapp.InAppLifecycleCallbacks
import com.oreo.util.moengage.inapp.SelfHandledCallback
import com.oreo.util.moengage.push.CustomPushMessageListener

import dagger.hilt.android.HiltAndroidApp
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

    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    companion object {
        var context: Application? = null

        var appLanguage: AppLanguage = ApplicationUtils.getDefaultLanguage()
        fun updateUserLanguage(language: AppLanguage) {
            appLanguage = language
            LocaleHelper.setLocale(context!!, language.languageCode)
            NoisefitApplication.setLocale(appLanguage.languageCode)
        }
    }

    override fun onCreate() {
//        ActivityLifecycleCallback.register(this)
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        context = this

        val savedLanguage = localDataStore.getSelectedAppLanguage()
        if (savedLanguage != null) {
            appLanguage = ApplicationUtils.getAppLanguageByCode(savedLanguage)
        } else {
            val phoneLanguage = Resources.getSystem().configuration.locale.language/*Locale.getDefault().language*/
            val language = ApplicationUtils.getAppLanguageByCode(phoneLanguage)
            appLanguage = language
            localDataStore.saveSelectedAppLanguage(language.languageCode)
        }
        NoisefitApplication.setLocale(appLanguage.languageCode)
        registerActivityLifecycleCallbacks(MyActivityLifecycleCallbacks(sessionManager,resourcesProvider))
        FileLogsUtils.initLogs(applicationContext)
        AppLogs.initAppLogs(applicationContext)
        //setDefaultLanguage(this)
        initMoEngage()

        //freshchat initialization
        initialiseFreshChat()

        if (BuildConfig.DEBUG) {
            ANRWatchDog().setIgnoreDebugger(true)
                .setANRListener { error -> // Handle the error. For example, log it to HockeyApp:
                    error.message?.let { AppLogs.sendAppLogs(it) }
                    error?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                }.start();
        }
    }

    private fun initialiseFreshChat() {
        val freshchatConfig = FreshchatConfig(
            "a75cade2-33cb-453a-9e2c-9091c550484c",
            "3a653bd1-bfda-4213-8649-a2b79b4f7b06"
        )
        freshchatConfig.domain = "msdk.in.freshchat.com"
        Freshchat.getInstance(applicationContext).init(freshchatConfig)
    }

    private fun initMoEngage() {

        val moEngage = MoEngage.Builder(this,
            "VWZYK0ZFPRQZM6G2WG9YBV47", DataCenter.DATA_CENTER_3)
            .configureNotificationMetaData(NotificationConfig(R.drawable.icon_transparent, R.drawable.icon_transparent))
            .build()
        MoEngage.initialiseDefaultInstance(moEngage)


        /*MoEngage.initialiseDefaultInstance(
            MoEngageBuilderKtx(
                application = this,
                appId = "VWZYK0ZFPRQZM6G2WG9YBV47_DEBUG",
                dataCenter = DataCenter.DATA_CENTER_3,
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
                logConfig = LogConfig(LogLevel.DEBUG, true)
            ).build()
        )*/

        MoEPushHelper.getInstance().pushPermissionResponse(applicationContext, true)
        MoEPushHelper.getInstance().setUpNotificationChannels(applicationContext)
        // register for application background listener
        MoECoreHelper.addAppBackgroundListener(ApplicationBackgroundListener())
        // register for logout complete listener
        MoECoreHelper.addLogoutCompleteListener(LogoutCompleteListener())
        setupPushCallbacks()
        setupInAppCallbacks()
    }

    private fun setupPushCallbacks() {
        // callback for notification events and notification customisation point.
        MoEPushHelper.getInstance().registerMessageListener(CustomPushMessageListener())
        // Callback for Firebase Token
        MoEFireBaseHelper.getInstance().addTokenListener { token ->
            Log.d(" fcm token:", "${token.pushToken}")
            MoEFireBaseHelper.getInstance().passPushToken(applicationContext, token.pushToken)
        }
    }

    private fun setupInAppCallbacks() {
        // callback for in-app campaign click
        MoEInAppHelper.getInstance().setClickActionListener(ClickActionCallback(sessionManager))
        // callback for in-app lifecycle - campaign shown/dismissed.
        MoEInAppHelper.getInstance().addInAppLifeCycleListener(InAppLifecycleCallbacks())
        // callback for self handled campaigns that are triggered based on events.
        MoEInAppHelper.getInstance().setSelfHandledListener(SelfHandledCallback())
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(base!!))
    }

}