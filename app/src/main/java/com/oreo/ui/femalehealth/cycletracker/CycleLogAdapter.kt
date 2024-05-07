package com.oreo.ui.femalehealth.cycletracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemCycleLogBinding
import com.oreo.data.model.FlowLog

class CycleLogAdapter() :
    RecyclerView.Adapter<CycleLogAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<FlowLog>()

    inner class ViewHolder(val binding: ItemCycleLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FlowLog) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemCycleLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: List<FlowLog>?) {
        mDataSet.clear()
        if (resultData != null) {
            mDataSet.addAll(resultData)
        }
        notifyDataSetChanged()
    }


}

