package com.noisefit_zhsdk.handler

import com.google.common.truth.Truth.assertThat
import com.zhapp.ble.bean.DailyBean
import com.zhapp.ble.bean.SleepBean
import org.junit.Test

class LunaSdk231DataNormalizerTest {

    @Test
    fun `normalizeLegacyMetricBreakup averages valid second-based samples into legacy buckets`() {
        val normalized = LunaSdk231DataNormalizer.normalizeLegacyMetricBreakup(
            values = listOf(
                80, 0, 100, 0, 90, 0, 110, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            ),
            frequency = 30,
            frequencyVersion = 1,
            targetFrequencyMinutes = 5
        )

        assertThat(normalized).containsExactly(95, 0).inOrder()
    }

    @Test
    fun `resolve calorie helpers prefer total calorie fields when Oura walking calories are zero`() {
        val dailyBean = DailyBean().apply {
            calorieData = arrayListOf(110, 119, 100)
            todayCalorieData = 954
            todayOuraCalorieData = 0
            todayOuraCalorieHourlyData = arrayListOf(0, 0, 0)
        }

        assertThat(LunaSdk231DataNormalizer.resolveHourlyCalories(dailyBean, 1)).isEqualTo(119)
        assertThat(LunaSdk231DataNormalizer.resolveTotalCalories(dailyBean)).isEqualTo(954)
    }

    @Test
    fun `usesLegacySleepMinuteUnits detects minute-based legacy sleep totals`() {
        val sleepBean = SleepBean().apply {
            startSleepTimestamp = 1_712_531_200L
            endSleepTimestamp = 1_712_560_000L
            sleepDuration = 420
            awakeTime = 60
            lightSleepTime = 220
            deepSleepTime = 160
            rapidEyeMovementTime = 40
        }

        assertThat(LunaSdk231DataNormalizer.usesLegacySleepMinuteUnits(sleepBean)).isTrue()
        assertThat(
            LunaSdk231DataNormalizer.normalizeLegacySleepDurationSeconds(
                sleepBean.sleepDuration,
                usesMinuteUnits = true
            )
        ).isEqualTo(25_200)
    }

    @Test
    fun `usesLegacySleepMinuteUnits keeps second-based sleep totals unchanged`() {
        val sleepBean = SleepBean().apply {
            startSleepTimestamp = 1_712_531_200L
            endSleepTimestamp = 1_712_560_000L
            sleepDuration = 25_200
            awakeTime = 3_600
            lightSleepTime = 13_200
            deepSleepTime = 9_600
            rapidEyeMovementTime = 2_400
        }

        assertThat(LunaSdk231DataNormalizer.usesLegacySleepMinuteUnits(sleepBean)).isFalse()
        assertThat(
            LunaSdk231DataNormalizer.normalizeLegacySleepDurationSeconds(
                sleepBean.sleepDuration,
                usesMinuteUnits = false
            )
        ).isEqualTo(25_200)
    }

    @Test
    fun `trimFutureMetricBreakupForToday clears future slots for current-day 15 minute data`() {
        val trimmed = LunaSdk231DataNormalizer.trimFutureMetricBreakupForToday(
            dateTime = "2026-04-14 00:00:00",
            values = listOf(10, 20, 30, 40, 50, 60),
            frequencyMinutes = 15,
            currentTimeMillis = 1_776_107_640_000L
        )

        assertThat(trimmed).containsExactly(10, 20, 30, 0, 0, 0).inOrder()
    }

    @Test
    fun `trimFutureMetricBreakupForToday keeps historical day data unchanged`() {
        val values = listOf(10, 20, 30, 40)
        val trimmed = LunaSdk231DataNormalizer.trimFutureMetricBreakupForToday(
            dateTime = "2026-04-13 00:00:00",
            values = values,
            frequencyMinutes = 15,
            currentTimeMillis = 1_776_107_640_000L
        )

        assertThat(trimmed).isEqualTo(values)
    }
}
