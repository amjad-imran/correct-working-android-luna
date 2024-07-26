package com.oreo.ui.sleep2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowSleepAnalysisSummaryBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible


class SleepAnalysisAdapter :
    RecyclerView.Adapter<SleepAnalysisAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SleepAnalysisData>()

    inner class ViewHolder(val binding: RowSleepAnalysisSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sleepAnalysis: SleepAnalysisData) {

            binding.tvTitle.text = sleepAnalysis.name
            binding.ivMain.setImageResource(sleepAnalysis.icon)

            if (sleepAnalysis.currentValue == null || sleepAnalysis.avgValue == null) {
                binding.groupData.gone()
                binding.imageRight.visible()
            } else {
                binding.groupData.visible()
                binding.imageRight.gone()

                binding.tvPercent.text = "${sleepAnalysis.currentValue}"
                binding.tvPercentChange.text = "${sleepAnalysis.avgValue}%"

                if (sleepAnalysis.currentValue >= sleepAnalysis.avgValue) {
                    binding.ivState.setImageResource(R.drawable.ic_arrow_increase)
                } else {
                    binding.ivState.setImageResource(R.drawable.ic_arrow_decrease)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            RowSleepAnalysisSummaryBinding.inflate(
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

    fun setData(resultData: ArrayList<SleepAnalysisData>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }


}

data class SleepAnalysisData(
    val name: String,
    val icon: Int,
    val currentValue: Int? = null,
    val avgValue: Int? = null,
)
