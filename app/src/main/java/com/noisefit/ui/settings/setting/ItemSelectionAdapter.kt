package com.noisefit.ui.settings.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowScrollValueSelectorBinding

class ItemSelectionAdapter: RecyclerView.Adapter<ItemSelectionAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()

    inner class ViewHolder(private val binding: RowScrollValueSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: String) {
            binding.tvValue.text = value


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
    fun setDataSet(dataList: ArrayList<String>) {
        mDataSet = dataList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size
}