package com.oreo.ui.chatGpt.functions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowAiWorkoutBinding
import com.noisefit.luna.databinding.RowNutrientBinding

class NutrientsAdapter() :
    RecyclerView.Adapter<NutrientsAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<String>()

    inner class ViewHolder(val binding: RowNutrientBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.tvNutrient.text = "Calories : 1200kcal"
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowNutrientBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }


}