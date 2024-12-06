package com.oreo.ui.chatGpt.functions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowSubMealBinding

class SubMealAdapter(val mDataSet: List<String>) :
    RecyclerView.Adapter<SubMealAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowSubMealBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.tvTitle.text = "Green Apple"
            binding.tvSubTitle.text = "1 medium size"
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