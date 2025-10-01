package com.oreo.ui.timelineScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemTimelineScreenBinding
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.MEAL_INTAKE_KEY_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.SYMPTOM_KEY

class ActivitiesListTimelineAdapter(
    val onItemClick: (ItemTimelineResponseModel) -> Unit
) : RecyclerView.Adapter<ActivitiesListTimelineAdapter.TimelineViewHolder>() {

    private val mList: ArrayList<ItemTimelineResponseModel> = ArrayList()

    inner class TimelineViewHolder(val binding: ItemTimelineScreenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ItemTimelineResponseModel) {
            binding.tvTitle.text = data.title
            data.titleColor?.let { binding.tvTitle.setTextColor(it) }

            binding.tvDesc.text = data.desc
            if (data.event.equals(SYMPTOM_KEY, true) || data.event.equals(MEAL_INTAKE_KEY_KEY)) {
                binding.tvTime.invisible()
            } else {
                binding.tvTime.apply {
                    text = data.displayTime
                    visible()
                }
            }

            binding.root.setOnClickListener {
                onItemClick(data)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        return TimelineViewHolder(
            ItemTimelineScreenBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    fun updateDataSet(list: List<ItemTimelineResponseModel>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

}