package com.oreo.ui.home.summary

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.noisefit.luna.R
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.AlertEvent
import com.noisefit_commans.models.DeviceAlertFeature
import com.noisefit_commans.models.HeartRateAlertSettings
import com.noisefit_commans.models.HighStressAlertSettings
import com.noisefit_commans.models.LocalDeviceAlertSettings
import com.noisefit_commans.models.PressureModeSettings
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SleepReminder
import com.noisefit_commans.models.Spo2AlertSettings
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.AlertDebugLogger
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit.watch.CommonGlobals
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BlankTestFragment : Fragment() {

    companion object {
        private const val DAILY_SYNC_MODE_TODAY = 1
        private const val DAILY_SYNC_MODE_HISTORY = 2
        private const val DAILY_SYNC_MODE_ALL = 3
        private const val RAW_UI_PREVIEW_MAX_CHARS = 300
    }

    @Inject
    lateinit var watchDataStore: WatchDataStore

    private val alertSettingsViewModel: AlertSettingsViewModel by viewModels()
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private var isBindingAlertState = false
    private var lastDailySyncStatus = "Auto sync on open uses the legacy default daily request."
    private var dashboardRefreshJob: Job? = null
    private var alertBannerHideJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_blank_test, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        triggerSync()
        setupBodyBatteryChart(view)
        bindDashboardCards(view)
        bindSdkSyncControls(view)
        bindRawPayloadViewer(view)
        initAlertSettings(view)
        alertSettingsViewModel.sessionManager.isRingCharging.observe(viewLifecycleOwner) { isCharging ->
            view.findViewById<TextView>(R.id.tvChargingStatus).apply {
                text = if (isCharging) "CHARGING" else "NOT CHARGING"
                setTextColor(if (isCharging) Color.parseColor("#4ADE80") else Color.parseColor("#888888"))
            }
        }
    }

    override fun onDestroyView() {
        dashboardRefreshJob?.cancel()
        dashboardRefreshJob = null
        alertBannerHideJob?.cancel()
        alertBannerHideJob = null
        super.onDestroyView()
    }

    private fun triggerSync() {
        try {
            val today = currentDate()
            lastDailySyncStatus = "Auto sync requested for $today using the legacy default mode."
            CommonGlobals.userActivityDataActions?.syncUserActivity(today, true)
            CommonGlobals.userActivityDataActions?.syncAutoSports()
        } catch (_: Exception) {}
    }

    private fun setupBodyBatteryChart(root: View) {
        root.findViewById<LineChart>(R.id.chartBodyBattery).apply {
            description.isEnabled = false
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            legend.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            setNoDataText("No body battery data yet")
            setNoDataTextColor(Color.parseColor("#555555"))
            setBackgroundColor(Color.parseColor("#111111"))
        }
    }

    private fun loadBodyBatteryChart(root: View) {
        val json = watchDataStore.testGetBodyBatteryData() ?: return
        try {
            val bean = Gson().fromJson(json, BodyBatteryWrapper::class.java) ?: return
            val dataPoints = bean.data ?: return
            if (dataPoints.isEmpty()) return

            val entries = ArrayList<Entry>()
            var validCount = 0
            dataPoints.forEachIndexed { index, value ->
                if (value > 0) {
                    entries.add(Entry(index.toFloat(), value.toFloat()))
                    validCount++
                }
            }
            if (entries.isEmpty()) return

            val currentStamina = entries.lastOrNull()?.y?.toInt() ?: 0
            val chart = root.findViewById<LineChart>(R.id.chartBodyBattery)
            val green = Color.parseColor("#4ADE80")
            val lineDataSet = LineDataSet(entries, "").apply {
                color = green
                lineWidth = 2f
                val singlePoint = validCount == 1
                setDrawCircles(singlePoint || validCount <= 5)
                circleRadius = 4f
                setCircleColor(green)
                setDrawCircleHole(false)
                setDrawValues(true)
                valueTextColor = green
                valueTextSize = 7f
                mode = if (singlePoint) LineDataSet.Mode.LINEAR else LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                highLightColor = Color.TRANSPARENT
            }
            chart.axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 105f
            }
            chart.xAxis.apply {
                spaceMin = 0.5f
                spaceMax = 0.5f
            }
            chart.data = LineData(lineDataSet)
            chart.invalidate()
            chart.notifyDataSetChanged()

            root.findViewById<TextView>(R.id.tvCurrentStamina).text =
                if (currentStamina > 0) "$currentStamina" else "--"
            root.findViewById<TextView>(R.id.tvBodyBatteryStatus).text =
                "$validCount readings / ${dataPoints.size} slots (every ${bean.bodyBatteryFrequency ?: 15} min)"
        } catch (_: Exception) {}
    }

    private data class BodyBatteryWrapper(
        val bodyBatteryFrequency: Int? = null,
        val data: List<Int>? = null
    )

    private data class RespiratoryWrapper(
        val data: List<Int>? = null,
        val date: String? = null
    )

    private fun bindSdkSyncControls(root: View) {
        updateSdkSyncStatus(root)
        root.findViewById<TextView>(R.id.tvSdkSyncDefault).setOnClickListener {
            requestDailySync(root, null, "Manual sync requested with legacy default mode.")
        }
        root.findViewById<TextView>(R.id.tvSdkSyncToday).setOnClickListener {
            requestDailySync(
                root,
                DAILY_SYNC_MODE_TODAY,
                "Manual sync requested with v2.3.1 today-only mode."
            )
        }
        root.findViewById<TextView>(R.id.tvSdkSyncHistory).setOnClickListener {
            requestDailySync(
                root,
                DAILY_SYNC_MODE_HISTORY,
                "Manual sync requested with v2.3.1 history mode."
            )
        }
        root.findViewById<TextView>(R.id.tvSdkSyncAll).setOnClickListener {
            requestDailySync(
                root,
                DAILY_SYNC_MODE_ALL,
                "Manual sync requested with v2.3.1 all-data mode."
            )
        }
    }

    private fun requestDailySync(root: View, mode: Int?, message: String) {
        lastDailySyncStatus = "$message Date ${currentDate()}."
        updateSdkSyncStatus(root)
        try {
            CommonGlobals.userActivityDataActions?.syncUserActivityByMode(currentDate(), mode)
            CommonGlobals.userActivityDataActions?.syncAutoSports()
            context.showShortToast("Daily sync request sent.")
        } catch (_: Exception) {
            lastDailySyncStatus = "Daily sync request failed to start."
            updateSdkSyncStatus(root)
            context.showShortToast("Unable to start daily sync.")
        }
    }

    private fun updateSdkSyncStatus(root: View) {
        root.findViewById<TextView>(R.id.tvSdkSyncStatus).text = lastDailySyncStatus
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun buildRawPreview(raw: String?): String {
        if (raw.isNullOrBlank()) {
            return "No payload captured yet.\n\nTap to open full raw snapshot."
        }
        val preview = raw.take(RAW_UI_PREVIEW_MAX_CHARS)
        return buildString {
            append("Captured | ")
            append(raw.length)
            append(" chars\n\n")
            append(preview)
            if (raw.length > preview.length) {
                append("...")
            }
            append("\n\nTap to open full raw snapshot.")
        }
    }

    private fun setTextIfChanged(textView: TextView, value: String) {
        if (textView.text.toString() != value) {
            textView.text = value
        }
    }

    private fun bindRawPayloadViewer(root: View) {
        bindRawPayloadOpener(
            root = root,
            textViewId = R.id.tvRawHeartRateV231,
            title = "CONTINUOUS HEART RATE RAW",
            payloadType = RawSdkPayloadBottomSheet.TYPE_CONTINUOUS_HEART_RATE
        )
        bindRawPayloadOpener(
            root = root,
            textViewId = R.id.tvRawPressureV231,
            title = "CONTINUOUS PRESSURE RAW",
            payloadType = RawSdkPayloadBottomSheet.TYPE_CONTINUOUS_PRESSURE
        )
        bindRawPayloadOpener(
            root = root,
            textViewId = R.id.tvRawSleepRri,
            title = "SLEEP RRI RAW",
            payloadType = RawSdkPayloadBottomSheet.TYPE_SLEEP_RRI
        )
        bindRawPayloadOpener(
            root = root,
            textViewId = R.id.tvRawSleepHrv,
            title = "SLEEP HRV RAW",
            payloadType = RawSdkPayloadBottomSheet.TYPE_SLEEP_HRV
        )
        bindRawPayloadOpener(
            root = root,
            textViewId = R.id.tvRawContinuousRri,
            title = "CONTINUOUS RRI RAW",
            payloadType = RawSdkPayloadBottomSheet.TYPE_CONTINUOUS_RRI
        )
        bindRawPayloadOpener(
            root = root,
            textViewId = R.id.tvRawSportHeartRateAfter,
            title = "POST-WORKOUT HEART RATE RAW",
            payloadType = RawSdkPayloadBottomSheet.TYPE_SPORT_HEART_RATE_AFTER
        )
        bindRawPayloadOpener(
            root = root,
            textViewId = R.id.tvRawDevSport,
            title = "DEV SPORT INFO (RAW SDK FIELDS)",
            payloadType = RawSdkPayloadBottomSheet.TYPE_DEV_SPORT
        )
    }

    private fun bindRawPayloadOpener(
        root: View,
        textViewId: Int,
        title: String,
        payloadType: String
    ) {
        root.findViewById<TextView>(textViewId).setOnClickListener {
            val tag = "raw_sdk_payload_$payloadType"
            if (childFragmentManager.findFragmentByTag(tag) == null) {
                RawSdkPayloadBottomSheet.newInstance(title, payloadType)
                    .show(childFragmentManager, tag)
            }
        }
    }

    private fun buildFrequencySummary(
        raw: String?,
        frequencyKey: String,
        primaryListKey: String,
        frequencyVersionKey: String = "frequencyVersion",
        secondaryListKey: String? = null
    ): String {
        if (raw.isNullOrBlank()) {
            return "No payload captured yet."
        }
        return try {
            val jsonObject = JsonParser.parseString(raw).asJsonObject
            val date = jsonObject.optString("date").ifBlank { "date unavailable" }
            val primaryCount = jsonObject.optArraySize(primaryListKey)
            val secondaryCount = secondaryListKey?.let { jsonObject.optArraySize(it) }
            val frequency = jsonObject.optInt(frequencyKey)
            val frequencyVersion = jsonObject.optInt(frequencyVersionKey)
            buildString {
                append(date)
                append(" | ")
                append(primaryListKey)
                append(" ")
                append(primaryCount)
                if (secondaryCount != null) {
                    append(" | ")
                    append(secondaryListKey)
                    append(" ")
                    append(secondaryCount)
                }
                if (frequency != null) {
                    append(" | every ")
                    append(frequency)
                    append(" ")
                    append(
                        when (frequencyVersion) {
                            1 -> "seconds"
                            0 -> "minutes"
                            else -> "units"
                        }
                    )
                }
            }
        } catch (_: Exception) {
            "Payload received but summary parsing failed."
        }
    }

    private fun buildPayloadSummary(raw: String?, primaryListKey: String): String {
        if (raw.isNullOrBlank()) {
            return "No payload captured yet."
        }
        return try {
            val jsonObject = JsonParser.parseString(raw).asJsonObject
            val date = jsonObject.optString("date").ifBlank { "date unavailable" }
            val count = jsonObject.optArraySize(primaryListKey)
            "$date | $primaryListKey count $count"
        } catch (_: Exception) {
            "Payload received but summary parsing failed."
        }
    }

    private fun JsonObject.optArraySize(key: String): Int {
        if (!has(key) || get(key).isJsonNull || !get(key).isJsonArray) {
            return 0
        }
        return getAsJsonArray(key).size()
    }

    private fun JsonObject.optInt(key: String): Int? {
        if (!has(key) || get(key).isJsonNull) {
            return null
        }
        return runCatching { get(key).asInt }.getOrNull()
    }

    private fun JsonObject.optString(key: String): String {
        if (!has(key) || get(key).isJsonNull) {
            return ""
        }
        return runCatching { get(key).asString }.getOrDefault("")
    }

    private fun bindDashboardCards(root: View) {
        dashboardRefreshJob?.cancel()
        dashboardRefreshJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                val batteryPercent = watchDataStore.getBatteryPercentRing()
                val remainingTime = watchDataStore.getBatteryRemainingTime() // unit: 0.5 days
                val fullyChargedTime = watchDataStore.getBatteryFullyChargeTime() // unit: minutes
                val fitnessAge = watchDataStore.getFitnessAge()
                val workout = watchDataStore.getWorkout()
                val respJson = watchDataStore.testGetRawRespiratoryJson()
                val respRate = if (respJson != null) {
                    try {
                        val wrapper = Gson().fromJson(respJson, RespiratoryWrapper::class.java)
                        wrapper?.data?.lastOrNull { it > 0 } ?: 0
                    } catch (_: Exception) {
                        0
                    }
                } else {
                    0
                }
                val heartRateRaw = watchDataStore.testGetRawContinuousHeartRateJson()
                val pressureRaw = watchDataStore.testGetRawContinuousPressureJson()
                val sleepRriRaw = watchDataStore.testGetRawSleepRriJson()
                val sleepHrvRaw = watchDataStore.testGetRawSleepHrvJson()
                val continuousRriRaw = watchDataStore.testGetRawContinuousRriJson()
                val sportHeartRateAfterRaw = watchDataStore.testGetRawSportHeartRateAfterJson()
                val devSportRaw = watchDataStore.testGetRawDevSportJson()
                val heartRateMeta = buildFrequencySummary(
                    raw = heartRateRaw,
                    frequencyKey = "continuousHeartRateFrequency",
                    primaryListKey = "heartRateData"
                )
                val pressureMeta = buildFrequencySummary(
                    raw = pressureRaw,
                    frequencyKey = "pressureFrequency",
                    primaryListKey = "pressureData",
                    secondaryListKey = "rriData"
                )
                val continuousRriMeta = buildFrequencySummary(
                    raw = continuousRriRaw,
                    frequencyKey = "frequency",
                    primaryListKey = "rri"
                )
                val sleepRriMeta = buildPayloadSummary(
                    raw = sleepRriRaw,
                    primaryListKey = "rri"
                )
                val sleepHrvMeta = buildPayloadSummary(
                    raw = sleepHrvRaw,
                    primaryListKey = "hrv"
                )
                val sportHeartRateAfterMeta = buildPayloadSummary(
                    raw = sportHeartRateAfterRaw,
                    primaryListKey = "hrList"
                )
                val previewHeartRateRaw = buildRawPreview(heartRateRaw)
                val previewPressureRaw = buildRawPreview(pressureRaw)
                val previewSleepRriRaw = buildRawPreview(sleepRriRaw)
                val previewSleepHrvRaw = buildRawPreview(sleepHrvRaw)
                val previewContinuousRriRaw = buildRawPreview(continuousRriRaw)
                val previewSportHeartRateAfterRaw = buildRawPreview(sportHeartRateAfterRaw)
                val previewDevSportRaw = buildRawPreview(devSportRaw)

                withContext(Dispatchers.Main) {
                    if (!isAdded || view == null) {
                        return@withContext
                    }
                    root.findViewById<TextView>(R.id.tvRemainingBatteryTime).text =
                        "$batteryPercent%"
                    root.findViewById<ProgressBar>(R.id.batteryProgressBar).progress =
                        batteryPercent.coerceIn(0, 100)
                    root.findViewById<TextView>(R.id.tvSleepDuration).text =
                        if (remainingTime > 0) "${remainingTime * 0.5f} days" else "--"
                    root.findViewById<TextView>(R.id.tvFullyChargedTime).text =
                        if (fullyChargedTime > 0) "$fullyChargedTime min" else "--"

                    root.findViewById<TextView>(R.id.tvExtra2).text =
                        if (fitnessAge > 0) "$fitnessAge" else "--"

                    fun tv(id: Int) = root.findViewById<TextView>(id)
                    fun Float?.nz() = if (this != null && this > 0f) this.toString() else "--"
                    fun Int?.nz() = if (this != null && this > 0) this.toString() else "--"
                    fun Long?.nz() = if (this != null && this > 0L) this.toString() else "--"

                    tv(R.id.tvWorkoutId).text = workout?.id?.toString() ?: "--"
                    tv(R.id.tvWorkoutIsSynced).text = workout?.isSynced?.toString() ?: "--"
                    tv(R.id.tvWorkoutIsAccepted).text = workout?.isAccepted?.toString() ?: "--"
                    tv(R.id.tvWorkoutDuration).text = workout?.duration?.toString() ?: "--"
                    tv(R.id.tvWorkoutDurationSeconds).text =
                        workout?.durationSeconds?.toString() ?: "--"
                    tv(R.id.tvWorkoutType).text = workout?.type?.toString() ?: "--"
                    tv(R.id.tvWorkoutType2).text = workout?.type?.toString() ?: "--"
                    tv(R.id.tvWorkoutIntensity).text = workout?.intensity?.toString() ?: "--"
                    tv(R.id.tvWorkoutCalories).text = workout?.calories?.toString() ?: "--"
                    tv(R.id.tvWorkoutSteps).text = workout?.steps?.toString() ?: "--"
                    tv(R.id.tvWorkoutStartTime).text =
                        workout?.startTime?.let { timeFormat.format(Date(it)) } ?: "--"
                    tv(R.id.tvWorkoutEndTime).text =
                        workout?.endTime?.let { timeFormat.format(Date(it)) } ?: "--"
                    tv(R.id.tvWorkoutCadence).text = workout?.cadence?.toString() ?: "--"
                    tv(R.id.tvWorkoutDistance).text = workout?.distance?.toString() ?: "--"
                    tv(R.id.tvWorkoutRecovery).text = workout?.recoveryTime?.toString() ?: "--"
                    tv(R.id.tvWorkoutFitnessAge).text = workout?.fitnessAge.nz()
                    tv(R.id.tvWorkoutEnergyConsumption).text = workout?.energyConsumption.nz()
                    tv(R.id.tvWorkoutDate).text = workout?.date ?: "--"
                    tv(R.id.tvWorkoutHrData).text = workout?.hrData ?: "--"
                    tv(R.id.tvWorkoutIntensityList).text = workout?.intensityList ?: "--"

                    tv(R.id.tvWorkoutVo2Max).text = workout?.vo2Max.nz()
                    tv(R.id.tvWorkoutTrainingEffect).text = workout?.trainingEffect.nz()
                    tv(R.id.tvWorkoutTrainingLoad).text = workout?.trainingLoad.nz()
                    tv(R.id.tvWorkoutAvgHeart).text = workout?.avgHeart.nz()
                    tv(R.id.tvWorkoutMaxHeart).text = workout?.maxHeart.nz()
                    tv(R.id.tvWorkoutMinHeart).text = workout?.minHeart.nz()
                    tv(R.id.tvWorkoutAvgPace).text = workout?.avgPace.nz()
                    tv(R.id.tvWorkoutFastPace).text = workout?.fastPace.nz()
                    tv(R.id.tvWorkoutAvgSpeed).text = workout?.avgSpeed.nz()
                    tv(R.id.tvWorkoutFastSpeed).text = workout?.fastSpeed.nz()
                    tv(R.id.tvWorkoutHrLimit).text = workout?.hrLimitTime.nz()
                    tv(R.id.tvWorkoutHrAnaerobic).text = workout?.hrAnaerobic.nz()
                    tv(R.id.tvWorkoutHrAerobic).text = workout?.hrAerobic.nz()
                    tv(R.id.tvWorkoutHrFatBurn).text = workout?.hrFatBurning.nz()
                    tv(R.id.tvWorkoutHrWarmUp).text = workout?.hrWarmUp.nz()
                    tv(R.id.tvWorkoutAvgStride).text = workout?.avgStride.nz()
                    tv(R.id.tvWorkoutMaxStride).text = workout?.maxStride.nz()
                    tv(R.id.tvWorkoutMinStride).text = workout?.minStride.nz()
                    tv(R.id.tvWorkoutCumRise).text = workout?.cumulativeRise.nz()
                    tv(R.id.tvWorkoutCumDecline).text = workout?.cumulativeDecline.nz()
                    tv(R.id.tvWorkoutAvgHeight).text = workout?.avgHeight.nz()

                    // Body battery chart
                    loadBodyBatteryChart(root)
                    updateSdkSyncStatus(root)

                    // Respiratory rate — latest non-zero reading from today's 5-min interval data
                    tv(R.id.tvRespRate).text = if (respRate > 0) "$respRate" else "--"


                    setTextIfChanged(tv(R.id.tvSdkHeartRateMeta), heartRateMeta)
                    setTextIfChanged(tv(R.id.tvSdkPressureMeta), pressureMeta)
                    setTextIfChanged(tv(R.id.tvSdkContinuousRriMeta), continuousRriMeta)
                    setTextIfChanged(tv(R.id.tvSdkSleepRriMeta), sleepRriMeta)
                    setTextIfChanged(tv(R.id.tvSdkSleepHrvMeta), sleepHrvMeta)
                    setTextIfChanged(tv(R.id.tvSdkSportHeartAfterMeta), sportHeartRateAfterMeta)
                    setTextIfChanged(tv(R.id.tvRawHeartRateV231), previewHeartRateRaw)
                    setTextIfChanged(tv(R.id.tvRawPressureV231), previewPressureRaw)
                    setTextIfChanged(tv(R.id.tvRawSleepRri), previewSleepRriRaw)
                    setTextIfChanged(tv(R.id.tvRawSleepHrv), previewSleepHrvRaw)
                    setTextIfChanged(tv(R.id.tvRawContinuousRri), previewContinuousRriRaw)
                    setTextIfChanged(tv(R.id.tvRawSportHeartRateAfter), previewSportHeartRateAfterRaw)

                    // Dev sport info — pretty-printed JSON
                    setTextIfChanged(tv(R.id.tvRawDevSport), previewDevSportRaw)
                }

                delay(2000L)
            }
        }
    }

    private fun initAlertSettings(root: View) {
        AlertDebugLogger.log("AlertUI", "BlankTestFragment alert panel initialized")
        root.findViewById<TextView>(R.id.tvExportAlertLogs).setOnClickListener {
            exportAlertLogs()
        }
        root.findViewById<TextView>(R.id.tvRefreshWearDetection).setOnClickListener {
            alertSettingsViewModel.refreshWearDetectionStatus()
        }
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchRestingHeartRate)
            .setOnCheckedChangeListener { _, _ ->
                if (!isBindingAlertState) {
                    commitHeartRateSettings(root)
                }
            }
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchWorkoutHeartRate)
            .setOnCheckedChangeListener { _, _ ->
                if (!isBindingAlertState) {
                    commitHeartRateSettings(root)
                }
            }
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchLowHeartRate)
            .setOnCheckedChangeListener { _, _ ->
                if (!isBindingAlertState) {
                    commitHeartRateSettings(root)
                }
            }
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSpo2Alert)
            .setOnCheckedChangeListener { _, _ ->
                if (!isBindingAlertState) {
                    commitSpo2Settings(root)
                }
            }
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchStressAlert)
            .setOnCheckedChangeListener { _, _ ->
                if (!isBindingAlertState) {
                    commitHighStressSettings(root)
                }
            }
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchBedtimeReminder)
            .setOnCheckedChangeListener { _, _ ->
                if (!isBindingAlertState) {
                    commitSleepReminder(root)
                }
            }
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchRelaxationPrompt)
            .setOnCheckedChangeListener { _, _ ->
                if (!isBindingAlertState) {
                    commitRelaxationPrompt(root)
                }
            }
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSedentaryReminder)
            .setOnCheckedChangeListener { _, _ ->
                if (!isBindingAlertState) {
                    commitSedentaryReminder(root)
                }
            }

        setNumberCommitListeners(root.findViewById(R.id.etRestingHeartRate)) {
            commitHeartRateSettings(root)
        }
        setNumberCommitListeners(root.findViewById(R.id.etWorkoutHeartRate)) {
            commitHeartRateSettings(root)
        }
        setNumberCommitListeners(root.findViewById(R.id.etLowHeartRate)) {
            commitHeartRateSettings(root)
        }
        setNumberCommitListeners(root.findViewById(R.id.etSpo2Alert)) {
            commitSpo2Settings(root)
        }
        setNumberCommitListeners(root.findViewById(R.id.etStressAlert)) {
            commitHighStressSettings(root)
        }
        setNumberCommitListeners(root.findViewById(R.id.etSedentaryInterval)) {
            commitSedentaryReminder(root)
        }

        root.findViewById<TextView>(R.id.tvBedtimeReminderTime).setOnClickListener {
            showTimePicker(
                initialTime = root.findViewById<TextView>(R.id.tvBedtimeReminderTime).text.toString()
            ) { formattedTime ->
                root.findViewById<TextView>(R.id.tvBedtimeReminderTime).text = formattedTime
                commitSleepReminder(root)
            }
        }
        root.findViewById<TextView>(R.id.tvSedentaryStartTime).setOnClickListener {
            showTimePicker(
                initialTime = root.findViewById<TextView>(R.id.tvSedentaryStartTime).text.toString()
            ) { formattedTime ->
                root.findViewById<TextView>(R.id.tvSedentaryStartTime).text = formattedTime
                commitSedentaryReminder(root)
            }
        }
        root.findViewById<TextView>(R.id.tvSedentaryEndTime).setOnClickListener {
            showTimePicker(
                initialTime = root.findViewById<TextView>(R.id.tvSedentaryEndTime).text.toString()
            ) { formattedTime ->
                root.findViewById<TextView>(R.id.tvSedentaryEndTime).text = formattedTime
                commitSedentaryReminder(root)
            }
        }

        alertSettingsViewModel.alertSettings.observe(viewLifecycleOwner) { state ->
            bindAlertState(root, state)
        }
        alertSettingsViewModel.getMessages().observe(viewLifecycleOwner) { event ->
            event.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        alertSettingsViewModel.sessionManager.alertMirrorEvent.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let { alertEvent ->
                showAlertBanner(root, alertEvent)
                alertSettingsViewModel.loadState()
            }
        }
        alertSettingsViewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            alertSettingsViewModel.handleQueryCallback(it)
        }
        alertSettingsViewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let { callback ->
                alertSettingsViewModel.handleUpdateCallback(callback)
            }
        }
        alertSettingsViewModel.sessionManager.connectStateRing.observe(viewLifecycleOwner) { state ->
            updateConnectionStatus(root, state)
            alertSettingsViewModel.onConnectedStateChanged(state)
        }
        alertSettingsViewModel.loadState()
    }

    private fun bindAlertState(root: View, state: LocalDeviceAlertSettings) {
        isBindingAlertState = true

        bindEditTextValue(root.findViewById(R.id.etRestingHeartRate), state.heartRate.restingThreshold)
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchRestingHeartRate).isChecked =
            state.heartRate.restingEnabled

        bindEditTextValue(root.findViewById(R.id.etWorkoutHeartRate), state.heartRate.workoutThreshold)
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchWorkoutHeartRate).isChecked =
            state.heartRate.workoutEnabled

        bindEditTextValue(root.findViewById(R.id.etLowHeartRate), state.heartRate.lowThreshold)
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchLowHeartRate).isChecked =
            state.heartRate.lowEnabled

        bindEditTextValue(root.findViewById(R.id.etSpo2Alert), state.spo2.threshold)
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSpo2Alert).isChecked =
            state.spo2.enabled

        bindEditTextValue(root.findViewById(R.id.etStressAlert), state.highStress.threshold)
        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchStressAlert).isChecked =
            state.highStress.enabled

        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchBedtimeReminder).isChecked =
            state.sleepReminder.status
        root.findViewById<TextView>(R.id.tvBedtimeReminderTime).text =
            formatTime(state.sleepReminder.hour, state.sleepReminder.minute)

        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchRelaxationPrompt).isChecked =
            state.pressureMode.relaxationPromptEnabled

        root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSedentaryReminder).isChecked =
            state.sedentaryReminder.status
        root.findViewById<TextView>(R.id.tvSedentaryStartTime).text =
            formatTime(state.sedentaryReminder.startHour, state.sedentaryReminder.startMinute)
        root.findViewById<TextView>(R.id.tvSedentaryEndTime).text =
            formatTime(state.sedentaryReminder.endHour, state.sedentaryReminder.endMinute)
        bindEditTextValue(root.findViewById(R.id.etSedentaryInterval), state.sedentaryReminder.interval)
        bindWearDetectionStatus(root, state)
        bindRecentAlerts(root, state)

        applyFeatureSupport(
            root = root,
            feature = DeviceAlertFeature.HEART_RATE,
            supported = state.isSupported(DeviceAlertFeature.HEART_RATE),
            root.findViewById(R.id.etRestingHeartRate),
            root.findViewById(R.id.switchRestingHeartRate),
            root.findViewById(R.id.etLowHeartRate),
            root.findViewById(R.id.switchLowHeartRate)
        )
        applyWorkoutHeartRateSupport(
            root = root,
            supported = state.isSupported(DeviceAlertFeature.HEART_RATE) && state.support.heartRateWorkout
        )
        applyFeatureSupport(
            root = root,
            feature = DeviceAlertFeature.SPO2,
            supported = state.isSupported(DeviceAlertFeature.SPO2),
            root.findViewById(R.id.etSpo2Alert),
            root.findViewById(R.id.switchSpo2Alert)
        )
        applyFeatureSupport(
            root = root,
            feature = DeviceAlertFeature.HIGH_STRESS_INDEX,
            supported = state.isSupported(DeviceAlertFeature.HIGH_STRESS_INDEX),
            root.findViewById(R.id.etStressAlert),
            root.findViewById(R.id.switchStressAlert)
        )
        applyFeatureSupport(
            root = root,
            feature = DeviceAlertFeature.SLEEP_REMINDER,
            supported = state.isSupported(DeviceAlertFeature.SLEEP_REMINDER),
            root.findViewById(R.id.tvBedtimeReminderTime),
            root.findViewById(R.id.switchBedtimeReminder)
        )
        applyFeatureSupport(
            root = root,
            feature = DeviceAlertFeature.RELAXATION_PROMPT,
            supported = state.isSupported(DeviceAlertFeature.RELAXATION_PROMPT),
            root.findViewById(R.id.switchRelaxationPrompt)
        )
        applyFeatureSupport(
            root = root,
            feature = DeviceAlertFeature.SEDENTARY_REMINDER,
            supported = state.isSupported(DeviceAlertFeature.SEDENTARY_REMINDER),
            root.findViewById(R.id.tvSedentaryStartTime),
            root.findViewById(R.id.tvSedentaryEndTime),
            root.findViewById(R.id.etSedentaryInterval),
            root.findViewById(R.id.switchSedentaryReminder)
        )
        applyFeatureSupport(
            root = root,
            feature = DeviceAlertFeature.WEAR_DETECTION,
            supported = state.isSupported(DeviceAlertFeature.WEAR_DETECTION),
            root.findViewById(R.id.tvRefreshWearDetection)
        )

        isBindingAlertState = false
    }

    private fun commitHeartRateSettings(root: View) {
        val currentAlertState = alertSettingsViewModel.alertSettings.value
        val currentState = currentAlertState?.heartRate ?: HeartRateAlertSettings()
        val workoutSupported = currentAlertState?.support?.heartRateWorkout == true
        val restingEnabled =
            root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchRestingHeartRate).isChecked
        val workoutEnabled = workoutSupported &&
            root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchWorkoutHeartRate).isChecked
        val lowEnabled =
            root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchLowHeartRate).isChecked

        val restingThreshold = resolveNumberInput(
            editText = root.findViewById(R.id.etRestingHeartRate),
            enabled = restingEnabled,
            fallback = currentState.restingThreshold
        )
        val workoutThreshold = resolveNumberInput(
            editText = root.findViewById(R.id.etWorkoutHeartRate),
            enabled = workoutSupported && workoutEnabled,
            fallback = currentState.workoutThreshold
        )
        val lowThreshold = resolveNumberInput(
            editText = root.findViewById(R.id.etLowHeartRate),
            enabled = lowEnabled,
            fallback = currentState.lowThreshold
        )

        if ((restingEnabled && restingThreshold == null) ||
            (workoutEnabled && workoutThreshold == null) ||
            (lowEnabled && lowThreshold == null)
        ) {
            AlertDebugLogger.log(
                "AlertUI",
                "commitHeartRateSettings invalidInput restingEnabled=$restingEnabled resting=$restingThreshold workoutEnabled=$workoutEnabled workout=$workoutThreshold lowEnabled=$lowEnabled low=$lowThreshold"
            )
            context.showShortToast("Enter valid values for enabled heart-rate alerts.")
            return
        }

        alertSettingsViewModel.saveHeartRateSettings(
            HeartRateAlertSettings(
                restingEnabled = restingEnabled,
                restingThreshold = restingThreshold ?: currentState.restingThreshold,
                workoutEnabled = workoutEnabled,
                workoutThreshold = workoutThreshold ?: currentState.workoutThreshold,
                lowEnabled = lowEnabled,
                lowThreshold = lowThreshold ?: currentState.lowThreshold
            )
        )
    }

    private fun commitSpo2Settings(root: View) {
        val currentState = alertSettingsViewModel.alertSettings.value?.spo2 ?: Spo2AlertSettings()
        val threshold = resolveNumberInput(
            editText = root.findViewById(R.id.etSpo2Alert),
            enabled = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSpo2Alert).isChecked,
            fallback = currentState.threshold
        )
        if (root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSpo2Alert).isChecked && threshold == null) {
            AlertDebugLogger.log("AlertUI", "commitSpo2Settings invalidInput threshold=$threshold")
            context.showShortToast("Enter a valid SpO2 threshold.")
            return
        }

        alertSettingsViewModel.saveSpo2Settings(
            Spo2AlertSettings(
                enabled = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSpo2Alert).isChecked,
                threshold = threshold ?: currentState.threshold
            )
        )
    }

    private fun commitHighStressSettings(root: View) {
        val currentState = alertSettingsViewModel.alertSettings.value?.highStress ?: HighStressAlertSettings()
        val enabled =
            root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchStressAlert).isChecked
        val threshold = resolveNumberInput(
            editText = root.findViewById(R.id.etStressAlert),
            enabled = enabled,
            fallback = currentState.threshold
        )
        if (enabled && threshold == null) {
            AlertDebugLogger.log("AlertUI", "commitHighStressSettings invalidInput threshold=$threshold")
            context.showShortToast("Enter a valid stress threshold.")
            return
        }

        alertSettingsViewModel.saveHighStressSettings(
            HighStressAlertSettings(
                enabled = enabled,
                threshold = threshold ?: currentState.threshold
            )
        )
    }

    private fun commitSleepReminder(root: View) {
        val bedtime =
            parseTime(root.findViewById<TextView>(R.id.tvBedtimeReminderTime).text.toString()) ?: return
        alertSettingsViewModel.saveSleepReminder(
            SleepReminder(
                status = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchBedtimeReminder).isChecked,
                hour = bedtime.get(Calendar.HOUR_OF_DAY),
                minute = bedtime.get(Calendar.MINUTE),
                second = 0,
                millisecond = 0
            )
        )
    }

    private fun commitRelaxationPrompt(root: View) {
        val currentState = alertSettingsViewModel.alertSettings.value ?: return
        alertSettingsViewModel.savePressureModeSettings(
            PressureModeSettings(
                stressMonitoringEnabled = currentState.pressureMode.stressMonitoringEnabled,
                relaxationPromptEnabled = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchRelaxationPrompt).isChecked
            )
        )
    }

    private fun commitSedentaryReminder(root: View) {
        val currentState = alertSettingsViewModel.alertSettings.value?.sedentaryReminder
            ?: SedentaryData(interval = 60, startHour = 9, endHour = 18)
        val interval = resolveNumberInput(
            editText = root.findViewById(R.id.etSedentaryInterval),
            enabled = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSedentaryReminder).isChecked,
            fallback = currentState.interval
        )
        val start = parseTime(root.findViewById<TextView>(R.id.tvSedentaryStartTime).text.toString())
        val end = parseTime(root.findViewById<TextView>(R.id.tvSedentaryEndTime).text.toString())
        if (start == null || end == null || (root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSedentaryReminder).isChecked && interval == null)) {
            AlertDebugLogger.log(
                "AlertUI",
                "commitSedentaryReminder invalidInput interval=$interval start=${start != null} end=${end != null}"
            )
            context.showShortToast("Enter a valid sedentary schedule.")
            return
        }
        alertSettingsViewModel.saveSedentaryReminder(
            SedentaryData(
                status = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSedentaryReminder).isChecked,
                interval = interval ?: currentState.interval,
                startHour = start.get(Calendar.HOUR_OF_DAY),
                startMinute = start.get(Calendar.MINUTE),
                endHour = end.get(Calendar.HOUR_OF_DAY),
                endMinute = end.get(Calendar.MINUTE)
            )
        )
    }

    private fun updateConnectionStatus(root: View, connectState: ConnectState?) {
        val connected = connectState is ConnectState.ConnectSuccess
        root.findViewById<TextView>(R.id.tvStatus).text = when (connectState) {
            is ConnectState.ConnectSuccess -> {
                "Device connected. Supported alert changes sync and verify immediately."
            }

            else -> {
                "Device disconnected. Changes stay local and sync after reconnect."
            }
        }
        root.findViewById<TextView>(R.id.tvRefreshWearDetection).apply {
            isEnabled = connected
            alpha = if (connected) 1f else 0.45f
        }
    }

    private fun applyFeatureSupport(
        root: View,
        feature: DeviceAlertFeature,
        supported: Boolean,
        vararg views: View
    ) {
        views.forEach { view ->
            view.isEnabled = supported
            view.alpha = if (supported) 1f else 0.45f
        }
        when (feature) {
            DeviceAlertFeature.HEART_RATE -> {
                root.findViewById<View>(R.id.layoutStressAlert).isVisible = true
            }

            DeviceAlertFeature.HIGH_STRESS_INDEX -> {
                root.findViewById<TextView>(R.id.tvStressAlertNote).text = if (supported) {
                    "Uses the screenless HRV/stress warning flow on this device."
                } else {
                    "This device did not confirm screenless HRV/stress warning support."
                }
            }

            DeviceAlertFeature.SPO2,
            DeviceAlertFeature.RELAXATION_PROMPT,
            DeviceAlertFeature.SLEEP_REMINDER,
            DeviceAlertFeature.SEDENTARY_REMINDER,
            DeviceAlertFeature.WEAR_DETECTION -> Unit
        }
    }

    private fun applyWorkoutHeartRateSupport(root: View, supported: Boolean) {
        val note = root.findViewById<TextView>(R.id.tvWorkoutHeartRateSupportNote)
        val row = root.findViewById<View>(R.id.layoutWorkoutHeartRateRow)
        val input = root.findViewById<AppCompatEditText>(R.id.etWorkoutHeartRate)
        val toggle = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchWorkoutHeartRate)
        row.alpha = if (supported) 1f else 0.45f
        input.isEnabled = supported
        toggle.isEnabled = supported
        note.isVisible = !supported
        if (!supported) {
            toggle.isChecked = false
        }
    }

    private fun bindWearDetectionStatus(root: View, state: LocalDeviceAlertSettings) {
        val wearStatus = root.findViewById<TextView>(R.id.tvWearDetectionStatus)
        val wearMeta = root.findViewById<TextView>(R.id.tvWearDetectionMeta)
        wearStatus.text = when {
            state.wearDetectionStatus.lastUpdatedAt < 0L -> {
                "Wear status unavailable on this device."
            }

            state.wearDetectionStatus.isWorn == true -> {
                "Currently worn"
            }

            state.wearDetectionStatus.isWorn == false -> {
                "Currently not worn"
            }

            else -> {
                "Tap refresh to query wear status."
            }
        }
        wearMeta.text = formatWearDetectionMeta(state)
        wearMeta.isVisible = wearMeta.text.isNotBlank()
    }

    private fun bindRecentAlerts(root: View, state: LocalDeviceAlertSettings) {
        val container = root.findViewById<ViewGroup>(R.id.llRecentAlertsList)
        val emptyView = root.findViewById<TextView>(R.id.tvRecentAlertsEmpty)
        container.removeAllViews()
        val recentAlerts = state.recentAlerts.take(5)
        emptyView.isVisible = recentAlerts.isEmpty()
        container.isVisible = recentAlerts.isNotEmpty()
        recentAlerts.forEach { event ->
            container.addView(createRecentAlertView(event))
        }
    }

    private fun createRecentAlertView(event: AlertEvent): View {
        val item = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.back_modal_new_black)
            val padding = resources.displayMetrics.density.times(12).toInt()
            setPadding(padding, padding, padding, padding)
            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = resources.displayMetrics.density.times(8).toInt()
            layoutParams = params
        }
        val titleView = TextView(requireContext()).apply {
            setTextColor(Color.WHITE)
            textSize = 13f
            text = "${event.title} • ${formatAlertEventTime(event.timestamp)}"
        }
        val detailView = TextView(requireContext()).apply {
            setTextColor(Color.parseColor("#9BA3AF"))
            textSize = 12f
            text = buildString {
                append(event.message)
                if (event.observedValue != null || event.threshold != null) {
                    append("  ")
                    append("Value ")
                    append(event.observedValue ?: "--")
                    append(" / Threshold ")
                    append(event.threshold ?: "--")
                }
                append("  ")
                append("Source ")
                append(event.source.name.lowercase(Locale.US))
                append("  ")
                append("Band ")
                append(event.bandSendState)
            }
        }
        item.addView(titleView)
        item.addView(detailView)
        return item
    }

    private fun showAlertBanner(root: View, event: AlertEvent) {
        val banner = root.findViewById<TextView>(R.id.tvAlertBanner)
        val bannerMessage = "${event.title}: ${event.message}"
        banner.tag = event.id
        banner.text = bannerMessage
        banner.isVisible = true
        alertBannerHideJob?.cancel()
        alertBannerHideJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(5000L)
            if (banner.tag == event.id && view != null) {
                banner.isVisible = false
            }
        }
    }

    private fun formatAlertEventTime(timeStamp: Long): String {
        if (timeStamp <= 0L) {
            return "--"
        }
        return timeFormat.format(Date(timeStamp))
    }

    private fun formatWearDetectionMeta(state: LocalDeviceAlertSettings): String {
        val wearStatus = state.wearDetectionStatus
        if (wearStatus.lastUpdatedAt < 0L) {
            return ""
        }
        val parts = arrayListOf<String>()
        wearStatus.source?.takeIf { it.isNotBlank() }?.let { source ->
            parts.add("Source ${source.replace('_', ' ')}")
        }
        if (wearStatus.lastUpdatedAt > 0L) {
            parts.add("Updated ${formatAlertEventTime(wearStatus.lastUpdatedAt)}")
        }
        wearStatus.observedValue?.let { observedValue ->
            parts.add("Value $observedValue")
        }
        return parts.joinToString("  ")
    }

    private fun setNumberCommitListeners(editText: AppCompatEditText, onCommit: () -> Unit) {
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onCommit()
                true
            } else {
                false
            }
        }
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !isBindingAlertState) {
                onCommit()
            }
        }
    }

    private fun resolveNumberInput(editText: AppCompatEditText, enabled: Boolean, fallback: Int): Int? {
        val value = editText.text?.toString()?.trim()
        return when {
            value.isNullOrEmpty() && !enabled -> fallback
            value.isNullOrEmpty() -> null
            else -> value.toIntOrNull()
        }
    }

    private fun bindEditTextValue(editText: AppCompatEditText, value: Int) {
        if (editText.hasFocus()) {
            return
        }
        val nextValue = value.toString()
        if (editText.text?.toString() != nextValue) {
            editText.setText(nextValue)
        }
    }

    private fun showTimePicker(initialTime: String, onTimeSelected: (String) -> Unit) {
        val calendar = parseTime(initialTime) ?: Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSelected(formatTime(hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun parseTime(value: String): Calendar? {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = timeFormat.parse(value) ?: return null
            calendar
        } catch (_: Exception) {
            null
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.let { timeFormat.format(it.time) }
    }

    private fun exportAlertLogs() {
        val context = context ?: return
        AlertDebugLogger.log("AlertUI", "exportAlertLogs requested")
        val uri = AlertDebugLogger.getFileUri(context)
        if (uri == null) {
            context.showShortToast("No alert logs available yet.")
            return
        }
        ShareUtil.shareFile(context, uri)
    }
}
