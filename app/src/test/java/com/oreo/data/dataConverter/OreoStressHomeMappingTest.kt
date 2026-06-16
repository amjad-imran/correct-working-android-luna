package com.oreo.data.dataConverter

import com.google.common.truth.Truth.assertThat
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.Stress
import org.junit.Test

class OreoStressHomeMappingTest {

    @Test
    fun `getStressCombinedData keeps the old 96 slot server stress breakup unchanged`() {
        val serverBreakUp = listOf(10, 20, 30) + List(93) { 0 }

        val model = OreoStressDataConvertor().getStressCombinedData(
            dayData = ServerUserHealthData(
                date = "2026-04-15",
                stress = Stress(breakUp = serverBreakUp)
            )
        )

        assertThat(model.items?.map { it.value }?.take(3)).containsExactly(10, 20, 30).inOrder()
        assertThat(model.items).hasSize(96)
    }

    @Test
    fun `getStressCombinedData returns an empty 96 slot graph when stress breakup is missing`() {
        val model = OreoStressDataConvertor().getStressCombinedData(
            dayData = ServerUserHealthData(
                date = "2026-04-15"
            )
        )

        assertThat(model.items).hasSize(96)
        assertThat(model.items?.all { it.value == 0 }).isTrue()
    }
}
