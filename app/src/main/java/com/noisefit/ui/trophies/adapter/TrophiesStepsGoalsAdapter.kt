package com.noisefit.ui.trophies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.trophies.StreaksItem
import com.noisefit.luna.databinding.RowTrophyStepsGoalBinding
import com.noisefit.ui.trophies.TrophiesType

class TrophiesStepsGoalsAdapter :
    RecyclerView.Adapter<TrophiesStepsGoalsAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<StreaksItem>()
    private var trophyType: TrophiesType? = null

    inner class ViewHolder(val binding: RowTrophyStepsGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StreaksItem) {
            binding.tvStreakShort.text = "${item.noOfDays}"
            binding.tvStreak.text = "${item.title}"

            if(item.isCollected){
                binding.ivTrophyImage.setImageResource(R.drawable.ic_step_streaks)
            }else{
                binding.ivTrophyImage.setImageResource(R.drawable.ic_step_streaks_dimmed)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowTrophyStepsGoalBinding.inflate(
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

    fun setDataSet(dataSet: List<StreaksItem>, trophyType: TrophiesType) {
        this.trophyType = trophyType
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}