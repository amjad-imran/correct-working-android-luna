package com.oreo.ui.stress.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemStressUnderstandingSubListBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.StressUnderstandingSubList


class StressUnderstandingSubAdapter() :
    RecyclerView.Adapter<StressUnderstandingSubAdapter.ViewHolder>() {


    private var mDataSet = ArrayList<String>()

    inner class ViewHolder(val binding: ItemStressUnderstandingSubListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String, position: Int) {

            binding.tvContent.text = data
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStressUnderstandingSubListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }
}