package com.noisefit.ui.onboarding.onboardProfile.goals

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowScrollValueSelectorBinding

class StepGoalsSelectionAdapter : RecyclerView.Adapter<StepGoalsSelectionAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<Int>()
    private var mSelectedPosition = -1


    inner class ViewHolder(private val binding: RowScrollValueSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.tvValue.text = "$position steps"

            if (mSelectedPosition == bindingAdapterPosition) {
                binding.tvValue.setTextColor(Color.parseColor("#eeeeee"))
            } else {
                binding.tvValue.setTextColor(Color.parseColor("#a6a6a6"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowScrollValueSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: ArrayList<Int>) {
        mDataSet = dataList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setSelectedPosition(layoutPosition: Int) {
        mSelectedPosition = layoutPosition
        notifyDataSetChanged()
    }



}