package com.oreo.ui.chatGpt.summary

import android.content.Context
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
            val dispData = getDisplayDate(data.key, binding.tvType.context)

            binding.tvType.text = dispData.first
            binding.tvUnit.text = dispData.second
            binding.tvValue.text = "${data.value}"
        }
    }


    /**
     * Pair(name,unit)
     */
    fun getDisplayDate(key: String?, context: Context): Pair<String?, String?> {
        if (key == null) return Pair(null, null)

        when (key) {
            "activity_score" -> return Pair("Activity Score", "")
            "master_avg_hr" -> return Pair("Master Avg HR", "BPM")
            "master_avg_hrv" -> return Pair("Master Avg HRV", "MS")
            "master_deep" -> return Pair("Master Deep", "")
            "master_duration" -> return Pair("Master Duration", "")
            "master_mid_time" -> return Pair("Master Mid Time", "")
            "master_rem" -> return Pair("Master Rem", "")
            "next_period_date" -> return Pair("Next Period Date", "")
            "readiness_score" -> return Pair("Readiness Score", "")
            "skin_temp_dev" -> return Pair("Skin Temp Dev", "")
            "sleep_need" -> return Pair("Sleep Need", "")
            "sleep_score" -> return Pair("Sleep Score", "")

            else -> return Pair(null, null)
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