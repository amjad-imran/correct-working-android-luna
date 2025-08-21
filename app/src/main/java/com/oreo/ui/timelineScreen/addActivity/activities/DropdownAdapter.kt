package com.oreo.ui.timelineScreen.addActivity.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import androidx.core.graphics.toColorInt
import com.noisefit.luna.databinding.SpinnerItemAdddLogCircadianBinding
import com.noisefit_commans.ui.setVisibilityByCondition

class DropdownAdapter(
    private val items: List<AddActivityListTimelineModel>,
    private val onItemClick: (AddActivityItemsEnum) -> Unit
) : RecyclerView.Adapter<DropdownAdapter.ViewHolder>() {

    companion object{
        private val itemColors = mapOf(
            AddActivityItemsEnum.MEAL to "#D8D3A3",
            AddActivityItemsEnum.LIGHT_EXPOSURE to "#F1C48E",
            AddActivityItemsEnum.CAFFEINE to "#EEB69F",
            AddActivityItemsEnum.WORKOUT to "#8ED3F1",
            AddActivityItemsEnum.WATER to "#4CAF50",
            AddActivityItemsEnum.CYCLE_LOG to "#F18EBD",
            AddActivityItemsEnum.NAP to "#A8A8ED",
            AddActivityItemsEnum.SLEEP to "#C5A8ED"
        )
    }

    class ViewHolder(private val binding: SpinnerItemAdddLogCircadianBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: AddActivityListTimelineModel,
            showDivider: Boolean,
            onItemClick: (AddActivityItemsEnum) -> Unit
        ){
            binding.tvTitle.text = data.name

            // Set color based on item
            val color = itemColors[data.type] ?: "#CCCCCC"
            binding.tvTitle.setTextColor(color.toColorInt())

            binding.divider.root.setVisibilityByCondition(showDivider)

            binding.root.setOnClickListener {
                onItemClick(data.type)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SpinnerItemAdddLogCircadianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.spinner_item_addd_log_circadian, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], items.size-1 != position, onItemClick)

    }

    override fun getItemCount() = items.size
}
