package com.oreo.ui.timelineScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemTimelineScreenBinding
import com.oreo.data.model.timeline.ItemTimelineModel

class ActivitiesListTimelineAdapter: RecyclerView.Adapter<ActivitiesListTimelineAdapter.TimelineViewHolder>() {

    private val mList: ArrayList<ItemTimelineModel> = ArrayList()

    inner class TimelineViewHolder(val binding: ItemTimelineScreenBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: ItemTimelineModel){
            binding.tvTitle.text = data.title
            binding.tvTitle.setTextColor(data.titleColor)

            binding.tvDesc.text = data.desc
            binding.tvTime.text = data.time
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

    fun updateDataSet(list: List<ItemTimelineModel>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

}