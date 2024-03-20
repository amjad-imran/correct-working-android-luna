package com.noisefit_commans.ui.custom

interface SleepStageAction {
    fun onValueSelected(data: ToolTipEntry)
    fun isInteractionOnGoing(onGoing: Boolean)
}