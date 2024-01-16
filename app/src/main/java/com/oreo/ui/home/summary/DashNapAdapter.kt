package com.oreo.ui.home.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowNapDashBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.health.Nap

class DashNapAdapter(private val napList: List<Nap>) :
    RecyclerView.Adapter<DashNapAdapter.ViewHolder>() {
    var listener: OnNapSelectedAction? = null

    inner class ViewHolder(val binding: RowNapDashBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(nap: Nap) {

            val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(nap.duration ?: 0)
            binding.tvDuration.text = if (hour == 0) {
                "$minute m"
            } else {
                "$hour h $minute m"
            }
            if ((nap.sleepScoreImpact ?: 0) > 0) {
                binding.ivSleep.visible()
                val sleepImpactScore = "+${nap.sleepScoreImpact.toString()}"
                binding.tvSleepScoreChange.text = sleepImpactScore
                binding.tvSleepScoreChange.setTextColor(binding.ivSleep.context.getColor(R.color.steps_arc))
            } else {
                if (nap.sleepScoreImpact == null || nap.sleepScoreImpact == 0) {
                    binding.ivSleep.gone()
                    binding.tvSleepScoreChange.gone()
                } else {
                    binding.ivSleep.visible()
                    binding.tvSleepScoreChange.text = nap.sleepScoreImpact.toString()
                    binding.tvSleepScoreChange.setTextColor(binding.ivSleep.context.getColor(R.color.nap_dash_sleep_impact_score))
                }
            }
            if ((nap.readinessScoreImpact ?: 0) > 0) {
                binding.ivReadiness.visible()
                val readinessImpactScore = "+${nap.readinessScoreImpact.toString()}"
                binding.tvReadinessScoreChange.text = readinessImpactScore
                binding.tvReadinessScoreChange.setTextColor(binding.ivReadiness.context.getColor(R.color.steps_arc))
            } else {
                if (nap.readinessScoreImpact == null || nap.readinessScoreImpact == 0) {
                    binding.ivReadiness.gone()
                    binding.tvReadinessScoreChange.gone()
                } else {
                    binding.ivReadiness.visible()
                    binding.tvReadinessScoreChange.text = nap.readinessScoreImpact.toString()
                    binding.tvReadinessScoreChange.setTextColor(binding.ivSleep.context.getColor(R.color.nap_dash_sleep_impact_score))
                }
            }

            //TODO show yesterday also
            binding.tvStartTime.text = DateFormats.parseDate(
                nap.startTime,
                DateFormats.dateTimeFormat5,
                DateFormats.timeFormat12
            )


            if (bindingAdapterPosition == (napList.size - 1)) {
                binding.divider.root.gone()
            } else {
                binding.divider.root.visible()
            }
            binding.root.setOnClickListener {
                listener?.onNapSelected(nap.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowNapDashBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = napList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(napList[position])
    }

    fun setOnNapSelectedListener(listener: OnNapSelectedAction) {
        this.listener = listener
    }


}

interface OnNapSelectedAction {
    fun onNapSelected(napId: String)
}