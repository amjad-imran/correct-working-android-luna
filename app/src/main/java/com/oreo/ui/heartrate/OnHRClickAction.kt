package com.oreo.ui.heartrate

interface OnHRClickAction {
    fun onValueSelected(value: Int, position: Int)
    fun isInteractionOnGoing(onGoing: Boolean)
    fun onTopClicked()
}
