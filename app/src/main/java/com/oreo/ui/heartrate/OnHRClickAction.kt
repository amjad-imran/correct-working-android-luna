package com.oreo.ui.heartrate

interface OnHRClickAction {
    fun onValueSelected(value: Int, position: Int, time: String? = null)
    fun isInteractionOnGoing(onGoing: Boolean)
    fun onTopClicked()
}
