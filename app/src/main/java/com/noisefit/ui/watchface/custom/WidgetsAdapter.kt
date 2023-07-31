package com.noisefit.ui.watchface.custom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.WatchFaceWidgets
import com.noisefit.luna.databinding.RowWidgetBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class WidgetsAdapter(val listener : WidgetsAdapterAction) : RecyclerView.Adapter<WidgetsAdapter.ViewHolder>() {

    val mDataSet = ArrayList<WatchFaceWidgets>()
    var selectedPosition = 0

    inner class ViewHolder(val binding: RowWidgetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFaceWidgets: WatchFaceWidgets) {
            if(watchFaceWidgets.type == -1){
                binding.ivWidget.gone()
            }else{
                binding.ivWidget.visible()
                binding.ivWidget.setImageResource(watchFaceWidgets.image)
            }

            binding.tvWidgetName.text = watchFaceWidgets.widgetName

            binding.rbSelected.isChecked = selectedPosition == bindingAdapterPosition

            binding.rbSelected.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    if (isChecked) {
                        selectedPosition = bindingAdapterPosition
                        listener.onWidgetSelected(watchFaceWidgets)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowWidgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(widgetsList: List<WatchFaceWidgets>) {
        mDataSet.clear()
        mDataSet.addAll(widgetsList)
        notifyDataSetChanged()
    }

    fun setSelected(selectedWidget: WatchFaceWidgets) {
        mDataSet.forEachIndexed { index, watchFaceWidgets ->
            if(watchFaceWidgets.type==selectedWidget.type){
                selectedPosition = index
                notifyItemChanged(0)
                notifyItemChanged(index)
                return@forEachIndexed
            }

        }

    }

    fun setSelectedWidgetName(selectedWidget: WatchFaceWidgets) {
        mDataSet.forEachIndexed { index, watchFaceWidgets ->
            if(watchFaceWidgets.widgetName==selectedWidget.widgetName){
                selectedPosition = index
                notifyItemChanged(0)
                notifyItemChanged(index)
                return@forEachIndexed
            }

        }

    }
}

interface WidgetsAdapterAction {
    fun onWidgetSelected(widget: WatchFaceWidgets)
}