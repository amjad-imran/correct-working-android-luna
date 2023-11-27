package com.noisefit.util


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.ImageView
import androidx.core.app.NotificationManagerCompat
import androidx.core.util.Preconditions.checkArgument
import androidx.work.*
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.BuildConfig
import com.noisefit.receiver.workManager.*
import com.noisefit.watch.WatchForm
import com.noisefit_commans.common.roundToNearestDecimalFlooor
import com.noisefit_commans.data.response.NplLeague
import com.noisefit_commans.models.*
import com.noisefit_commans.ui.loadCircleCacheWithProgress
import com.noisefit_commans.ui.loadCircleWCacheWithProgress
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.loadImageWCacheWithProgress
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.LOGS
import com.oreo.receiver.workManager.GoogleFitSyncWork
import com.oreo.receiver.workManager.OreoSyncDataWork
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor


private const val UniqueSyncDataWorkName: String = "SyncDataWork"
private const val UniqueRingSyncDataWorkName: String = "RingSyncDataWork"
private const val LOGS_SYNC_WORKER_NAME: String = "LOGS_SYNC_WORKER_NAME"
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

    /** value in seconds
     * Returns HH:MM:SS
     */
    fun getFormattedVideoDurationFromSeconds(value: Int): String {
        if (value == 0) {
            return "00:00"
        } else {
            val hours: Int = value / 3600
            val minutes: Int = (value % 3600) / 60
            val seconds = value % 60;

            if (hours != 0) {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun setRescueWorkManager(context: Context) {
        val request = OneTimeWorkRequestBuilder<RescueServiceInBgWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun stopWeatherScheduler(context: Context) {
        LOGS.d("$UniqueWeatherWorkName: inside stopWeatherScheduler ")
        WorkManager.getInstance(context).cancelUniqueWork(UniqueWeatherWorkName)
    }

    fun stopSportScheduler(context: Context) {
        LOGS.d("$UniqueSportSyncWorkName: inside stopSportScheduler ")
        WorkManager.getInstance(context).cancelUniqueWork(UniqueSportSyncWorkName)
    }


    fun clearJobs(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    suspend fun isOreoSyncDataWorkerRunning(context: Context): Boolean {
        val uniqueId = getUniqueRingSyncDataWorkName()
        return isWorkScheduled(uniqueId, context)
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

    suspend fun startOreoSyncScheduler(context: Context): Boolean {
        val uniqueId = getUniqueRingSyncDataWorkName()
        val isWorkScheduled = isWorkScheduled(uniqueId, context)
        LOGS.w("SyncDataWork: inside startOreoSyncScheduler $uniqueId isWorkScheduled $isWorkScheduled")
        if (!isWorkScheduled) {

            WorkManager.getInstance(context).cancelUniqueWork(uniqueId)
            val constraints = Constraints.Builder()
                .build()


            /*val work =
                PeriodicWorkRequest.Builder(OreoSyncDataWork::class.java, 60, TimeUnit.MINUTES)
                    .addTag(uniqueId)
                    .setConstraints(constraints)
                    .build()*/
            /*WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueId,
                ExistingPeriodicWorkPolicy.KEEP,
                work

            )*/

            val work =
                OneTimeWorkRequest.Builder(OreoSyncDataWork::class.java)
                    .addTag(uniqueId)
                    .setConstraints(constraints)
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


    fun startFeedbackSubmitWorker(context: Context): Boolean {

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest =
            OneTimeWorkRequest.Builder(FeedbackSubmitWorker::class.java)
                .addTag(LOGS_SYNC_WORKER_NAME)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            LOGS_SYNC_WORKER_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return true
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

        if (cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork) != null) {
            // connected to the internet
            status = true
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

        val heightValue: Float =
            if (unitTypeHeight.lowercase() == HeightUnitSystem.METRIC.type.lowercase()) {
                height.div(100)
            } else {
                height.times(0.02).toFloat()
            }

        weightValue = if (unitTypeWeight.lowercase() == WeightUnitSystem.METRIC.type.lowercase()) {
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
        if (unitTypeHeight.lowercase() == HeightUnitSystem.METRIC.type.lowercase()) {
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
        if (unitTypeWeight.lowercase() == WeightUnitSystem.METRIC.type.lowercase()) {
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
        bmrValue = (defValue1.plus(defValue2.times(weightValue)).plus(defValue3.times(heightValue))
            .minus(defValue4.times(age))).toDouble()
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

    /**
     * duration in minutes
     */
    fun getActivityDurationFormat2(duration: Long?): String {
        if (duration == null) return ""

        val (hour, minute) = getFormattedSleepDuration(
            (duration ?: 0L).toInt()
        )

        val secs = 0
        // Output like "00:00:00"
        return String.format("%02d:%02d:%02d", hour, minute, secs)
    }

    fun calculateProgressPercentage(nplData: NplLeague): Int {
        val progress: Int = if (nplData.correctQues == 0) {
            0
        } else
            (nplData.correctQues.times(100)).div(nplData.totalQues)
        return progress
    }


}