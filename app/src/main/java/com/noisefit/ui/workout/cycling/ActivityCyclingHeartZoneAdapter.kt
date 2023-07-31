package com.noisefit.ui.workout.cycling

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowCyclingHeartRateZoneBinding
import com.noisefit_commans.data.model.HeartRateZoneData


class ActivityCyclingHeartZoneAdapter :
    RecyclerView.Adapter<ActivityCyclingHeartZoneAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<HeartRateZoneData>()

    inner class ViewHolder(val binding: RowCyclingHeartRateZoneBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(detailData: HeartRateZoneData) {

            val percentageText = "${detailData.progress}%"
            binding.tvPercentage.text = percentageText
            binding.tvTitle.text = detailData.name
            binding.tvTime.text = detailData.minutes
            binding.layoutProgress.pbSteps.progress = detailData.progress
            binding.layoutProgress.pbSteps.setIndicatorColor(
                ContextCompat.getColor(
                    binding.layoutProgress.pbSteps.context,
                    detailData.color ?: 0
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowCyclingHeartRateZoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data: List<HeartRateZoneData>) {
        mDataSet = data as ArrayList<HeartRateZoneData>
        notifyDataSetChanged()
    }
}