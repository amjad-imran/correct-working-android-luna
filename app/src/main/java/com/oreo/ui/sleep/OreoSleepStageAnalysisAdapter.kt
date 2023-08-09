package com.oreo.ui.sleep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.OreoItemSleepStageLayoutBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.SleepStageAnalysis
import com.noisefit_commans.models.SleepType
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class OreoSleepStageAnalysisAdapter :
    RecyclerView.Adapter<OreoSleepStageAnalysisAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SleepStageAnalysis>()

    inner class ViewHolder(val binding: OreoItemSleepStageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sleepStageAnalysis: SleepStageAnalysis) {
            binding.tvType.text = sleepStageAnalysis.type
            if (sleepStageAnalysis.timeInMinutes == -1) {
                binding.tvPercentage.gone()
                binding.tvLevel.gone()
                binding.tvTime.gone()
            }else{
                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(sleepStageAnalysis.timeInMinutes)
                val sleepString = if (hour == 0) {
                    "${minute}min"
                } else {
                    "${hour}hr ${minute}min"
                }
                val percentage = "${sleepStageAnalysis.percentage}%"
                binding.tvLevel.text = getLevel(sleepStageAnalysis.percentage)
                binding.tvPercentage.text = percentage
                binding.tvTime.text = sleepString
                binding.tvLevel.visible()
                binding.tvPercentage.visible()
                binding.tvTime.visible()
            }

            binding.pbSteps.progress = sleepStageAnalysis.percentage

            setTypeImage(binding, sleepStageAnalysis)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoItemSleepStageLayoutBinding.inflate(
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
        binding: OreoItemSleepStageLayoutBinding,
        sleepStageAnalysis: SleepStageAnalysis
    ) {
        when (sleepStageAnalysis.sleepType) {
            SleepType.AWAKE -> {
                binding.imvType.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.imvType.context,
                        R.drawable.ic_awake_sleep_oreo
                    )
                )

                binding.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.pbSteps.context, R.color.white)
                )

            }

            SleepType.DEEP -> {
                binding.imvType.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.imvType.context,
                        R.drawable.ic_deep_sleep_oreo
                    )
                )
                binding.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.pbSteps.context, R.color.deep_start_oreo)
                )
            }

            SleepType.LIGHT -> {
                binding.imvType.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.imvType.context,
                        R.drawable.ic_light_sleep_oreo
                    )
                )
                binding.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.pbSteps.context, R.color.light_start_oreo)
                )
            }

            SleepType.REM -> {
                binding.imvType.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.imvType.context,
                        R.drawable.ic_rem_sleep_oreo
                    )
                )
                binding.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(binding.pbSteps.context, R.color.rem_start_oreo)
                )
            }

            else -> {}
        }
    }

}

