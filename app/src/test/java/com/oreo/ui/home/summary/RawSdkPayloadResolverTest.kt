package com.oreo.ui.home.summary

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.SdkRawCaptureEntry
import com.noisefit_commans.data.local.abstraction.SdkRawCaptureEnvelope
import org.junit.Test

class RawSdkPayloadResolverTest {

    private val gson = Gson()

    @Test
    fun `resolveSdkRawPayload prefers latest valid capture when latest callback is zero only`() {
        val payload = gson.toJson(
            SdkRawCaptureEnvelope(
                sessionId = 1L,
                startedAt = 100L,
                updatedAt = 200L,
                mode = 1,
                label = "TODAY",
                captures = listOf(
                    SdkRawCaptureEntry(
                        capturedAt = 1_000L,
                        payload = """
                            {"date":"2026-04-06","continuousHeartRateFrequency":5,"frequencyVersion":0,"heartRateData":[0,72,0]}
                        """.trimIndent()
                    ),
                    SdkRawCaptureEntry(
                        capturedAt = 2_000L,
                        payload = """
                            {"date":"2026-04-06","continuousHeartRateFrequency":5,"frequencyVersion":0,"heartRateData":[0,0,0]}
                        """.trimIndent()
                    )
                )
            )
        )

        val resolved = resolveSdkRawPayload(
            RawSdkPayloadBottomSheet.TYPE_CONTINUOUS_HEART_RATE,
            payload
        )

        assertThat(resolved.totalCaptureCount).isEqualTo(2)
        assertThat(resolved.validCaptureCount).isEqualTo(1)
        assertThat(resolved.selectedCapture?.capturedAt).isEqualTo(1_000L)
        assertThat(resolved.latestCapture?.capturedAt).isEqualTo(2_000L)
        assertThat(
            buildSdkFrequencySummaryText(
                resolved = resolved,
                frequencyKey = "continuousHeartRateFrequency",
                primaryListKey = "heartRateData"
            )
        ).contains("heartRateData 1/3")
    }

    @Test
    fun `resolveSdkRawPayload supports legacy single payload storage`() {
        val payload =
            """{"date":"2026-04-06","frequency":60,"rri":[0,820,0,790],"frequencyVersion":0}"""

        val resolved = resolveSdkRawPayload(
            RawSdkPayloadBottomSheet.TYPE_CONTINUOUS_RRI,
            payload
        )

        assertThat(resolved.totalCaptureCount).isEqualTo(1)
        assertThat(resolved.validCaptureCount).isEqualTo(1)
        assertThat(resolved.selectedRawPayload).isEqualTo(payload)
        assertThat(
            buildSdkPayloadSummaryText(
                resolved = resolved,
                primaryListKey = "rri"
            )
        ).contains("rri 2/4")
    }

    @Test
    fun `resolveSdkRawPayload treats sport heart-rate samples as nested data`() {
        val payload = gson.toJson(
            SdkRawCaptureEnvelope(
                sessionId = 1L,
                startedAt = 100L,
                updatedAt = 200L,
                mode = 3,
                label = "ALL",
                captures = listOf(
                    SdkRawCaptureEntry(
                        capturedAt = 1_000L,
                        payload = """
                            {"date":"2026-04-06","hrList":[{"heartRate":[0,88,0],"startTimestamp":1712340000},{"heartRate":[0,0,0],"startTimestamp":1712340300}]}
                        """.trimIndent()
                    ),
                    SdkRawCaptureEntry(
                        capturedAt = 2_000L,
                        payload = """
                            {"date":"2026-04-06","hrList":[{"heartRate":[0,0,0],"startTimestamp":1712340600},{"heartRate":[0,0,0],"startTimestamp":1712340900}]}
                        """.trimIndent()
                    )
                )
            )
        )

        val resolved = resolveSdkRawPayload(
            RawSdkPayloadBottomSheet.TYPE_SPORT_HEART_RATE_AFTER,
            payload
        )

        assertThat(resolved.totalCaptureCount).isEqualTo(2)
        assertThat(resolved.validCaptureCount).isEqualTo(1)
        assertThat(resolved.selectedCapture?.capturedAt).isEqualTo(1_000L)
        assertThat(resolved.latestCapture?.capturedAt).isEqualTo(2_000L)
        assertThat(
            buildSdkPayloadSummaryText(
                resolved = resolved,
                primaryListKey = "hrList"
            )
        ).contains("hrList 2 segments | heartRate 1/6")
        assertThat(buildSdkRawSnapshotText(resolved)).contains("zero-only")
    }
}
