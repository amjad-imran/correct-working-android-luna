package com.noisefit

import com.google.common.truth.Truth
import com.noisefit_commans.utils.DateFormats
import org.junit.Test

class GoogleFitDataSourceImplTest {

    @Test
    fun `timestamp overlap returns true`() {
        Truth.assertThat(DateFormats.checkIfTimeOverlap(10,20,19,40)).isTrue()
        Truth.assertThat(DateFormats.checkIfTimeOverlap(10,20,20,30)).isTrue()
        Truth.assertThat(DateFormats.checkIfTimeOverlap(10,20,5,12)).isTrue()
        Truth.assertThat(DateFormats.checkIfTimeOverlap(10,20,10,20)).isTrue()
    }

    @Test
    fun `timestamp overlap returns false`() {
        Truth.assertThat(DateFormats.checkIfTimeOverlap(10,20,30,40)).isFalse()
        Truth.assertThat(DateFormats.checkIfTimeOverlap(10,20,5,9)).isFalse()
        Truth.assertThat(DateFormats.checkIfTimeOverlap(10,20,100,200)).isFalse()
    }


}