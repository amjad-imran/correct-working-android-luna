package com.oreo.ui.circadianAlignment.quiz

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class SingleScrollLinearLayoutManager(
    context: Context
):
    LinearLayoutManager(context, RecyclerView.VERTICAL, false) {

    private var isScrollEnabled = true

    override fun canScrollVertically(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun getVerticalSnapPreference(): Int = SNAP_TO_START
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 100f / displayMetrics.densityDpi
            }
        }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    fun scrollToNext(recyclerView: RecyclerView) {
        val current = findFirstVisibleItemPosition()
        if (current < itemCount - 1) {
            recyclerView.smoothScrollToPosition(current + 1)
        }
    }

    fun scrollToPrevious(recyclerView: RecyclerView) {
        val current = findFirstVisibleItemPosition()
        if (current > 0) {
            recyclerView.smoothScrollToPosition(current - 1)
        }
    }

}