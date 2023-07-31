package com.noisefit.util

import com.google.common.truth.Truth
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.ActivityConvertUtils
import org.junit.Test

class ActivityConvertUtilsTest {

    @Test
    fun `average pace in metrics`() {
        val expectedResult = "05'00"
        val result = ActivityConvertUtils.avgPace(Units.METRIC,5000,25*60)
        Truth.assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `average speed in metrics`() {
        val expectedResult = "12.00"
        val result = ActivityConvertUtils.averageSpeed(Units.METRIC,5000,25*60)
        Truth.assertThat(result).isEqualTo(expectedResult)
    }

}