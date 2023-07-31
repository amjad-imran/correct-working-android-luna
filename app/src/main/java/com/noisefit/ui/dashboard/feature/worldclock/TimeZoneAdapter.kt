package com.noisefit.ui.dashboard.feature.worldclock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.response.WorldClockNetwork
import com.noisefit.luna.databinding.RowTimeZoneBinding

class TimeZoneAdapter(val listener: TimeZoneListAction) :
    RecyclerView.Adapter<TimeZoneAdapter.ViewHolder>() {

    var mDataSet = ArrayList<WorldClockNetwork>()

    inner class ViewHolder(val binding: RowTimeZoneBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(worldClockNetwork: WorldClockNetwork) {
            binding.tvTimeZoneN.text = worldClockNetwork.city

            binding.root.setOnClickListener {
                listener.onZoneSelected(worldClockNetwork)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowTimeZoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<WorldClockNetwork>?) {
        mDataSet = dataSet as ArrayList<WorldClockNetwork>
        notifyDataSetChanged()
    }
}

interface TimeZoneListAction {
    fun onZoneSelected(clock: WorldClockNetwork)
}