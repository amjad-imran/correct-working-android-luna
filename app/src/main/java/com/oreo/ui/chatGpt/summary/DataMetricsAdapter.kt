package com.oreo.ui.chatGpt.summary

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowAiSummaryBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.model.AiSummaryDataModel
import com.oreo.data.model.DataMetrics
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        if (data.value.isNullOrEmpty()) return null

        when (data.key) {
            DataMetricKeys.ACTIVITY_SCORE.key -> return AiSummaryDataModel(
                name = context.getString(R.string.text_activity_score).uppercase(),
                isDate = false,
                "",
                data.value
            )

            DataMetricKeys.AVG_HR.key -> return AiSummaryDataModel(
                name = context.getString(R.string.text_avg_hr).uppercase(),
                isDate = false,
                "BPM",
                data.value
            )

            DataMetricKeys.AVG_HRV.key -> return AiSummaryDataModel(
                name = context.getString(R.string.text_avg_hrv).uppercase(),
                isDate = false,
                "MS",
                data.value
            )

            DataMetricKeys.DEEP.key -> {
                val (hours, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    data.value.toFloatOrNull() ?: 0f
                )
                val text = if (hours == 0) {
                    "$minute min"
                } else {
                    "$hours hr $minute min"
                }
                return AiSummaryDataModel(
                    name = context.getString(R.string.text_deep).uppercase(),
                    isDate = false, "", text
                )
            }

            DataMetricKeys.DURATION.key -> {
                val (hours, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    data.value.toFloatOrNull() ?: 0f
                )
                val text = if (hours == 0) {
                    "$minute min"
                } else {
                    "$hours hr $minute min"
                }

                return AiSummaryDataModel(
                    name = context.getString(R.string.text_duration).uppercase(),
                    isDate = false,
                    "",
                    text
                )
            }

            DataMetricKeys.MID_TIME.key -> return AiSummaryDataModel(
                name = context.getString(R.string.text_mid_time).uppercase(),
                isDate = false,
                "",
                data.value
            )

            DataMetricKeys.REM.key-> {
                val (hours, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    data.value.toFloatOrNull() ?: 0f
                )
                val text = if (hours == 0) {
                    "$minute min"
                } else {
                    "$hours hr $minute min"
                }
                return AiSummaryDataModel(
                    name = context.getString(R.string.text_rem).uppercase(), isDate = false, "",
                    text
                )
            }

            DataMetricKeys.NEXT_PERIOD_DATE.key-> {
                val formattedDate = try {
                    LocalDate.parse(data.value).format(DateTimeFormatter.ofPattern("dd/MM/yy"))
                } catch (exp: Exception) {
                    ""
                }
                return AiSummaryDataModel(
                    name = context.getString(R.string.text_next_period_date).uppercase(),
                    isDate = true,
                    "",
                    formattedDate
                )
            }

            DataMetricKeys.READINESS_SCORE.key -> return AiSummaryDataModel(
                name = context.getString(R.string.text_readiness_score).uppercase(),
                isDate = false,
                "",
                data.value
            )

            DataMetricKeys.SKIN_TEMP_DEV.key-> return AiSummaryDataModel(
                name = context.getString(R.string.tex_skin_temp_dev).uppercase(),
                isDate = false,
                "C",
                data.value
            )

            DataMetricKeys.SLEEP_NEED.key-> {
                val (hours, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    data.value.toFloatOrNull() ?: 0f
                )
                val text = if (hours == 0) {
                    "$minute min"
                } else {
                    "$hours hr $minute min"
                }
                return AiSummaryDataModel(
                    name = context.getString(R.string.text_sleep_need).uppercase(),
                    isDate = false,
                    "",
                    text
                )
            }

            DataMetricKeys.SLEEP_SCORE.key -> return AiSummaryDataModel(
                name = context.getString(R.string.text_sleep_score).uppercase(), isDate = false, "",
                data.value
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
        val keys = DataMetricKeys.entries.map {
            it.key
        }

        val filteredData = dataSet.filter {
            it.key in keys
        }

        mDataSet.clear()
        mDataSet.addAll(filteredData)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }


}

enum class DataMetricKeys(val key: String) {
    ACTIVITY_SCORE("activity_score"),
    AVG_HR("master_avg_hr"),
    AVG_HRV("master_avg_hrv"),
    DEEP("master_deep"),
    DURATION("master_duration"),
    MID_TIME("master_mid_time"),
    REM("master_rem"),
    NEXT_PERIOD_DATE("next_period_date"),
    READINESS_SCORE("readiness_score"),
    SKIN_TEMP_DEV("skin_temp_dev"),
    SLEEP_NEED("sleep_need"),
    SLEEP_SCORE("sleep_score")
}