package com.oreo.ui.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowRingCareBinding
import com.noisefit.luna.databinding.RowScrollValueSelectorBinding
import com.oreo.data.model.RingCare

class RingCareAdapter : RecyclerView.Adapter<RingCareAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<RingCare>()

    inner class ViewHolder(private val binding: RowRingCareBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data:RingCare) {

            binding.tvTitle.text = data.title

            binding.rvPoints.layoutManager = LinearLayoutManager(binding.rvPoints.context)
            binding.rvPoints.adapter = RingCarePointsAdapter().apply {
                this.setDataSet(data.content)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowRingCareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<RingCare>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }
}