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
            "activity_score" -> return AiSummaryDataModel(
                name = context.getString(R.string.text_activity_score).uppercase(),
                isDate = false,
                "",
                data.value
            )

            "master_avg_hr" -> return AiSummaryDataModel(
                name = context.getString(R.string.text_avg_hr).uppercase(),
                isDate = false,
                "BPM",
                data.value
            )

            "master_avg_hrv" -> return AiSummaryDataModel(
                name = context.getString(R.string.text_avg_hrv).uppercase(),
                isDate = false,
                "MS",
                data.value
            )

            "master_deep" -> {
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

            "master_duration" -> {
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

            "master_mid_time" -> return AiSummaryDataModel(
                name = context.getString(R.string.text_mid_time).uppercase(),
                isDate = false,
                "",
                data.value
            )

            "master_rem" -> {
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

            "next_period_date" -> {
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

            "readiness_score" -> return AiSummaryDataModel(
                name = context.getString(R.string.text_readiness_score).uppercase(),
                isDate = false,
                "",
                data.value
            )

            "skin_temp_dev" -> return AiSummaryDataModel(
                name = context.getString(R.string.tex_skin_temp_dev).uppercase(),
                isDate = false,
                "C",
                data.value
            )

            "sleep_need" -> {
                val (hours, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    data.value.toFloatOrNull() ?: 0f
                )
                val text = if (hours == 0) {
                    "$minute min"
                } else {
                    "$hours hr $minute min"
                }
                return AiSummaryDataModel(
                    name = context.getString(R.string.text_sleep_need).uppercase(), isDate = false, "",
                    text
                )
            }

            "sleep_score" -> return AiSummaryDataModel(
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
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }


}