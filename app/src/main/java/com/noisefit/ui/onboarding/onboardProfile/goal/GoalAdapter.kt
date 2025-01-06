package com.noisefit.ui.onboarding.onboardProfile.goal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.GoalModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowGoalBinding

class GoalAdapter : RecyclerView.Adapter<GoalAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<GoalModel>()


    inner class ViewHolder(val binding: RowGoalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoalModel) {
            binding.tvName.text = data.text

            if (data.isSelected) {
                binding.ivCheck.setImageResource(R.drawable.ic_goal_check)
            } else {
                binding.ivCheck.setImageResource(R.drawable.ic_goal_uncheck)
            }

            binding.root.setOnClickListener {
                data.isSelected = data.isSelected.not()
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(data: List<GoalModel>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    fun getSelectedGoals(): List<GoalModel> {
        return mDataSet.filter {
            it.key != null && it.isSelected
        }
    }
}