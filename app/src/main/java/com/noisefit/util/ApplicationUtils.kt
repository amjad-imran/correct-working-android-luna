package com.noisefit.util


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.Address
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.ImageView
import androidx.core.app.NotificationManagerCompat
import androidx.core.util.Preconditions.checkArgument
import androidx.work.*
import com.google.gson.Gson
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.noisefit.BuildConfig
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.receiver.broadcastReceiver.SportsNotificationReceiver
import com.noisefit.receiver.service.NotificationAlertService
import com.noisefit.receiver.workManager.*
import com.noisefit_commans.common.roundToNearestDecimalFlooor
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.ui.loadCircleCacheWithProgress
import com.noisefit_commans.ui.loadCircleWCacheWithProgress
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.loadImageWCacheWithProgress
import com.noisefit.watch.WatchForm
import com.noisefit_commans.data.response.NplLeague
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.models.SleepType
import com.noisefit_commans.models.TaskEnums
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import com.oreo.receiver.workManager.OreoSyncDataWork
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor


private const val UniqueSyncDataWorkName: String = "SyncDataWork"
private const val UniqueRingSyncDataWorkName: String = "RingSyncDataWork"
private const val UniqueWeatherWorkName: String = "WeatherWork"
private const val UniqueGoogleSyncDataWorkName: String = "GoogleSyncDataWork"
private const val UniqueActivitySyncWorkName: String = "ActivitySyncWork"
const val UniqueWatchFaceSyncWorkName: String = "UniqueWatchFaceSyncWorkName"
const val UniqueDiyWatchFaceSyncWorkName: String = "UniqueDiyWatchFaceSyncWorkName"
private const val UniqueAgpsReminderWorkName: String = "AgpsReminderWork"
private const val UniqueSportSyncWorkName: String = "SportSyncWork"
const val UniqueMatchReminderWorkName: String = "MatchReminderWork"

const val RescueServiceInBgWorker: String = "RescueServiceInBgWorker"


object ApplicationUtils {

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        return pwrm.isIgnoringBatteryOptimizations(name)
    }

    fun getBarWidth(containerWidth: Float, count: Int): Float {
        var barWidth = 10
        when (count) {
            30, 31, 29 -> {
                barWidth = 8
            }

            7 -> {
                barWidth = 20
            }

            12 -> {
                barWidth = 10
            }
        }
        return (barWidth * count) / containerWidth
    }

    fun getAppVersion(): String {
        return BuildConfig.VERSION_NAME
    }

    private fun getUniqueSyncDataWorkName(): String {
        return UniqueSyncDataWorkName
    }

    private fun getUniqueRingSyncDataWorkName(): String {
        return UniqueRingSyncDataWorkName
    }

    private fun getUniqueGoogleFitWorkName(): String {
        return UniqueGoogleSyncDataWorkName
    }

    private fun getUniqueActivitySyncWorkName(): String {
        return UniqueActivitySyncWorkName
    }

    private fun getUniqueWatcFaceSyncWorkName(): String {
        return UniqueWatchFaceSyncWorkName
    }

    private fun getUniqueDiyWatchFaceSyncWorkName(): String {
        return UniqueDiyWatchFaceSyncWorkName
    }

    private fun getAGPSWorkName(): String {
        return UniqueAgpsReminderWorkName
    }

    private fun getUniqueSportSyncWorkName(): String {
        return UniqueSportSyncWorkName
    }

