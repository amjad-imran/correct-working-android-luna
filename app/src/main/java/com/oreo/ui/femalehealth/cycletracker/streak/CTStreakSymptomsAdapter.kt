package com.oreo.ui.femalehealth.cycletracker.streak

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.SymptomsRecordItemBinding
import com.noisefit_commans.ui.loadImage

class CTStreakSymptomsAdapter :
    RecyclerView.Adapter<CTStreakSymptomsAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Pair<String, String>>()


    inner class ViewHolder(val binding: SymptomsRecordItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Pair<String, String>) {
            binding.tvTitle.text = data.second
            binding.ivSymptoms.loadImage(binding.ivSymptoms.context, data.first)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            SymptomsRecordItemBinding.inflate(
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



