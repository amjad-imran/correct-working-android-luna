package com.oreo.alerts

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.models.AlertEvent
import com.noisefit_commans.models.AlertEventSource
import com.noisefit_commans.models.DeviceAlertFeature
import com.noisefit_commans.models.ManualMeasurement
import com.noisefit_commans.models.WearDetectionStatus
import java.util.Calendar
import kotlin.math.roundToInt

enum class MirrorCondition(val feature: DeviceAlertFeature) {
    HEART_RATE_HIGH(DeviceAlertFeature.HEART_RATE),
    HEART_RATE_LOW(DeviceAlertFeature.HEART_RATE),
    SPO2_LOW(DeviceAlertFeature.SPO2),
    HIGH_STRESS(DeviceAlertFeature.HIGH_STRESS_INDEX)
}

data class MirrorConditionState(
    var armed: Boolean = true,
    var lastTriggeredAt: Long = 0L,
    var lastObservedAt: Long = 0L,
    var lastObservedValue: Int? = null
)

data class MirrorDecision(
    val triggered: Boolean,
    val rearmed: Boolean = false,
    val stage: String,
    val observedValue: Int,
    val threshold: Int
)

object AlertMirrorEvaluator {

    private val gson = Gson()
    private val numberListType = object : TypeToken<List<Double>>() {}.type

    fun evaluateHighThreshold(
        state: MirrorConditionState,
        value: Int,
        threshold: Int,
        hysteresis: Int,
        repeatIntervalMs: Long,
        now: Long
    ): MirrorDecision {
        state.lastObservedAt = now
        state.lastObservedValue = value
        if (state.armed) {
            if (value >= threshold) {
                state.armed = false
                state.lastTriggeredAt = now
                return MirrorDecision(
                    triggered = true,
                    stage = "threshold_cross",
                    observedValue = value,
                    threshold = threshold
                )
            }
            return MirrorDecision(false, stage = "safe", observedValue = value, threshold = threshold)
        }
        if (value <= threshold - hysteresis) {
            state.armed = true
            return MirrorDecision(
                triggered = false,
                rearmed = true,
                stage = "rearm",
                observedValue = value,
                threshold = threshold
            )
        }
        if (value >= threshold && now - state.lastTriggeredAt >= repeatIntervalMs) {
            state.lastTriggeredAt = now
            return MirrorDecision(
                triggered = true,
                stage = "threshold_cross",
                observedValue = value,
                threshold = threshold
            )
        }
        return MirrorDecision(false, stage = "cooldown_skip", observedValue = value, threshold = threshold)
    }

    fun evaluateLowThreshold(
        state: MirrorConditionState,
        value: Int,
        threshold: Int,
        hysteresis: Int,
        repeatIntervalMs: Long,
        now: Long
    ): MirrorDecision {
        state.lastObservedAt = now
        state.lastObservedValue = value
        if (state.armed) {
            if (value <= threshold) {
                state.armed = false
                state.lastTriggeredAt = now
                return MirrorDecision(
                    triggered = true,
                    stage = "threshold_cross",
                    observedValue = value,
                    threshold = threshold
                )
            }
            return MirrorDecision(false, stage = "safe", observedValue = value, threshold = threshold)
        }
        if (value >= threshold + hysteresis) {
            state.armed = true
            return MirrorDecision(
                triggered = false,
                rearmed = true,
                stage = "rearm",
                observedValue = value,
                threshold = threshold
            )
        }
        if (value <= threshold && now - state.lastTriggeredAt >= repeatIntervalMs) {
            state.lastTriggeredAt = now
            return MirrorDecision(
                triggered = true,
                stage = "threshold_cross",
                observedValue = value,
                threshold = threshold
            )
        }
        return MirrorDecision(false, stage = "cooldown_skip", observedValue = value, threshold = threshold)
    }

    fun extractLatestPositiveValue(breakUp: String?): Int? {
        if (breakUp.isNullOrBlank()) {
            return null
        }
        return try {
            val values: List<Double> = gson.fromJson(breakUp, numberListType) ?: emptyList()
            values.asReversed().firstOrNull { it > 0.0 }?.roundToInt()
        } catch (_: Exception) {
            null
        }
    }

