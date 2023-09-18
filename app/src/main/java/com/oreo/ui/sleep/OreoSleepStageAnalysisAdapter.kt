package com.oreo.ui.sleep

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.LayoutUpdatedSleepStageAnalysisBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.SleepStageAnalysis
import com.noisefit_commans.models.SleepType
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class OreoSleepStageAnalysisAdapter :
    RecyclerView.Adapter<OreoSleepStageAnalysisAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SleepStageAnalysis>()

    inner class ViewHolder(val binding: LayoutUpdatedSleepStageAnalysisBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sleepStageAnalysis: SleepStageAnalysis) {
            binding.tvStageName.text = sleepStageAnalysis.type
            val weightPercentValue = calculateWeightPercent(sleepStageAnalysis.percentage)
            binding.view1.layoutParams =
                binding.view1.layoutParams.apply {
                    (this as LinearLayout.LayoutParams).weight =
                        weightPercentValue
                }
            binding.lytChildContainer.layoutParams =
                binding.lytChildContainer.layoutParams.apply {

                    (this as LinearLayout.LayoutParams).weight =
                        100 - weightPercentValue
                }

            binding.tvDuration.text = returnRemark(sleepStageAnalysis.timeInMinutes)
            if (weightPercentValue > 0)
                binding.view1.visible()
            else
                binding.view1.gone()

            setTypeImage(binding, sleepStageAnalysis)

        }
    }

    private fun returnRemark(value: Int): String {
        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(value)
        val leftText: String = if (hour > 0)
            if (minute > 0)
                "$hour h $minute min"
            else
                "$hour h"
        else if (minute > 0) {
            "$minute min"
        } else {
            "-"
        }
        return leftText

    }

    private fun calculateWeightPercent(progress: Int): Float {
        val progressPercent: Float = if (progress >= 42) {
            42F
        } else {
            progress.toFloat()
        }
        return progressPercent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutUpdatedSleepStageAnalysisBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: ArrayList<SleepStageAnalysis>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

    fun getLevel(percent: Int): String {
        return when (percent) {
            in 0 until 33 -> "Low"
            in 34 until 66 -> "Moderate"
            in 67 until 100 -> "High"
            else -> ""
        }

    }

    private fun setTypeImage(
        binding: LayoutUpdatedSleepStageAnalysisBinding,
        sleepStageAnalysis: SleepStageAnalysis
    ) {
        when (sleepStageAnalysis.sleepType) {
            SleepType.AWAKE -> {
                binding.view1.setBackgroundResource(R.drawable.awake_bar_with_round_edge)

            }

            SleepType.DEEP -> {
                binding.view1.setBackgroundResource(R.drawable.deep_bar_with_round_edge)
            }

            SleepType.LIGHT -> {
                binding.view1.setBackgroundResource(R.drawable.light_bar_with_round_edge)
            }

            SleepType.REM -> {
                binding.view1.setBackgroundResource(R.drawable.rem_bar_with_round_edge)
            }

            else -> {}
        }
    }

}

