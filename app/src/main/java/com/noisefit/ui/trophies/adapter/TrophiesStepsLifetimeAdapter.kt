package com.noisefit.ui.trophies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.model.trophies.DailyItem
import com.noisefit.databinding.RowTrophyStepsMilestonesBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.trophies.TrophiesType
import com.noisefit_commans.utils.prettyCount
import com.noisefit_commans.models.Units

class TrophiesStepsLifetimeAdapter(val listener :StepsLifetimeAction) :
    RecyclerView.Adapter<TrophiesStepsLifetimeAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<DailyItem>()
    private var trophyType: TrophiesType? = null
    private var unit: Units = Units.METRIC

    inner class ViewHolder(val binding: RowTrophyStepsMilestonesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DailyItem) {

            if (trophyType == TrophiesType.STEPS) {
                if (item.isStepsAchieved == 1 && item.isStepsCollect==1) {
                    binding.ivNotification.gone()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_lifetime)
                }else if(item.isStepsAchieved==1 && item.isStepsCollect==0){
                    binding.ivNotification.visible()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_lifetime)
                } else {
                    binding.ivNotification.gone()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_lifetime_dimmed)
                }
                binding.tvGoalShort.text = "${item.steps.prettyCount()}"
                binding.tvGoalData.text = "${item.steps.prettyCount()} Steps"
            } else {
                if (item.isDistanceAchieved == 1 && item.isDistanceCollect==1) {
                    binding.ivNotification.gone()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_lifettime)
                }else if(item.isDistanceAchieved==1 && item.isDistanceCollect==0){
                    binding.ivNotification.visible()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_lifettime)
                } else {
                    binding.ivNotification.gone()
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_lifettime_dimmed)
                }


                if (unit == Units.METRIC) {
                    binding.tvGoalShort.text = item.titleForKm.split(" ")[0]
                    binding.tvGoalData.text = item.titleForKm
                } else {
                    binding.tvGoalShort.text = item.titleForMile.split(" ")[0]
                    binding.tvGoalData.text = item.titleForMile
                }
            }

            binding.root.setOnClickListener {
                if (trophyType == TrophiesType.STEPS) {
                    if(item.isStepsAchieved==1){
                        listener.onTrophyClicked(item)
                    }
                }else{
                    if(item.isDistanceAchieved==1){
                        listener.onTrophyClicked(item)
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

interface StepsLifetimeAction{
    fun onTrophyClicked(item : DailyItem)
}