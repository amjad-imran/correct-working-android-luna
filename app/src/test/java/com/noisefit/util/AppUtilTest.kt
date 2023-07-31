package com.noisefit.util

import com.google.common.truth.Truth
import com.noisefit.util.ApplicationUtils
import org.junit.Test


class AppUtilTest{

    @Test
    fun `format sleep duration in hour and minutes`() {
        val inputValue = 450
        val expectedResult = Pair(7,30)
        val result = ApplicationUtils.getFormattedSleepDuration(inputValue)
        Truth.assertThat(result).isEqualTo(expectedResult)
    }
}