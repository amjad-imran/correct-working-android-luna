package com.noisefit_commans.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class ValueLayoutManager(val context: Context) : LinearLayoutManager(context) {

    init {
        orientation = VERTICAL
    }

    var callback: OnItemSelectedListener? = null
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        recyclerView = view!!

        // Smart snapping
        LinearSnapHelper().attachToRecyclerView(recyclerView)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            val recyclerViewCenterY = getRecyclerViewCenterY()
            var minDistance = recyclerView.height
            var position = -1
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val childCenterY =
                    getDecoratedTop(child) + (getDecoratedBottom(child) - getDecoratedTop(child)) / 2
                val newDistance = abs(childCenterY - recyclerViewCenterY)
                if (newDistance < minDistance) {
                    minDistance = newDistance
                    position = recyclerView.getChildLayoutPosition(child)
                }
            }
            callback?.onItemSelected(position)
        }
    }

    private fun getRecyclerViewCenterY(): Int {
        return (recyclerView.bottom - recyclerView.top) / 2
    }

    interface OnItemSelectedListener {
        fun onItemSelected(layoutPosition: Int)
    }
}