//    fun showLoadingDialog(context: Context): ProgressDialog {
//        val progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
//        progressDialog.show()
//        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        progressDialog.setContentView(R.layout.default_loader)
//        progressDialog.isIndeterminate = true
//        progressDialog.setCancelable(false)
//        progressDialog.setCanceledOnTouchOutside(false)
//        return progressDialog
//
//    }

    fun openAppSettings(activity: Activity) {
        val intent = Intent(
            ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + activity.packageName)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
    }

    fun requestNotificationAccess(activity: Activity) {
        val accessIntent = Intent()
        accessIntent.action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        } else "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
        accessIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        accessIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        accessIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        activity.startActivity(accessIntent)
    }

    fun isNotificationServiceRunning(context: Context): Boolean {
        val contentResolver: ContentResolver = context.contentResolver
        val enabledNotificationListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            NotificationAlertService.SERVICE_NAME
        )
    }


    fun seriesItemWithInset(
        context: Context,
        initialValue: Float,
        maxValue: Float,
        color: Int,
        inset: Float,
        width: Float
    ): SeriesItem {

        return SeriesItem.Builder(context.resources.getColor(color, null))
            .setInset(PointF(inset, inset))
            .setShowPointWhenEmpty(true)
            .setRange(0f, maxValue, initialValue).setLineWidth(width).build()
    }

    fun seriesItemWithoutInset(
        context: Context, initialValue: Float, maxValue: Float, color: Int, width: Float
    ): SeriesItem {
        return SeriesItem.Builder(context.resources.getColor(color, null))
            .setShowPointWhenEmpty(true)
            .setRange(0f, maxValue, initialValue).setLineWidth(width).build()
    }

    fun isLocationProviderEnabled(context: Context): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        }
        return false
    }

    fun <T> loadImage(bgImv: ImageView, url: T?, screenType: WatchForm) {
        when (screenType) {
            WatchForm.CIRCLE -> {
                bgImv.loadCircleCacheWithProgress(bgImv.context, url)
            }

            else -> {
                bgImv.loadImageCacheWithProgress(bgImv.context, url)
            }
        }
    }

    fun <T> loadImageWC(bgImv: ImageView, url: T?, screenType: WatchForm) {
        when (screenType) {
            WatchForm.CIRCLE -> {
                bgImv.loadCircleWCacheWithProgress(bgImv.context, url)
            }

            else -> {
                bgImv.loadImageWCacheWithProgress(bgImv.context, url)
            }
        }
    }

    fun generateRandomId(): Int {
        return (floor(Math.random() * 16) + 1).toInt()
    }

    fun startNotificationListenerService(localDataStore: DataStoredInterface, context: Context) {
        if (localDataStore.getConnectedDevice() == null) return
        if (!localDataStore.isNotificationAlertEnabled()) return
        if (notificationActionGranted(context)) {
            try {

                try {
                    val pm = context.packageManager
                    pm.setComponentEnabledSetting(
                        ComponentName(
                            context,
                            NotificationAlertService::class.java
                        ),
                        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        ComponentName(
                            context,
                            NotificationAlertService::class.java
                        ),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
                    )

                } catch (exp: Exception) {
                    exp.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun notificationActionGranted(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val packageName = context.packageName
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(packageName)
    }

    /**
     * Returns Hour,Monute
     */
    fun getFormattedSleepDuration(value: Int): Pair<Int, Int> {
        return if (value == 0) {
            Pair(0, 0)
        } else {
            val hours: Int = value / 60
            val minutes: Int = value % 60
            Pair(hours, minutes)
        }
    }

    /** value in seconds
     * Returns Hour,Minute
     */
    fun getFormattedSleepDurationFromSeconds(value: Int): Pair<Int, Int> {
        return if (value == 0) {
            Pair(0, 0)
        } else {
            val hours: Int = value / 3600
            val minutes: Int = (value % 3600) / 60
            Pair(hours, minutes)
        }
    }


    fun scheduleMatchWork(context: Context) {
        //scheduleMatchWork(11, 0, context)
        setMatchAlarm(11, 0, context)
    }

    private fun scheduleMatchWork(hour: Int, minute: Int, context: Context) {
        val uniqueId = UniqueMatchReminderWorkName
        LOGS.d("scheduleMatchWork inside added")

        val calendar: Calendar = Calendar.getInstance()
        val nowMillis: Long = calendar.timeInMillis
        if (calendar.get(Calendar.HOUR_OF_DAY) > hour ||
            calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) + 1 >= minute
        ) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val diff: Long = calendar.timeInMillis - nowMillis
        val mWorkManager = WorkManager.getInstance(context)
//        val constraints: Constraints = Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
        mWorkManager.cancelAllWorkByTag(uniqueId)

        WorkManager.getInstance(context).cancelUniqueWork(uniqueId)
        val work =
            OneTimeWorkRequest.Builder(MatchReminderWork::class.java)
                .addTag(uniqueId)
                .setInitialDelay(diff, TimeUnit.MILLISECONDS)
                .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueId,
            ExistingWorkPolicy.REPLACE,
            work
        )

        LOGS.d("scheduleMatchWork added")
    }

    private fun setMatchAlarm(hour: Int, minute: Int, context: Context) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SportsNotificationReceiver::class.java)
        intent.action = SportsNotificationReceiver.ACTION
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        val calendar: Calendar = Calendar.getInstance()
        if (calendar.get(Calendar.HOUR_OF_DAY) > hour ||
            calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) /*+ 1*/ >= minute
        ) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelMatchAlarm(context: Context) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager?
        val intent = Intent(context, SportsNotificationReceiver::class.java)
        intent.action = SportsNotificationReceiver.ACTION
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        alarmManager?.cancel(pendingIntent)
    }

    fun setRescueWorkManager(context: Context) {
        val request = OneTimeWorkRequestBuilder<RescueServiceInBgWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun startWeatherScheduler(context: Context) {

        val uniqueId = UniqueWeatherWorkName
        LOGS.d("$UniqueWeatherWorkName: inside startWeatherScheduler ")
        /* if (!isWorkScheduled(uniqueId, context)) {*/

        WorkManager.getInstance(context).cancelUniqueWork(uniqueId)
        val constraints = Constraints.Builder()
            .build()
        val work =
            PeriodicWorkRequest.Builder(WeatherWork::class.java, 1, TimeUnit.HOURS)
                .addTag(uniqueId)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueId,
            ExistingPeriodicWorkPolicy.KEEP,
            work

        )
        /*}*/
    }


    fun stopWeatherScheduler(context: Context) {
        LOGS.d("$UniqueWeatherWorkName: inside stopWeatherScheduler ")
        WorkManager.getInstance(context).cancelUniqueWork(UniqueWeatherWorkName)
    }

    fun stopSportScheduler(context: Context) {
        LOGS.d("$UniqueSportSyncWorkName: inside stopSportScheduler ")
        WorkManager.getInstance(context).cancelUniqueWork(UniqueSportSyncWorkName)
    }


    fun stopMatchReminderScheduler(context: Context) {
        cancelMatchAlarm(context)
        WorkManager.getInstance(context).cancelUniqueWork(UniqueMatchReminderWorkName)

    }

    fun clearJobs(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    suspend fun startSyncScheduler(context: Context): Boolean {
        val uniqueId = getUniqueSyncDataWorkName()
        LOGS.d("SyncDataWork: inside startSyncScheduler ")
        if (!isWorkScheduled(uniqueId, context)) {

            WorkManager.getInstance(context).cancelUniqueWork(uniqueId)
            val constraints = Constraints.Builder()
                .build()


            val work =
                PeriodicWorkRequest.Builder(SyncDataWork::class.java, 60, TimeUnit.MINUTES)
                    .addTag(uniqueId)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueId,
                ExistingPeriodicWorkPolicy.KEEP,
                work

            )
            return true
        }

        return false
    }

    suspend fun startOreoSyncScheduler(context: Context): Boolean {
        val uniqueId = getUniqueRingSyncDataWorkName()
        val isWorkScheduled = isWorkScheduled(uniqueId, context)
        LOGS.d("SyncDataWork: inside startOreoSyncScheduler $uniqueId isWorkScheduled $isWorkScheduled")
        if (!isWorkScheduled) {

            WorkManager.getInstance(context).cancelUniqueWork(uniqueId)
            val constraints = Constraints.Builder()
                .build()


            val work =
                PeriodicWorkRequest.Builder(OreoSyncDataWork::class.java, 60, TimeUnit.MINUTES)
                    .addTag(uniqueId)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueId,
                ExistingPeriodicWorkPolicy.KEEP,
                work

            )
            return true
        }

        return false
    }

    fun startSportScheduler(context: Context) {
        val uniqueId = getUniqueSportSyncWorkName()
        LOGS.d("SportWork: inside startSportScheduler ")
        AppLogs.sendAppLogs("startSportScheduler")
        WorkManager.getInstance(context).cancelUniqueWork(uniqueId)

        val work = OneTimeWorkRequest.Builder(SportWork::class.java)
            .addTag(uniqueId)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueId,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    suspend fun startGoogleFitSyncScheduler(context: Context): Boolean {
        val uniqueId = getUniqueGoogleFitWorkName()
        LOGS.d("SyncDataWork: inside startGoogleFitSyncScheduler ")
        if (!isWorkScheduled(uniqueId, context)) {

            WorkManager.getInstance(context).cancelUniqueWork(uniqueId)
            val work =
                OneTimeWorkRequest.Builder(GoogleFitSyncWork::class.java)
                    .addTag(uniqueId)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueId,
                ExistingWorkPolicy.KEEP,
                work
            )
            return true
        }

        return false
    }

    suspend fun startActivitySyncScheduler(context: Context): Boolean {
        val uniqueId = getUniqueActivitySyncWorkName()
        LOGS.d("ActivitySyncWork: inside startActivitySyncScheduler ")
        if (!isWorkScheduled(uniqueId, context)) {

            WorkManager.getInstance(context).cancelUniqueWork(uniqueId)
            val work =
                OneTimeWorkRequest.Builder(ActivitySyncWork::class.java)
                    .addTag(uniqueId)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueId,
                ExistingWorkPolicy.KEEP,
                work
            )
            return true
        }

        return false
    }

    suspend fun isWatchFaceTransferInProgress(context: Context): Boolean {
        val uniqueId = getUniqueWatcFaceSyncWorkName()
        return isWorkScheduled1(uniqueId, context)
    }

    suspend fun isDIYWatchFaceTransferInProgress(context: Context): Boolean {
        val uniqueId = getUniqueDiyWatchFaceSyncWorkName()
        return isWorkScheduled1(uniqueId, context)
    }

    private suspend fun isWorkScheduled1(workName: String, context: Context): Boolean {
        var running = false
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(workName).await()
        if (workInfos == null || workInfos.size == 0) return false
        for (workStatus in workInfos) {
            running =
                (workStatus.state == WorkInfo.State.RUNNING) or (workStatus.state == WorkInfo.State.ENQUEUED)
        }
        return running
    }


    suspend fun startWatchFaceTransferWorker(context: Context, watchface: Watchface2): Boolean {
        val uniqueId = getUniqueWatcFaceSyncWorkName()
        LOGS.d("WatchFaceTransferWorker: inside startWatchFaceTransferWorker ")
        if (!isWorkScheduled(uniqueId, context)) {

            WorkManager.getInstance(context).cancelUniqueWork(uniqueId)

            val data = Data.Builder()
            data.putString("watchFace", Gson().toJson(watchface))

            val work =
                OneTimeWorkRequest.Builder(WatchfaceWork::class.java)
                    .setInputData(data.build())
                    .addTag(uniqueId)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueId,
                ExistingWorkPolicy.REPLACE,
                work
            )
            return true
        }

        return false
    }

    suspend fun startDiyWatchFaceTransferWorker(
        context: Context,
        watchFace: DiyCustomWatchFace
    ): Boolean {
        val uniqueId = getUniqueDiyWatchFaceSyncWorkName()
        LOGS.d("WatchFaceTransferWorker: inside startDiyWatchFaceTransferWorker ")
        if (!isWorkScheduled(uniqueId, context)) {

            WorkManager.getInstance(context).cancelUniqueWork(uniqueId)

            val data = Data.Builder()
            data.putString("watchFace", Gson().toJson(watchFace))

            val work =
                OneTimeWorkRequest.Builder(DiyWatchfaceWork::class.java)
                    .setInputData(data.build())
                    .addTag(uniqueId)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueId,
                ExistingWorkPolicy.KEEP,
                work
            )
            return true
        }

        return false
    }

    suspend fun startAGPSScheduler(context: Context): Boolean {
        val uniqueId = getAGPSWorkName()
        LOGS.d("ActivitySyncWork: inside startActivitySyncScheduler ")
        if (!isWorkScheduled(uniqueId, context)) {

            WorkManager.getInstance(context).cancelUniqueWork(uniqueId)
            val work =
                OneTimeWorkRequest.Builder(AgpsReminderWork::class.java)
                    .addTag(uniqueId)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueId,
                ExistingWorkPolicy.REPLACE,
                work
            )
            return true
        }

        return false
    }

    private suspend fun isWorkScheduled(workName: String, context: Context): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(workName).await()
        return if (workInfos.size == 1) {
            val workInfo = workInfos[0]
            workInfo.state == WorkInfo.State.BLOCKED || workInfo.state == WorkInfo.State.RUNNING
        } else {
            false
        }
    }


    fun convertMapToBundle(map: HashMap<String, Any>): Bundle {
        val bundle = Bundle()
        map.forEach {
            val data = it.value
            data.let { _ ->
                val key = it.key.lowercase().replace(" ", "_")
                when (data) {
                    is String -> {
                        bundle.putString(key, it.value as String)
                    }

                    is Int -> {
                        bundle.putInt(key, it.value as Int)
                    }

                    is Boolean -> {
                        bundle.putBoolean(key, it.value as Boolean)
                    }

                    is Double -> {
                        bundle.putDouble(key, it.value as Double)
                    }

                }
            }

        }
        return bundle
    }

    fun getChallengeTypeText(challengeType: String): String {
        return when (challengeType) {
            "step" -> {
                "Steps"
            }

            "distance" -> {
                "Distance"
            }

            "Calories" -> {
                "Calories"
            }

            else -> ""
        }
    }

    fun getChallengeTypeTextWithSuffix(challengeType: String): String {
        return when (challengeType) {
            "step" -> {
                "Steps you covered"
            }

            "distance" -> {
                "Distance you covered"
            }

            "calories" -> {
                "Calories you burned"
            }

            else -> ""
        }
    }

    fun getChallengeTypeUnit(challengeType: String, unit: Units): String {
        when (challengeType) {
            "step" -> {
                return "Steps"
            }

            "distance" -> {
                return "Kms"
//                return if (unit == Units.IMPERIAL) {
//                    "mi"
//                } else {
//                    "Kms"
//                }
            }

            "calories" -> {
                return "Kcal"
            }

            else -> return ""
        }
    }

    fun getNumberAsPerUnit(challengeType: String, number: Double, unit: Units): String {
        if (challengeType == "distance") {
            return if (unit == Units.IMPERIAL) {
                val newNumber = number * AppConstants.KM_TO_MILE
                if (newNumber % 1 == 0.0) {
                    (newNumber.toInt()).toString()
                } else {
                    String.format("%.2f", newNumber)
                }
            } else {
                if ((number % 1) == 0.0) {
                    (number.toInt()).toString()
                } else {
                    String.format("%.2f", number)
                }
            }
        } else {
            return (number.toInt()).toString()
        }
    }

    fun getChallengeTypeIcon(challengeType: String): Int {
//        return when (challengeType) {
//            "step" -> {
//                R.drawable.ic_steps_gradient
//            }
//            "distance" -> {
//                R.drawable.ic_distance_gradient
//            }
//            "calories" -> {
//                R.drawable.ic_calorie_gradient
//            }
//            else -> 0
//        }
        return 0
    }

    fun isOutSideIndia(): Boolean {
        return !(TimeZone.getDefault().id.equals("Asia/Kolkata") || TimeZone.getDefault().id.equals(
            "Asia/Calcutta"
        ))
    }

    fun isInternetConnected(): Boolean {
        var status = false
        val cm =
            NoiseFitApplicationMain.context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork) != null) {
                // connected to the internet
                status = true
            }
        } else {
            if (cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnectedOrConnecting) {
                // connected to the internet
                status = true
            }
        }

        return status
    }

    fun getLocationString(address: Address?): String {
        if (address == null) return "N/A"

        val subLocality = address.subLocality
        val locality = address.locality
        val countryName = address.countryName
        val locationArray = ArrayList<String>()
        if (subLocality != null) {
            locationArray.add(subLocality)
        }
        if (locality != null) {
            locationArray.add(locality)
        }
        if (countryName != null) {
            locationArray.add(countryName)
        }
        return locationArray.joinToString(", ")
    }

    fun getLocationStringCityState(addresses: List<Address>?): Pair<String, String>? {
        if (addresses.isNullOrEmpty()) return null

        addresses.forEach { address ->
            val pattern = Regex("^[a-z A-Z]*$")
            if ((address.locality != null)
                && (address.locality.matches(pattern))
            ) {
                val city = address.locality
                val state = address.adminArea
                return Pair(city, state)
            }
        }
        return null
    }

    @Throws(ParseException::class)
    fun getDateInMMMDDYYYY(stringData: String?): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val data = sdf.parse(stringData!!)
        return output.format(data!!)
    }

    @SuppressLint("RestrictedApi")
    fun getDayOfMonthSuffix(n: Int): String {
        checkArgument(n in 1..31, "illegal day of month: $n")
        return if (n in 11..13) {
            "th"
        } else when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }


    fun getRepeatDays(repeatDays: List<Boolean>?): String {
        if (repeatDays.isNullOrEmpty()) {
            return "-"
        }
        val stringBuilder = StringBuilder()
        var count = 0
        repeatDays.forEachIndexed { index, status ->
            if (index == 1 && status) {
                count += 1
                stringBuilder.append("Mon").append(", ")
            } else if (index == 2 && status) {
                count += 1
                stringBuilder.append("Tue").append(", ")
            } else if (index == 3 && status) {
                count += 1
                stringBuilder.append("Wed").append(", ")
            } else if (index == 4 && status) {
                count += 1
                stringBuilder.append("Thu").append(", ")
            } else if (index == 5 && status) {
                count += 1
                stringBuilder.append("Fri").append(", ")
            } else if (index == 6 && status) {
                count += 1
                stringBuilder.append("Sat").append(", ")
            } else if (index == 7 && status) {
                count += 1
                stringBuilder.append("Sun").append(", ")
            }
        }

        return when (count) {
            0 -> {
                return "-"
            }

            7 -> {
                stringBuilder.clear()
                stringBuilder.append("Every day").toString()
            }

            else -> {
                stringBuilder.deleteCharAt(stringBuilder.length - 2).toString()
            }
        }
    }

    fun getRepeatReminderDays(repeatDays: List<Boolean>?): String {
        if (repeatDays.isNullOrEmpty()) {
            return "-"
        }

        val stringBuilder = StringBuilder()
        var count = 0
        repeatDays.forEachIndexed { index, status ->
            if (index == 0 && status) {
                count += 1
                stringBuilder.append("Mon").append(", ")
            } else if (index == 1 && status) {
                count += 1
                stringBuilder.append("Tue").append(", ")
            } else if (index == 2 && status) {
                count += 1
                stringBuilder.append("Wed").append(", ")
            } else if (index == 3 && status) {
                count += 1
                stringBuilder.append("Thu").append(", ")
            } else if (index == 4 && status) {
                count += 1
                stringBuilder.append("Fri").append(", ")
            } else if (index == 5 && status) {
                count += 1
                stringBuilder.append("Sat").append(", ")
            } else if (index == 6 && status) {
                count += 1
                stringBuilder.append("Sun").append(", ")
            }
        }
        return when (count) {
            0 -> {
                return "-"
            }


            7 -> {
                stringBuilder.clear()
                stringBuilder.append("Every day").toString()
            }

            else -> {
                stringBuilder.deleteCharAt(stringBuilder.length - 2).toString()
            }
        }
    }

    fun getSleepType(type: String): SleepType {
        return when (type.lowercase().trim()) {
            "light" -> {
                SleepType.LIGHT
            }

            "deep" -> {
                SleepType.DEEP
            }

            "awake" -> {
                SleepType.AWAKE
            }

            "rem" -> {
                SleepType.REM
            }

            else -> {
                return SleepType.SOBER
            }
        }
    }

    fun getImageForTask(taskEnum: String?): Int {
        val imageType = when (taskEnum) {
            TaskEnums.WATCH_PAIR.type -> {
                R.drawable.ic_task_watch_paired
            }

            TaskEnums.PROFILE.type -> {
                R.drawable.ic_task_p_complete
            }

            TaskEnums.STEPS.type -> {
                R.drawable.ic_task_c_steps
            }

            TaskEnums.DISTANCE.type -> {
                R.drawable.ic_task_distance
            }

            TaskEnums.CALORIES.type -> {
                R.drawable.ic_task_calories
            }

            TaskEnums.CUSTOM_WATCHFACE.type -> {
                R.drawable.ic_task_wf_created
            }

            TaskEnums.CHALLENGE_PARTICIPATION.type -> {
                R.drawable.ic_task_c_joined
            }

            TaskEnums.FRIEND_ADDED.type -> {
                R.drawable.ic_task_add_friend
            }

            TaskEnums.WORKOUT.type -> {
                R.drawable.ic_task_w_complete
            }

            TaskEnums.SHARE.type -> {
                R.drawable.ic_task_w_share
            }

            else -> 0
        }
        return imageType
    }

    fun bmiCalculate(
        height: Float,
        weight: Float,
        unitTypeHeight: String,
        unitTypeWeight: String
    ): Pair<String, Long> {
        //1 kg=2.20 lbs
        //0.45 kg= 1 lbs
        //1 cm=0.39 inches
        //1 inch=0.02 meter
        //1 cm=0.01 meter

        val weightValue: Float
        val finalValue: Float

        val heightValue: Float = if (unitTypeHeight == HeightUnitSystem.METRIC.type) {
            height.div(100)
        } else {
            height.times(0.02).toFloat()
        }

        weightValue = if (unitTypeWeight == WeightUnitSystem.METRIC.type) {
            weight
        } else {
            weight.times(0.45).toFloat()//converted in kg
        }

        finalValue = weightValue.div(heightValue.times(heightValue))
        val bmiCategory: String
        val stepsValue: Long
        if (finalValue <= 18.5) {
            bmiCategory = "Underweight"
            stepsValue = 3000
        } else if (finalValue > 18.5 && finalValue <= 25) {
            bmiCategory = "Normal"
            stepsValue = 3000
        } else if (finalValue > 25 && finalValue <= 30) {
            bmiCategory = "Overweight"
            stepsValue = 5000
        } else {
            bmiCategory = "Obese"
            stepsValue = 10000
        }




        return Pair(
            "${finalValue.roundToNearestDecimalFlooor(finalValue)} (${bmiCategory})",
            stepsValue
        )
    }


    fun bmrCalculate(
        height: Float,
        weight: Float,
        unitTypeHeight: String,
        unitTypeWeight: String,
        age: Int,
        gender: Gender
    ): Int {
        val bmrValue: Double
        var defValue1: Float
        var defValue2: Float
        var defValue3: Float
        var defValue4: Float

        val heightValue: Float
        if (unitTypeHeight == HeightUnitSystem.METRIC.type) {
            heightValue = height
            if (gender == Gender.FEMALE) {
                defValue1 = 655.0955F
                defValue2 = 9.5634F
                defValue3 = 1.8496F
                defValue4 = 4.6756F
            } else {
                defValue1 = 66.473F
                defValue2 = 13.7516F
                defValue3 = 5.0033F
                defValue4 = 6.755F
            }
        } else {
            heightValue = height
            if (gender == Gender.FEMALE) {
                defValue1 = 65.5F
                defValue2 = 4.35F
                defValue3 = 4.7F
                defValue4 = 4.7F
            } else {
                defValue1 = 66F
                defValue2 = 6.2F
                defValue3 = 12.7F
                defValue4 = 6.76F
            }
        }

        val weightValue: Float
        if (unitTypeWeight == WeightUnitSystem.METRIC.type) {
            weightValue = weight
            if (gender == Gender.FEMALE) {
                defValue1 = 655.0955F
                defValue2 = 9.5634F
                defValue3 = 1.8496F
                defValue4 = 4.6756F
            } else {
                defValue1 = 66.473F
                defValue2 = 13.7516F
                defValue3 = 5.0033F
                defValue4 = 6.755F
            }
        } else {
            weightValue = weight
            if (gender == Gender.FEMALE) {
                defValue1 = 65.5F
                defValue2 = 4.35F
                defValue3 = 4.7F
                defValue4 = 4.7F
            } else {
                defValue1 = 66F
                defValue2 = 6.2F
                defValue3 = 12.7F
                defValue4 = 6.76F
            }
        }
        bmrValue = defValue1.plus(defValue2.times(weightValue)).plus(defValue3.times(heightValue))
            .minus(defValue4.times(age)).toDouble()
        return roundNearestValue(
            bmrValue.toFloat().roundToNearestDecimalFlooor(bmrValue.toFloat()).toDouble(), 10.0
        )


    }

    fun roundNearestValue(value: Double, nearestRoundUpValue: Double): Int {
        return (Math.round(value / 10.0) * nearestRoundUpValue).toInt()
    }

    fun formatTimeMinuteSec(value: Long): String {
        val minute = TimeUnit.SECONDS.toMinutes(value) - TimeUnit.SECONDS.toHours(value) * 60
        val second = TimeUnit.SECONDS.toSeconds(value) - TimeUnit.SECONDS.toMinutes(value) * 60
        val mntPfx: String = if (minute < 10)
            "0$minute"
        else
            minute.toString()
        val scdPfx: String = if (second < 10)
            "0$second"
        else
            second.toString()
        return "$mntPfx:$scdPfx"

    }

    fun calculateProgress(finalValue: Int, progressValue: Int): Int {
        val tempValue: Float = progressValue.toFloat() / finalValue
        return tempValue.times(100).toInt()

    }

    fun getActivityDurationFormat2(duration: Long?): String {
        if (duration == null) return ""
        val hrs = (duration / 3600)
        val mins = (duration % 3600 / 60)
        val secs = duration % 60

        // Output like "00:00:00"
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    fun calculateProgressPercentage(nplData: NplLeague): Int {
        val progress: Int = if (nplData.correctQues == 0) {
            0
        } else
            (nplData.correctQues.times(100)).div(nplData.totalQues)
        return progress
    }


}