    fun isBedtimeDue(
        now: Long,
        hour: Int,
        minute: Int,
        recentAlerts: List<AlertEvent>
    ): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        val isTargetMinute = calendar.get(Calendar.HOUR_OF_DAY) == hour &&
            calendar.get(Calendar.MINUTE) == minute
        if (!isTargetMinute) {
            return false
        }
        return !hasAlertInSameMinute(
            recentAlerts = recentAlerts,
            feature = DeviceAlertFeature.SLEEP_REMINDER,
            source = AlertEventSource.SCHEDULE,
            now = now
        )
    }

    fun isSedentaryDue(
        now: Long,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        intervalMinutes: Int,
        recentAlerts: List<AlertEvent>
    ): Boolean {
        if (intervalMinutes <= 0) {
            return false
        }
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        if (currentMinutes < start || currentMinutes > end) {
            return false
        }
        val elapsed = currentMinutes - start
        if (elapsed < 0 || elapsed % intervalMinutes != 0) {
            return false
        }
        return !hasAlertInSameMinute(
            recentAlerts = recentAlerts,
            feature = DeviceAlertFeature.SEDENTARY_REMINDER,
            source = AlertEventSource.SCHEDULE,
            now = now
        )
    }

    private fun hasAlertInSameMinute(
        recentAlerts: List<AlertEvent>,
        feature: DeviceAlertFeature,
        source: AlertEventSource,
        now: Long
    ): Boolean {
        val minuteBucket = now / 60_000L
        return recentAlerts.any { event ->
            event.feature == feature &&
                event.source == source &&
                event.timestamp / 60_000L == minuteBucket
        }
    }
}

object WearStatusResolver {

    const val RECENT_SENSOR_EVIDENCE_WINDOW_MS = 2 * 60_000L
    const val STALE_NOT_WORN_WINDOW_MS = 5 * 60_000L
    const val NOT_WRIST_ERROR_REASON = 0x01
    const val SOURCE_DIRECT_QUERY = "direct_query"
    const val SOURCE_SENSOR_SAMPLE = "sensor_sample"
    const val SOURCE_MANUAL_MEASUREMENT = "manual_measurement"
    const val SOURCE_STALE_TIMEOUT = "stale_timeout"

    fun fromDirectQuery(
        directStatus: Int,
        now: Long,
        recentEvidenceAt: Long?,
        recentEvidenceValue: Int?
    ): WearDetectionStatus {
        if (directStatus == 1) {
            return WearDetectionStatus(
                isWorn = true,
                lastUpdatedAt = now,
                source = SOURCE_DIRECT_QUERY,
                observedValue = recentEvidenceValue
            )
        }
        if (recentEvidenceAt != null && now - recentEvidenceAt <= RECENT_SENSOR_EVIDENCE_WINDOW_MS) {
            return WearDetectionStatus(
                isWorn = true,
                lastUpdatedAt = recentEvidenceAt,
                source = SOURCE_SENSOR_SAMPLE,
                observedValue = recentEvidenceValue
            )
        }
        return WearDetectionStatus(
            isWorn = false,
            lastUpdatedAt = now,
            source = SOURCE_DIRECT_QUERY,
            observedValue = recentEvidenceValue
        )
    }

    fun fromSensorEvidence(
        now: Long,
        observedValue: Int
    ): WearDetectionStatus {
        return WearDetectionStatus(
            isWorn = true,
            lastUpdatedAt = now,
            source = SOURCE_SENSOR_SAMPLE,
            observedValue = observedValue
        )
    }

    fun fromManualMeasurement(
        measurement: ManualMeasurement,
        now: Long
    ): WearDetectionStatus? {
        if (measurement.errorReason == NOT_WRIST_ERROR_REASON || measurement.isWrist == false) {
            return WearDetectionStatus(
                isWorn = false,
                lastUpdatedAt = now,
                source = SOURCE_MANUAL_MEASUREMENT,
                observedValue = measurement.value
            )
        }
        if (!measurement.isError && measurement.value > 0) {
            return WearDetectionStatus(
                isWorn = true,
                lastUpdatedAt = now,
                source = SOURCE_MANUAL_MEASUREMENT,
                observedValue = measurement.value
            )
        }
        return null
    }

    fun inferPassive(
        current: WearDetectionStatus,
        now: Long,
        recentEvidenceAt: Long?,
        recentEvidenceValue: Int?
    ): WearDetectionStatus {
        if (recentEvidenceAt != null && now - recentEvidenceAt <= RECENT_SENSOR_EVIDENCE_WINDOW_MS) {
            return WearDetectionStatus(
                isWorn = true,
                lastUpdatedAt = recentEvidenceAt,
                source = SOURCE_SENSOR_SAMPLE,
                observedValue = recentEvidenceValue
            )
        }
        if (recentEvidenceAt == null || now - recentEvidenceAt >= STALE_NOT_WORN_WINDOW_MS) {
            return WearDetectionStatus(
                isWorn = false,
                lastUpdatedAt = now,
                source = SOURCE_STALE_TIMEOUT,
                observedValue = recentEvidenceValue
            )
        }
        return current
    }
}
