package com.oreo.ui.femalehealth.cycletracker.log.bottom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemSymptomDayLogListBinding
import com.noisefit_commans.ui.loadImage


class SymptomDayLogAdapter :
    RecyclerView.Adapter<SymptomDayLogAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Pair<String, String>>()

    inner class ViewHolder(val binding: ItemSymptomDayLogListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Pair<String, String>) {
            binding.title.text = data.second
            binding.icon.loadImage(binding.icon.context, data.first)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemSymptomDayLogListBinding.inflate(
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

    fun setData(resultData: List<Pair<String, String>>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }


}



