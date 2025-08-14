package com.oreo.ui.timelineScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemTimelineScreenBinding
import com.oreo.data.model.timeline.ItemTimelineResponseModel

class ActivitiesListTimelineAdapter: RecyclerView.Adapter<ActivitiesListTimelineAdapter.TimelineViewHolder>() {

    private val mList: ArrayList<ItemTimelineResponseModel> = ArrayList()

    inner class TimelineViewHolder(val binding: ItemTimelineScreenBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: ItemTimelineResponseModel){
            binding.tvTitle.text = data.title
            data.titleColor?.let { binding.tvTitle.setTextColor(it) }

            binding.tvDesc.text = data.desc
            binding.tvTime.text = data.displayTime
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

    fun updateDataSet(list: List<ItemTimelineResponseModel>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

}