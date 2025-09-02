package com.noisefit.util

import androidx.core.graphics.toColorInt
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
        avgNowIndex: Int
    ): Pair<CircadianMidPointState, CircadianMidPointStatus> {

        if (avgBeforeIndex == Int.MIN_VALUE) {
            return Pair(CircadianMidPointState.None, CircadianMidPointStatus.Locked)
        }

        if (avgNowIndex == Int.MIN_VALUE) {
            return Pair(CircadianMidPointState.None, CircadianMidPointStatus.AwaitingSync)
        }

        val beforeInPhase = avgBeforeIndex in phaseRange
        val nowInPhase = avgNowIndex in phaseRange
        val beforeInBg = avgBeforeIndex in bgRange
        val nowInBg = avgNowIndex in bgRange

        // Fully aligned
        if (avgBeforeIndex == avgNowIndex && beforeInPhase && nowInPhase) {
            return Pair(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Maintained)
        }

        // Outside both phase and bg ranges
        if (!beforeInBg && !nowInBg) {
            val isBeforeAndNowLeft = avgBeforeIndex < phaseRange.first() && avgNowIndex < phaseRange.first()
            return if (isBeforeAndNowLeft) {
                if (avgNowIndex < avgBeforeIndex) {
                    Pair(CircadianMidPointState.PhaseAdvance, CircadianMidPointStatus.Worsening)
                } else {
                    Pair(CircadianMidPointState.PhaseAdvance, CircadianMidPointStatus.Correcting)
                }
            } else {
                if (avgNowIndex < avgBeforeIndex) {
                    Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Correcting)
                } else {
                    Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Worsening)
                }
            }
        }

        // Both in phase range
        if (beforeInPhase && nowInPhase) {
            return Pair(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Maintained)
        }

        // Both after phase range
        if (avgBeforeIndex > phaseRange.last() && avgNowIndex > phaseRange.last()) {
            return if (avgNowIndex < avgBeforeIndex) {
                Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Correcting)
            } else {
                Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Worsening)
            }
        }

        // Moved from after to inside phase
        if (nowInPhase && avgBeforeIndex > phaseRange.last()) {
            return Pair(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Correcting)
        }

        // Moved from inside to after phase
        if (beforeInPhase && avgNowIndex > phaseRange.last()) {
            return Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Worsening)
        }

        // Moved from inside to before phase
        if (beforeInPhase && avgNowIndex < phaseRange.first()) {
            return Pair(CircadianMidPointState.PhaseAdvance, CircadianMidPointStatus.Worsening)
        }

        // Moved from before to inside phase
        if (nowInPhase && avgBeforeIndex < phaseRange.first()) {
            return Pair(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Correcting)
        }

        // Fallback
        return Pair(CircadianMidPointState.None, CircadianMidPointStatus.FAILED)
    }


}