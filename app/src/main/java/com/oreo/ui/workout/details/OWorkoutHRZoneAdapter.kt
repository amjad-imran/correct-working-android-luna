package com.oreo.ui.workout.details

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.noisefit.luna.databinding.RowOwdHrZoneItemBinding
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.OWDActivityHRZoneData

class OWorkoutHRZoneAdapter() :
    RecyclerView.Adapter<OWorkoutHRZoneAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OWDActivityHRZoneData>()
    private var listener: OWorkoutHRZoneInteractionListener? = null

    fun setListener(listener: OWorkoutHRZoneInteractionListener) {
        this.listener = listener
    }

    inner class ViewHolder(val binding: RowOwdHrZoneItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailData: OWDActivityHRZoneData, position: Int) {

            binding.root.alpha = if (detailData.isDisable) {
                0.5f
            } else {
                1f
            }
            binding.tvTitle.text = detailData.title
            binding.tvRange.text = detailData.range
            binding.tvPercentage.text = "${detailData.percentage}%"
            binding.tvDuration.text = detailData.duration
            val color = Color.parseColor(detailData.color)
            binding.tvTitle.setTextColor(color)
            binding.tvPercentage.setTextColor(color)
//            binding.percentageBar.percentageColor = color
//            binding.percentageBar.percentage = detailData.percentage
            binding.root.setOnClickListener {
                LOGS.d("dasadsads ${detailData.isHighlighted}")
                listener?.onClick(position, !detailData.isHighlighted, detailData)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OWorkoutHRZoneAdapter.ViewHolder {
        val binding =
            RowOwdHrZoneItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OWorkoutHRZoneAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    fun updateData(position: Int, isHighlighted: Boolean) {
        mDataSet.forEachIndexed { index, owdActivityHRZoneData ->

            if(isHighlighted){
                mDataSet[index].isHighlighted = index == position
                mDataSet[index].isDisable = index != position
            }else{
                mDataSet[index].isHighlighted = false
                mDataSet[index].isDisable = false
            }

        }

        LOGS.d("dasadsads $position $isHighlighted ${Gson().toJson(mDataSet)}")
//        mDataSet[position].isHighlighted = isDisable
        notifyDataSetChanged()
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data: List<OWDActivityHRZoneData>) {
        mDataSet = data as ArrayList<OWDActivityHRZoneData>
        notifyDataSetChanged()
    }

    interface OWorkoutHRZoneInteractionListener {
        fun onClick(selectedPosition: Int, isHighlighted: Boolean, data: OWDActivityHRZoneData)
    }
}