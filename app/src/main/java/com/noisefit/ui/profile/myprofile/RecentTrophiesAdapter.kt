package com.noisefit.ui.profile.myprofile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.trophies.TrophyBadge
import com.noisefit.luna.databinding.RowRecentTrophiesBinding
import com.noisefit_commans.utils.prettyCount
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats

class RecentTrophiesAdapter(val listener: RecentTrophyAction) :
    RecyclerView.Adapter<RecentTrophiesAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<TrophyBadge>()
    private var unit: Units = Units.METRIC
    private var viewWidth:Int=0

    inner class ViewHolder(val binding: RowRecentTrophiesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trophyBadge: TrophyBadge) {
            if (trophyBadge.activityType.equals("steps", true)) {
                if (trophyBadge.badgeType.equals("streak", true)) {
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_step_streaks)
                } else {//Else a badge
                    if (trophyBadge.badge.badgeType.equals("daily", true)) {
                        binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_milestones)
                    } else {
                        binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_lifetime)
                    }
                }
                binding.tvType.text = "${trophyBadge.badge.steps.prettyCount()} Steps"
                binding.tvDaysAgo.text = DateFormats.calculateDayDifference(
                    DateFormats.getCurrentDate(),
                    trophyBadge.createdAt
                )
                binding.parentContainer.layoutParams.width=viewWidth
            } else {
                if (trophyBadge.badgeType.equals("streak", true)) {
                    binding.ivTrophyImage.setImageResource(R.drawable.ic_step_streaks)
                } else {//Else a badge
                    if (trophyBadge.badge.badgeType.equals("daily", true)) {
                        binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_milestones)
                    } else {
                        binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_lifettime)
                    }
                }
                if (unit == Units.METRIC) {
                    binding.tvType.text = trophyBadge.badge.titleForKm
                } else {
                    binding.tvType.text = trophyBadge.badge.titleForMile
                }

                binding.tvDaysAgo.text = DateFormats.calculateDayDifference(
                    DateFormats.getCurrentDate(),
                    trophyBadge.createdAt
                )
                binding.parentContainer.layoutParams.width=viewWidth
            }
            binding.root.setOnClickListener {
                listener.onTrophyClicked(trophyBadge)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowRecentTrophiesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(it: List<TrophyBadge>) {
        mDataSet.clear()
        mDataSet.addAll(it)
        notifyDataSetChanged()
    }

    fun setUnit(unit: Units) {
        this.unit = unit
    }

    fun getDeviceWidth(widthValue:Int) {
        this.viewWidth=widthValue
    }

}

interface RecentTrophyAction {
    fun onTrophyClicked(trophyBadge: TrophyBadge)
}