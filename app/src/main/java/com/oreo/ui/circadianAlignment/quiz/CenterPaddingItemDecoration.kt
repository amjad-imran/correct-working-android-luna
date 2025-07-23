package com.oreo.ui.circadianAlignment.quiz

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CenterPaddingItemDecoration(private val topBottomPadding: Int):
    RecyclerView.ItemDecoration(){

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        // Add top padding to the first item
        if (position == 0) {
            outRect.top = topBottomPadding
        }

        // Add bottom padding to the last item
        if (position == itemCount - 1) {
            outRect.bottom = topBottomPadding
        }
    }

}