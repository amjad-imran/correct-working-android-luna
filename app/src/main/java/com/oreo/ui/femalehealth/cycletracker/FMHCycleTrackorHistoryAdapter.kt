package com.oreo.ui.femalehealth.cycletracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.FmhCycleHistoryItemBinding
import com.noisefit.luna.databinding.FmhCycleTrackHistoryItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.FMHCycleHistoryDataModel

class FMHCycleTrackorHistoryAdapter() :
    RecyclerView.Adapter<FMHCycleTrackorHistoryAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<FMHCycleHistoryDataModel>()

    inner class ViewHolder(val binding: FmhCycleTrackHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FMHCycleHistoryDataModel) {
            binding.tvHeader.text = "Current cycle: ${data.cycleLength} days"
            binding.tvStartedOn.text = "Started on ${data.startDate}"

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            FmhCycleTrackHistoryItemBinding.inflate(
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

    fun setData(resultData: List<FMHCycleHistoryDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }


}

