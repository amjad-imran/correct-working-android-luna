package com.noisefit.util

import android.content.Context
import androidx.core.graphics.toColorInt
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CircadianMidPointModel
import com.oreo.data.model.CircadianMidPointState
import com.oreo.data.model.CircadianMidPointStatus
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object CircadianMidPointGraphUtils {

    fun getMidPointIndex(startDateTime: LocalDateTime, currentDateTime: LocalDateTime): Int {
        val intervalSize = 10
        val circadianMinutesBetween = ChronoUnit.MINUTES.between(startDateTime, currentDateTime)
        return (circadianMinutesBetween / intervalSize).toInt() //+ 1
    }

    fun whiteMidPoint(text: String): CircadianMidPointModel {
        return createMidPoint(
            index = 0,
            "#D2D2D2",
            "#3D3F43",
            text
        )
    }

    fun redMidPoint(text: String): CircadianMidPointModel {
        return createMidPoint(
            index = 0,
            "#FF8A8A",
            "#2C1F1F",
            text
        )
    }

    fun orangeMidPoint(text: String): CircadianMidPointModel {
        return createMidPoint(
            index = 0,
            "#FFB963",
            "#3C372A",
            text
        )
    }

    fun greenMidPoint(text: String): CircadianMidPointModel {
        return createMidPoint(
            index = 0,
            "#59E1A5",
            "#2A3C2D",
            text
        )
    }

    fun createMidPoint(
        index: Int,
        colorHex: String,
        bgColorHex: String,
        title: String
    ): CircadianMidPointModel {
        return CircadianMidPointModel().apply {
            this.index = index
            this.color = colorHex.toColorInt()
            this.bgColor = bgColorHex.toColorInt()
            this.title = title
        }
    }

    fun phaseState(
        bgRange: IntArray,
        phaseRange: IntArray,
        avgBeforeIndex: Int,
        avgNowIndex: Int,
        context: Context
    ): Triple<CircadianMidPointState, CircadianMidPointStatus, String?> {

        LOGS.d("sajcbjkac : bgRange : ${bgRange.contentToString()}\nphaseRange : ${phaseRange.contentToString()}\navgBeforeIndex : $avgBeforeIndex\navgNowIndex : $avgNowIndex")

        if (avgBeforeIndex == Int.MIN_VALUE) {
            return Triple(CircadianMidPointState.None, CircadianMidPointStatus.Locked, null)
        }

        if (avgNowIndex == Int.MIN_VALUE) {
            return Triple(CircadianMidPointState.None, CircadianMidPointStatus.AwaitingSync, null)
        }

        val beforeInPhase = avgBeforeIndex in phaseRange
        val nowInPhase = avgNowIndex in phaseRange
        val beforeInBg = avgBeforeIndex in bgRange
        val nowInBg = avgNowIndex in bgRange

        // Fully aligned
        if (avgBeforeIndex == avgNowIndex) {
            return when {
                beforeInPhase && nowInPhase ->
                    Triple(
                        CircadianMidPointState.PhaseAligned,
                        CircadianMidPointStatus.Maintained,
                        null
                    )

                avgBeforeIndex < phaseRange.first() ->
                    Triple(
                        CircadianMidPointState.PhaseAdvance,
                        CircadianMidPointStatus.Worsening,
                        context.getString(R.string.text_your_rhythm_is_significantly_early_try_extending_your_evening_routine_and_getting_light_later_to_shift_gently)
                    )

                else ->
                    Triple(
                    CircadianMidPointState.PhaseDelay,
                    CircadianMidPointStatus.Worsening,
                        context.getString(R.string.text_your_rhythm_is_highly_delayed_begin_winding_down_earlier_and_seek_morning_light_to_nudge_it_earlier)
                )
            }
        }

        // Outside both phase and bg ranges
        if (!beforeInBg && !nowInBg) {
            val isBeforeAndNowLeft = avgBeforeIndex < phaseRange.first() && avgNowIndex < phaseRange.first()
            return if (isBeforeAndNowLeft) {
                if (avgNowIndex < avgBeforeIndex) {
                    Triple(
                        CircadianMidPointState.PhaseAdvance,
                        CircadianMidPointStatus.Worsening,
                        context.getString(R.string.text_your_rhythm_is_significantly_early_try_extending_your_evening_routine_and_getting_light_later_to_shift_gently)
                    )
                } else {
                    Triple(
                        CircadianMidPointState.PhaseAdvance,
                        CircadianMidPointStatus.Correcting,
                        context.getString(R.string.text_you_re_improving_staying_active_later_and_delaying_sleep_cues_is_helping)
                    )
                }
            } else {
                if (avgNowIndex < avgBeforeIndex) {
                    Triple(
                        CircadianMidPointState.PhaseDelay,
                        CircadianMidPointStatus.Correcting,
                        null
                    )
                } else {
                    Triple(
                        CircadianMidPointState.PhaseDelay,
                        CircadianMidPointStatus.Worsening,
                        null
                    )
                }
            }
        }

        // Both in phase range
        if (beforeInPhase && nowInPhase) {
            return Triple(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Maintained, null)
        }

        // Both after phase range
        if (avgBeforeIndex > phaseRange.last() && avgNowIndex > phaseRange.last()) {
            return if (avgNowIndex < avgBeforeIndex) {
                Triple(
                    CircadianMidPointState.PhaseDelay,
                    CircadianMidPointStatus.Correcting,
                    context.getString(R.string.text_you_re_shifting_earlier_keep_supporting_it_with_morning_sunlight_and_earlier_wind_downs)
                )
            } else {
                Triple(
                    CircadianMidPointState.PhaseDelay,
                    CircadianMidPointStatus.Worsening,
                    context.getString(R.string.text_your_rhythm_shifted_later_try_dimming_lights_and_reducing_screen_time_before_bed_to_realign)
                )
            }
        }

        // Moved from after to inside phase
        if (nowInPhase && avgBeforeIndex > phaseRange.last()) {
            return Triple(
                CircadianMidPointState.PhaseAligned,
                CircadianMidPointStatus.Correcting,
                context.getString(R.string.text_you_ve_returned_to_alignment_maintain_the_rhythm_with_regular_sleep_and_morning_light)
            )
        }

        // Moved from inside to after phase
        if (beforeInPhase && avgNowIndex > phaseRange.last()) {
            return Triple(
                CircadianMidPointState.PhaseDelay,
                CircadianMidPointStatus.Worsening,
                context.getString(R.string.text_your_rhythm_shifted_later_try_dimming_lights_and_reducing_screen_time_before_bed_to_realign)
            )
        }

        // Moved from inside to before phase
        if (beforeInPhase && avgNowIndex < phaseRange.first()) {
            return Triple(
                CircadianMidPointState.PhaseAdvance,
                CircadianMidPointStatus.Worsening,
                context.getString(R.string.text_you_re_drifting_earlier_try_delaying_your_evening_routine_and_getting_light_later_in_the_day)
            )
        }

        // Moved from before to inside phase
        if (nowInPhase && avgBeforeIndex < phaseRange.first()) {
            return Triple(
                CircadianMidPointState.PhaseAligned,
                CircadianMidPointStatus.Correcting,
                context.getString(R.string.text_you_ve_returned_to_ideal_alignment_keep_reinforcing_it_with_regular_sleep_and_morning_light)
            )
        }

        // Fallback
        return Triple(CircadianMidPointState.None, CircadianMidPointStatus.AwaitingSync, null)
    }


}