package com.noisefit.ui.trophies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.trophies.DailyItem
import com.noisefit.luna.databinding.RowTrophyStepsMilestonesBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.trophies.TrophiesType
import com.noisefit_commans.utils.prettyCount
import com.noisefit_commans.models.Units

class TrophiesStepsMilestonesAdapter(val listener: StepsMilestonesAction) :
    RecyclerView.Adapter<TrophiesStepsMilestonesAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<DailyItem>()
    private var trophyType: TrophiesType? = null
    private var unit: Units = Units.METRIC

    inner class ViewHolder(val binding: RowTrophyStepsMilestonesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dailyItem: DailyItem) {

            if (trophyType == TrophiesType.STEPS) {
                if (dailyItem.isStepsAchieved == 1 && dailyItem.isStepsCollect == 1) {
                    binding.ivNotification.gone()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_milestones)
                } else if (dailyItem.isStepsAchieved == 1 && dailyItem.isStepsCollect == 0) {
                    binding.ivNotification.visible()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_milestones)
                } else {
                    binding.ivNotification.gone()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_milestones_dimmed)
                }
                binding.tvGoalShort.text = "${dailyItem.steps.prettyCount()}"
                binding.tvGoalData.text = "${dailyItem.steps.prettyCount()} Steps"
            } else {
                if (dailyItem.isDistanceAchieved == 1 && dailyItem.isDistanceCollect == 1) {
                    binding.ivNotification.gone()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_milestones)
                } else if (dailyItem.isDistanceAchieved == 1 && dailyItem.isDistanceCollect == 0) {
                    binding.ivNotification.visible()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_milestones)
                } else {
                    binding.ivNotification.gone()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_milestones_dimmed)
                }

                if (unit == Units.METRIC) {
                    binding.tvGoalShort.text = dailyItem.titleForKm.split(" ")[0]
                    binding.tvGoalData.text = dailyItem.titleForKm
                } else {
                    binding.tvGoalShort.text = dailyItem.titleForMile.split(" ")[0]
                    binding.tvGoalData.text = dailyItem.titleForMile
                }

            }

            binding.root.setOnClickListener {
                if (trophyType == TrophiesType.STEPS) {
                    if (dailyItem.isStepsAchieved == 1) {
                        listener.onTrophyClicked(dailyItem)
                    }
                } else {
                    if (dailyItem.isDistanceAchieved == 1) {
                        listener.onTrophyClicked(dailyItem)
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowTrophyStepsMilestonesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<DailyItem>, trophyType: TrophiesType) {
        this.trophyType = trophyType
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun setUnit(unit: Units) {
        this.unit = unit
    }
}

interface StepsMilestonesAction {
    fun onTrophyClicked(item: DailyItem)
}