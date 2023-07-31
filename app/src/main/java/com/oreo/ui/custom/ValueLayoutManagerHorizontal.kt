package com.oreo.ui.custom

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.noisefit_commans.ui.getSnapPosition
import kotlin.math.abs

class ValueLayoutManagerHorizontal(val context: Context) : LinearLayoutManager(context) {

    init {
        orientation = HORIZONTAL
    }

    var callback: OnItemSelectedListener? = null
    private lateinit var recyclerView: RecyclerView
    private var snapHelper: SnapHelper? = null

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        recyclerView = view!!

        // Smart snapping
        snapHelper = LinearSnapHelper()
        snapHelper?.attachToRecyclerView(recyclerView)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        /*if (state == RecyclerView.SCROLL_STATE_IDLE) {
            val recyclerViewCenterX = getRecyclerViewCenterX()
            var minDistance = recyclerView.height
            var position = -1
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val childCenterY =
                    getDecoratedTop(child) + (getDecoratedBottom(child) - getDecoratedTop(child)) / 2
                val newDistance = abs(childCenterY - recyclerViewCenterX)
                if (newDistance < minDistance) {
                    minDistance = newDistance
                    position = recyclerView.getChildLayoutPosition(child)
                }
            }
            callback?.onItemSelected(position)
        }*/
    }

    private fun getRecyclerViewCenterX(): Int {
        return (recyclerView.right - recyclerView.left) / 2
    }

    interface OnItemSelectedListener {
        fun onItemSelected(layoutPosition: Int)
    }
}