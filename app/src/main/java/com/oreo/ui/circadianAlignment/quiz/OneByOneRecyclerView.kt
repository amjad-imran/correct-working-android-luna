package com.oreo.ui.circadianAlignment.quiz

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class OneByOneRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        // Reduce velocity to ensure only one item is scrolled
        val adjustedVelocityY = velocityY.coerceIn(-2000, 2000)
        return super.fling(velocityX, adjustedVelocityY)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

}