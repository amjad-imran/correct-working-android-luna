package com.oreo.ui.workout.details

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowOwdHrZoneItemBinding
import com.noisefit.luna.databinding.RowOwdItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OWDActivityData
import com.oreo.data.model.OWDActivityHRZoneData

class OWorkoutHRZoneAdapter : RecyclerView.Adapter<OWorkoutHRZoneAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OWDActivityHRZoneData>()

    inner class ViewHolder(val binding: RowOwdHrZoneItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailData: OWDActivityHRZoneData) {

            binding.tvTitle.text = detailData.title
            binding.tvRange.text = detailData.range
            binding.tvPercentage.text = "${detailData.percentage}%"
            binding.tvDuration.text = detailData.duration
            val color = Color.parseColor(detailData.color)
            binding.tvTitle.setTextColor(color)
            binding.percentageBar.percentageColor = color
            binding.percentageBar.percentage = detailData.percentage
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OWorkoutHRZoneAdapter.ViewHolder {
        val binding = RowOwdHrZoneItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OWorkoutHRZoneAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data: List<OWDActivityHRZoneData>) {
        mDataSet = data as ArrayList<OWDActivityHRZoneData>
        notifyDataSetChanged()
    }
}