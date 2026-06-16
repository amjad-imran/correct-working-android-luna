package com.oreo.data.dataConverter

import com.google.common.truth.Truth.assertThat
import com.noisefit_commans.data.model.OreoSleepNetworkEntity
import com.noisefit_commans.data.model.OreoUserDataPost
import org.junit.Test

class OreoOnlineDataMapperTest {

    @Test
    fun `hasCombinedHistoryPayload returns true when sleep is the only payload`() {
        val combinedData = OreoUserDataPost(
            sleeps = listOf(OreoSleepNetworkEntity())
        )

        assertThat(hasCombinedHistoryPayload(combinedData)).isTrue()
    }

    @Test
    fun `hasCombinedHistoryPayload returns false when every payload is empty`() {
        assertThat(hasCombinedHistoryPayload(OreoUserDataPost())).isFalse()
    }
}
