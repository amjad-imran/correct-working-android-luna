package com.oreo.ui.chatGpt.functions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.AiMeal
import com.noisefit.luna.databinding.RowSubMealBinding

class SubMealAdapter(val mDataSet: List<AiMeal>) :
    RecyclerView.Adapter<SubMealAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowSubMealBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AiMeal) {
            binding.tvTitle.text = data.meal_name
            binding.tvSubTitle.text = data.portion
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowSubMealBinding.inflate(
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
}