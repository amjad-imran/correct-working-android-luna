package com.oreo.ui.customHomeScreen

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemHomeDragBinding
import com.noisefit_commans.utils.LOGS

class ItemAdapter(private val listener:ItemClickListener) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private val mDataSet = ArrayList<CustomHomeScreenItem>()
    var onDragStartListener: ((RecyclerView.ViewHolder) -> Unit)? = null
    private val DRAG_DELAY_MS = 150L

    inner class ItemViewHolder(val binding: ItemHomeDragBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemHomeDragBinding = DataBindingUtil.inflate(inflater, R.layout.item_home_drag, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item: CustomHomeScreenItem = mDataSet[position]  // Data Binding
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

        holder.binding.switchMain.setOnCheckedChangeListener{ button, isChecked ->
//            if (button.isChecked.not()){
//                return@setOnCheckedChangeListener
//            }
            item.switchState = isChecked
            if(isChecked.not()){
                listener.onItemStateChanged(position)
            }
        }

    }

    override fun getItemCount(): Int = mDataSet.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return

        val item = mDataSet.removeAt(fromPosition)
        mDataSet.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun updateData(dataSet: List<CustomHomeScreenItem>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun getDataSet(): List<CustomHomeScreenItem> = mDataSet
    fun enableSleepToggle() {
        val index = mDataSet.indexOfFirst { it.key.equals("sleep",true) }

        if(index!=-1){
            mDataSet[index].switchState = true
            notifyItemChanged(index)
        }
    }

}
interface ItemClickListener{
    fun onItemStateChanged(position: Int)
}