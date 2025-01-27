package com.noisefit.util

import com.google.common.truth.Truth
import com.oreo.util.DateTimeUtil
import org.junit.Test
import java.time.LocalTime


class DateTimeUtilTest {

    @Test
    fun `2 local date time to duration in minutes`() {
        Truth.assertThat(
            DateTimeUtil.getDurationMinutes(
                LocalTime.of(23, 0),
                LocalTime.of(6, 0)
            )
        ).isEqualTo(7 * 60)
    }
}