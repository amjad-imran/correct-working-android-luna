package com.oreo.ui.stress

interface OnStressClickAction {
    fun onValueSelected(value: Int, position: Int)
    fun isInteractionOnGoing(onGoing: Boolean)
    fun onTopClicked()
}
