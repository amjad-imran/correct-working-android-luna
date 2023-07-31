package com.noisefit.ui.common

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.ui.dashboard.feature.stock.StockAdapter
import com.noisefit.ui.dashboard.feature.worldclock.WorldClockAdapter


class ItemMoveCallbackListener(val adapter: RecyclerView.Adapter<*>) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        when (adapter) {
            //TO be added later
            /*is QuickReplyAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }*/
            is WorldClockAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is StockAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            /*is ContactListAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is AppModeAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }*/
        }

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
    }
}