package com.noisefit.util

import com.google.common.truth.Truth
import com.noisefit.ui.common.calculateInitFromPercentage
import org.junit.Test

class FloatExtensionsTest {


    @Test
    fun `calculateInitFromPercentage test success`(){
        Truth.assertThat(100f.calculateInitFromPercentage(100f)).isEqualTo(100)
        Truth.assertThat(60f.calculateInitFromPercentage(100f)).isEqualTo(60)
        Truth.assertThat(23f.calculateInitFromPercentage(100f)).isEqualTo(23)
        Truth.assertThat(99f.calculateInitFromPercentage(100f)).isEqualTo(99)
        Truth.assertThat(0f.calculateInitFromPercentage(100f)).isEqualTo(0)
    }
}