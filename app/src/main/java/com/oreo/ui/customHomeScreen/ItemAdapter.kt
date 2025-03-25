package com.oreo.ui.customHomeScreen

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemHomeDragBinding

class ItemAdapter(private val items: MutableList<CustomHomeScreenItem>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    var onDragStartListener: ((RecyclerView.ViewHolder) -> Unit)? = null
    private val DRAG_DELAY_MS = 150L

    inner class ItemViewHolder(val binding: ItemHomeDragBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemHomeDragBinding = DataBindingUtil.inflate(inflater, R.layout.item_home_drag, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item: CustomHomeScreenItem = items[position]  // Data Binding
        holder.binding.ivIcon.setImageResource(item.icon)
        holder.binding.tvTitle.text = item.title
        holder.binding.switchMain.isChecked = item.switchState

        holder.binding.ivDragIcon.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Post a delayed runnable to start drag
                    v.postDelayed({
                        onDragStartListener?.invoke(holder)
                    }, DRAG_DELAY_MS)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Remove the delayed callback if touch ends early
                    v.removeCallbacks(null)
                    false
                }
                else -> false
            }
        }

    }

    override fun getItemCount(): Int = items.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
        Log.d("itemReorder", items.toString())
    }
}