package com.oreo.ui.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowCarePointsBinding

class RingCarePointsAdapter : RecyclerView.Adapter<RingCarePointsAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<String>()

    inner class ViewHolder(private val binding: RowCarePointsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(s: String) {
            binding.tvPoint.text = s

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowCarePointsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }
}