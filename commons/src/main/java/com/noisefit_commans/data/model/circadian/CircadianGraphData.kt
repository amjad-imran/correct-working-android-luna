package com.noisefit_commans.data.model.circadian

import com.google.gson.annotations.SerializedName

data class CircadianGraphData(

    val onboarding: Boolean?,

    @SerializedName("sleep_data")
    val sleepData: SleepData?,

    @SerializedName("activity_window_graph")
    val activityWindowGraph: ItemCircadianGraphData?,

    @SerializedName("caffeine_window_graph")
    val caffeineWindowGraph: ItemCircadianGraphData?,

    @SerializedName("cortisol_peak_window_graph")
    val cortisolPeakWindowGraph: ItemCircadianGraphData?,

    @SerializedName("dlmo_phase_window_graph")
    val dlmoPhaseWindowGraph: ItemCircadianGraphData?,

    @SerializedName("first_focus_peak_window_graph")
    val firstFocusPeakWindowGraph: ItemCircadianGraphData?,

    @SerializedName("gh_pulse_window_graph")
    val ghPulseWindowGraph: ItemCircadianGraphData?,

    @SerializedName("light_anchoring_phase_window_graph")
    val lightAnchoringPhaseWindowGraph: ItemCircadianGraphData?,

    @SerializedName("melatonin_prep_phase_window_graph")
    val melatoninPrepPhaseWindowGraph: ItemCircadianGraphData?,

    @SerializedName("second_focus_peak_window_graph")
    val secondFocusPeakWindowGraph: ItemCircadianGraphData?,

    @SerializedName("sleep_window_opens_graph")
    val sleepWindowOpensGraph: ItemCircadianGraphData?,

    //-
    @SerializedName("circadian_mid_point")
    val circadianMidPointData: CircadianMidPointData?,

    @SerializedName("energy_graph")
    val energyGraph: List<EnergyGraph>?,

    @SerializedName("start_time")
    val startTime: String?,

    @SerializedName("end_time")
    val endTime: String?,

    val title: String?,
    val description: String?,

    @SerializedName("is_locked")
    val isLockedCircularView: Boolean?
)

data class SleepData(
    @SerializedName("bed_time")
    val bedTime: String?,
    @SerializedName("wake_time")
    val wakeTime: String?,
)

data class EnergyGraph(
    @SerializedName("start_time")
    val startTime: String?,
    val energy: Float?,
)

data class ItemCircadianGraphData(
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("end_time")
    val endTime: String?,
    @SerializedName("peak_time")
    val peakTime: String?
)

data class CircadianMidPointData(
    @SerializedName("start_time")
    val startTime: String?,

    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("circadian_midpoint")
    val circadianMidpoint: String?,

    @SerializedName("avg_now")
    val avgNow: String?,

    @SerializedName("avg_before")
    val avgBefore: String?,

    val chronotype: String?,

    @SerializedName("nudge")
    val nudge: NudgeCircadianGraph?
)

data class NudgeCircadianGraph(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("cue_1") val cue1: String? = null,
    @SerializedName("cue_2") val cue2: String? = null,
)