package com.oreo.ui.heartrate

import com.oreo.ui.custom.Item


interface OnHRClickAction {
    fun onValueSelected(
        item: Item?,
        position: Int,
        time: String? = null
    )

    fun isInteractionOnGoing(onGoing: Boolean)
    fun onTopClicked()
}
