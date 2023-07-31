package com.noisefit.ui.common.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.ui.dashboard.feature.appList.AppListSelectionAdapter
import com.noisefit.ui.dashboard.feature.contact.ContactListAdapter
import com.noisefit.ui.dashboard.feature.qrPayment.QrCodeListingAdapter
import com.noisefit.ui.dashboard.feature.quickreply.QuickReplyAdapter
import com.noisefit.ui.dashboard.feature.sportSelection.zh.ZhSportSelectionAdapter
import com.noisefit.ui.dashboard.feature.stock.StockAdapter
import com.noisefit.ui.dashboard.feature.widget.WidgetSelectionAdapter
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

            is QrCodeListingAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is AppListSelectionAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is ContactListAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is ZhSportSelectionAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is QuickReplyAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is WorldClockAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is StockAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
            is WidgetSelectionAdapter -> {
                adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            }
        }

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
    }
}