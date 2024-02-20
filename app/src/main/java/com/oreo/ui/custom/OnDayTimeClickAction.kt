package com.oreo.ui.custom

interface OnDayTimeClickAction {
    fun onValueSelected(value: Int, position: Int)
    fun isInteractionOnGoing(onGoing: Boolean)
    fun onTopClicked()
}