package com.noisefit.ui.onboarding.onboardProfile.userDetails

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowScrollValueSelectorBinding
import com.noisefit_commans.models.WeightUnitSystem

class WeightSelectionAdapter : RecyclerView.Adapter<WeightSelectionAdapter.ViewHolder>() {

    private var mDataSet = java.util.ArrayList<Int>()
    private var weightUnitSystem = WeightUnitSystem.METRIC
    private var mSelectedPosition = -1


    inner class ViewHolder(private val binding: RowScrollValueSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val text =
                "$position ${if (weightUnitSystem == WeightUnitSystem.METRIC) "kg" else "lbs"}"
            binding.tvValue.text = text

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

    fun setDataSet(dataList: ArrayList<Int>, weightUnitSystem: WeightUnitSystem) {
        mDataSet = dataList
        this.weightUnitSystem = weightUnitSystem
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setSelectedPosition(layoutPosition: Int) {
        mSelectedPosition = layoutPosition
        notifyDataSetChanged()
    }


}