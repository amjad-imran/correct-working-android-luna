package com.noisefit.ui.dashboard.graphs.stress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.model.StressType
import com.noisefit_commans.data.model.StressZoneAnalysis
import com.noisefit.databinding.ItemStressStageLayoutBinding

class StressZoneAnalysisAdapter : RecyclerView.Adapter<StressZoneAnalysisAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<StressZoneAnalysis>()

    inner class ViewHolder(val binding: ItemStressStageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sleepStageAnalysis: StressZoneAnalysis) {
            binding.tvType.text = sleepStageAnalysis.type
            val percentage = "${sleepStageAnalysis.percentage}%"
            binding.tvPercentage.text = percentage
            binding.tvTime.text = sleepStageAnalysis.range
            binding.ltProgress.pbSteps.progress = sleepStageAnalysis.percentage
            setTypeImage(binding, sleepStageAnalysis)

        }

    }

    private fun setTypeImage(
        binding: ItemStressStageLayoutBinding,
        stressZoneAnalysis: StressZoneAnalysis
    ) {
        when (stressZoneAnalysis.stressType) {
            StressType.High -> {

                binding.ltProgress.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.ltProgress.pbSteps.context, R.color.stress_high)
                )
            }
            StressType.Medium -> {
                binding.ltProgress.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(
                        binding.ltProgress.pbSteps.context,
                        R.color.stress_medium
                    )
                )
            }
            StressType.Relax -> {

                binding.ltProgress.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.ltProgress.pbSteps.context, R.color.stress_relax)
                )
            }
            StressType.Normal -> {

                binding.ltProgress.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(
                        binding.ltProgress.pbSteps.context,
                        R.color.stress_normal
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStressStageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    fun setData(data: ArrayList<StressZoneAnalysis>) {
        mDataSet = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

}