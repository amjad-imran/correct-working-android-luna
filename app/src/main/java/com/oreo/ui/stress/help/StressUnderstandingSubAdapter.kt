package com.oreo.ui.stress.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemStressUnderstandingSubListBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.StressUnderstandingSubList


class StressUnderstandingSubAdapter() :
    RecyclerView.Adapter<StressUnderstandingSubAdapter.ViewHolder>() {


    private var mDataSet = ArrayList<StressUnderstandingSubList>()

    inner class ViewHolder(val binding: ItemStressUnderstandingSubListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StressUnderstandingSubList, position: Int) {

            binding.tvTitle.text = data.title
            binding.imv.loadImage(binding.imv.context,data.image)
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
    fun setDataSet(list: List<StressUnderstandingSubList>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }
}