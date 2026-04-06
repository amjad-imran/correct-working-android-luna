package com.oreo.ui.home.summary.alerts

import com.google.common.truth.Truth.assertThat
import com.noisefit_commans.models.AlertEvent
import com.noisefit_commans.models.AlertEventSource
import com.noisefit_commans.models.DeviceAlertFeature
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.ManualMeasurement
import com.noisefit_commans.models.WearDetectionStatus
import com.oreo.alerts.AlertMirrorEvaluator
import com.oreo.alerts.MirrorConditionState
import com.oreo.alerts.WearStatusResolver
import org.junit.Test

class AlertMirrorSupportTest {

    @Test
    fun `high threshold fires once, rearms after recovery, and repeats after interval`() {
        val state = MirrorConditionState()

        val firstTrigger = AlertMirrorEvaluator.evaluateHighThreshold(
            state = state,
            value = 155,
            threshold = 150,
            hysteresis = 5,
            repeatIntervalMs = 180_000L,
            now = 1_000L
        )
        assertThat(firstTrigger.triggered).isTrue()
        assertThat(firstTrigger.stage).isEqualTo("threshold_cross")

        val cooldown = AlertMirrorEvaluator.evaluateHighThreshold(
            state = state,
            value = 154,
            threshold = 150,
            hysteresis = 5,
            repeatIntervalMs = 180_000L,
            now = 60_000L
        )
        assertThat(cooldown.triggered).isFalse()
        assertThat(cooldown.stage).isEqualTo("cooldown_skip")

        val repeated = AlertMirrorEvaluator.evaluateHighThreshold(
            state = state,
            value = 153,
            threshold = 150,
            hysteresis = 5,
            repeatIntervalMs = 180_000L,
            now = 181_000L
        )
        assertThat(repeated.triggered).isTrue()

        val rearmed = AlertMirrorEvaluator.evaluateHighThreshold(
            state = state,
            value = 144,
            threshold = 150,
            hysteresis = 5,
            repeatIntervalMs = 180_000L,
            now = 200_000L
        )
        assertThat(rearmed.rearmed).isTrue()
        assertThat(rearmed.stage).isEqualTo("rearm")

        val triggeredAfterRecovery = AlertMirrorEvaluator.evaluateHighThreshold(
            state = state,
            value = 151,
            threshold = 150,
            hysteresis = 5,
            repeatIntervalMs = 180_000L,
            now = 201_000L
        )
        assertThat(triggeredAfterRecovery.triggered).isTrue()
    }

    @Test
    fun `low threshold reminder fires once per schedule minute`() {
        val now = 8 * 60_000L
        val existing = listOf(
            AlertEvent(
                id = "evt_1",
                timestamp = now,
                feature = DeviceAlertFeature.SEDENTARY_REMINDER,
                source = AlertEventSource.SCHEDULE
            )
        )

        assertThat(
            AlertMirrorEvaluator.isSedentaryDue(
                now = now,
                startHour = 0,
                startMinute = 0,
                endHour = 23,
                endMinute = 59,
                intervalMinutes = 8,
                recentAlerts = existing
            )
        ).isFalse()

        assertThat(
            AlertMirrorEvaluator.isSedentaryDue(
                now = now + 60_000L,
                startHour = 0,
                startMinute = 0,
                endHour = 23,
                endMinute = 59,
                intervalMinutes = 9,
                recentAlerts = existing
            )
        ).isTrue()
    }

    @Test
    fun `wear resolver trusts recent sensor evidence over direct not-worn query`() {
        val resolved = WearStatusResolver.fromDirectQuery(
            directStatus = 0,
            now = 120_000L,
            recentEvidenceAt = 119_000L,
            recentEvidenceValue = 76
        )

        assertThat(resolved.isWorn).isTrue()
        assertThat(resolved.source).isEqualTo(WearStatusResolver.SOURCE_SENSOR_SAMPLE)
        assertThat(resolved.observedValue).isEqualTo(76)
    }

    @Test
    fun `wear resolver marks not worn on not-wrist manual measurement and on stale timeout`() {
        val fromManualMeasurement = WearStatusResolver.fromManualMeasurement(
            measurement = ManualMeasurement(
                value = 0,
                isError = true,
                manualMeasureType = ManualMeasureType.BLOOD_OXYGEN,
                errorReason = WearStatusResolver.NOT_WRIST_ERROR_REASON,
                isWrist = false
            ),
            now = 50_000L
        )

        assertThat(fromManualMeasurement?.isWorn).isFalse()
        assertThat(fromManualMeasurement?.source).isEqualTo(WearStatusResolver.SOURCE_MANUAL_MEASUREMENT)

        val stale = WearStatusResolver.inferPassive(
            current = WearDetectionStatus(isWorn = true, lastUpdatedAt = 0L),
            now = 400_000L,
            recentEvidenceAt = null,
            recentEvidenceValue = null
        )

        assertThat(stale.isWorn).isFalse()
        assertThat(stale.source).isEqualTo(WearStatusResolver.SOURCE_STALE_TIMEOUT)
    }
}
