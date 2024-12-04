package com.oreo.ui.chatGpt.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowAiSummaryBinding
import com.oreo.data.model.DataMetrics

class DataMetricsAdapter : RecyclerView.Adapter<DataMetricsAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<DataMetrics>()

    inner class ViewHolder(val binding: RowAiSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataMetrics) {
            binding.tvType.text = data.name
            binding.tvUnit.text = data.unit
            binding.tvValue.text = "${data.value}"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataMetricsAdapter.ViewHolder {
        return ViewHolder(
            RowAiSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<DataMetrics>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }


}