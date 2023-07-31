package com.noisefit.ui.common.bottomSheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowScrollValueSelectorBinding


class ValueSelectorAdapter : RecyclerView.Adapter<ValueSelectorAdapter.ViewHolder>() {

    private var mDataSet = java.util.ArrayList<String>()


    inner class ViewHolder(private val binding: RowScrollValueSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: String) {
            binding.tvValue.text = position
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

    fun setDataSet(dataSet: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = mDataSet.size


}