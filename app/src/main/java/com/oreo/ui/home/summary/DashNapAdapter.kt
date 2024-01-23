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
import java.lang.StringBuilder

class DashNapAdapter(
    private val napList: List<Nap>,
    private val date: String,
    private val showWhite: Boolean = false
) :
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
                binding.ivSleepSeperator.visible()
                val sleepImpactScore = "+${nap.sleepScoreImpact.toString()}"
                binding.tvSleepScoreChange.text = sleepImpactScore
                if (!showWhite) {
                    binding.tvSleepScoreChange.setTextColor(binding.ivSleep.context.getColor(R.color.steps_arc))
                }
            } else {
                if (nap.sleepScoreImpact == null || nap.sleepScoreImpact == 0) {
                    binding.ivSleep.gone()
                    binding.ivSleepSeperator.gone()
                    binding.tvSleepScoreChange.gone()
                } else {
                    binding.ivSleep.visible()
                    binding.ivSleepSeperator.visible()
                    binding.tvSleepScoreChange.text = nap.sleepScoreImpact.toString()
                    if (!showWhite) {
                        binding.tvSleepScoreChange.setTextColor(binding.ivSleep.context.getColor(R.color.nap_dash_sleep_impact_score))
                    }
                }
            }
            if ((nap.readinessScoreImpact ?: 0) > 0) {
                binding.ivReadiness.visible()
                binding.ivReadinessSeparator.visible()
                val readinessImpactScore = "+${nap.readinessScoreImpact.toString()}"
                binding.tvReadinessScoreChange.text = readinessImpactScore
                if (!showWhite) {
                    binding.tvReadinessScoreChange.setTextColor(
                        binding.ivReadiness.context.getColor(
                            R.color.steps_arc
                        )
                    )
                }
            } else {
                if (nap.readinessScoreImpact == null || nap.readinessScoreImpact == 0) {
                    binding.ivReadiness.gone()
                    binding.ivReadinessSeparator.gone()
                    binding.tvReadinessScoreChange.gone()
                } else {
                    binding.ivReadiness.visible()
                    binding.ivReadinessSeparator.visible()
                    binding.tvReadinessScoreChange.text = nap.readinessScoreImpact.toString()
                    if (!showWhite) {
                        binding.tvReadinessScoreChange.setTextColor(
                            binding.ivSleep.context.getColor(
                                R.color.nap_dash_sleep_impact_score
                            )
                        )
                    }
                }
            }


            val timeBuilder = StringBuilder()
            if (!nap.date.equals(date)) {
                timeBuilder.append("Yesterday ")
            }
            timeBuilder.append(
                DateFormats.parseDate(
                    nap.startTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.timeFormat12
                )?.lowercase()
            )

            binding.tvStartTime.text = timeBuilder.toString()


            if (bindingAdapterPosition == (napList.size - 1)) {
                binding.divider.root.gone()
            } else {
                binding.divider.root.visible()
            }
            binding.root.setOnClickListener {
                if (nap.sleepScoreImpact == null || nap.readinessScoreImpact == null) return@setOnClickListener
                if (nap.sleepScoreImpact == 0 && nap.readinessScoreImpact == 0) return@setOnClickListener

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