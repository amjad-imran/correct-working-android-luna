package com.oreo.ui.timelineScreen.addActivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemActivityListTimelineBinding
import com.noisefit_commans.ui.setVisibilityByCondition
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel

class ActivitiesListAdapter(
    private val onItemClick: (AddActivityListTimelineModel) -> Unit
) : RecyclerView.Adapter<ActivitiesListAdapter.ViewHolder>() {

    private val mList: ArrayList<AddActivityListTimelineModel> = ArrayList()

    inner class ViewHolder(private val binding: ItemActivityListTimelineBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AddActivityListTimelineModel, isLast: Boolean) {
            binding.tvItem.text = item.name
            binding.tvItem.setTextColor(item.titleColor)
            binding.divider.root.setVisibilityByCondition(!isLast)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemActivityListTimelineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position], position == mList.size - 1)
    }

    override fun getItemCount() = mList.size

    fun updateDataSet(list: List<AddActivityListTimelineModel>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }
}