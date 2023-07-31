package com.noisefit.ui.dashboard.graphs.sleep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.databinding.ItemSleepStageLayoutBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.SleepStageAnalysis
import com.noisefit_commans.models.SleepType
import com.noisefit_commans.utils.LOGS


class SleepStageAnalysisAdapter : RecyclerView.Adapter<SleepStageAnalysisAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<SleepStageAnalysis>()

    inner class ViewHolder(val binding: ItemSleepStageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sleepStageAnalysis: SleepStageAnalysis) {
            binding.tvLevel.text = getLevel(sleepStageAnalysis.percentage)
            binding.tvType.text = sleepStageAnalysis.type
            val percentage = "${sleepStageAnalysis.percentage}%"
            val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(sleepStageAnalysis.timeInMinutes)

            val sleepString = if (hour == 0) {
                "${minute}min${if (minute > 1) "s" else ""}"
            } else {
                "${hour}hr ${minute}min${if (minute > 1) "s" else ""}"
            }
            binding.tvPercentage.text = percentage
            binding.tvTime.text = sleepString
            binding.ltProgress.pbSteps.progress = sleepStageAnalysis.percentage
            setTypeImage(binding, sleepStageAnalysis)

        }

    }

    fun getLevel(percent: Int): String {
        return when (percent) {
            in 0 until 34 -> "Low"
            in 34 until 67 -> "Moderate"
            in 67 until 100 -> "High"
            100 -> "High"
            else -> ""
        }

    }

    private fun setTypeImage(
        binding: ItemSleepStageLayoutBinding,
        sleepStageAnalysis: SleepStageAnalysis
    ) {
        when (sleepStageAnalysis.sleepType) {
            SleepType.AWAKE -> {
                binding.imvType.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.imvType.context,
                        R.drawable.ic_awake_sleep
                    )
                )

                binding.ltProgress.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.ltProgress.pbSteps.context, R.color.awake_start)
                )

            }
            SleepType.DEEP -> {
                binding.imvType.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.imvType.context,
                        R.drawable.ic_deep_sleep
                    )
                )
                binding.ltProgress.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.ltProgress.pbSteps.context, R.color.deep_start)
                )
            }
            SleepType.LIGHT -> {
                binding.imvType.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.imvType.context,
                        R.drawable.ic_light_sleep
                    )
                )
                binding.ltProgress.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.ltProgress.pbSteps.context, R.color.light_start)
                )
            }
            SleepType.REM -> {
                binding.imvType.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.imvType.context,
                        R.drawable.ic_rem_sleep
                    )
                )
                binding.ltProgress.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.ltProgress.pbSteps.context, R.color.rem_start)
                )
            }
            else -> {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSleepStageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    fun setData(data: ArrayList<SleepStageAnalysis>) {
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