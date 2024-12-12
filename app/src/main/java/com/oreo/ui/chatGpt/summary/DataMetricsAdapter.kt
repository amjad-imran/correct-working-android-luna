package com.oreo.ui.chatGpt.summary

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowAiSummaryBinding
import com.oreo.data.model.AiSummaryDataModel
import com.oreo.data.model.DataMetrics

class DataMetricsAdapter : RecyclerView.Adapter<DataMetricsAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<DataMetrics>()

    inner class ViewHolder(val binding: RowAiSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataMetrics) {

            val dataParsed = getDisplayDate(data, binding.tvType.context)

            binding.tvType.text = dataParsed?.name ?: ""
            binding.tvUnit.text = dataParsed?.unit ?: ""
            binding.tvValue.text = dataParsed?.value ?: ""
        }
    }


    /**
     * Pair(name,unit)
     */
    fun getDisplayDate(data: DataMetrics, context: Context): AiSummaryDataModel? {
        if (data.value == null && data.value1 == null) return null

        when (data.key) {
            "activity_score" -> return AiSummaryDataModel(
                name = "Activity Score",
                isDate = false,
                "",
                ""
            )

            "master_avg_hr" -> return AiSummaryDataModel(
                name = "Master Avg HR",
                isDate = false,
                "BPM",
                ""
            )

            "master_avg_hrv" -> return AiSummaryDataModel(
                name = "Master Avg HRV",
                isDate = false,
                "MS",
                ""
            )

            "master_deep" -> return AiSummaryDataModel(name = "Master Deep", isDate = false, "","")
            "master_duration" -> return AiSummaryDataModel(
                name = "Master Duration",
                isDate = false,
                "",
                ""
            )

            "master_mid_time" -> return AiSummaryDataModel(
                name = "Master Mid Time",
                isDate = false,
                "",
                ""
            )

            "master_rem" -> return AiSummaryDataModel(
                name = "Master Rem", isDate = false, "",
                ""
            )

            "next_period_date" -> return AiSummaryDataModel(
                name = "Next Period Date",
                isDate = true,
                "",
                "${data.value1}"
            )

            "readiness_score" -> return AiSummaryDataModel(
                name = "Readiness Score",
                isDate = false,
                "",
                ""
            )

            "skin_temp_dev" -> return AiSummaryDataModel(
                name = "Skin Temp Dev", isDate = false, "",
                ""
            )

            "sleep_need" -> return AiSummaryDataModel(
                name = "Sleep Need", isDate = false, "",
                ""
            )

            "sleep_score" -> return AiSummaryDataModel(
                name = "Sleep Score", isDate = false, "",
                ""
            )

            else -> return null
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