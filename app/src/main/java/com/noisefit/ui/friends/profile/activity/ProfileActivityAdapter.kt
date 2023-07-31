package com.noisefit.ui.friends.profile.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.model.ProfileActivitiesData
import com.noisefit.databinding.ItemProfileActivityListBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.graph.StepsChartUtils
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.prettyCount
import com.noisefit_commans.utils.prettyCountDecimal


class ProfileActivityAdapter : RecyclerView.Adapter<ProfileActivityAdapter.ViewHolder>() {

    val mDataSet = ArrayList<ProfileActivitiesData>()

    inner class ViewHolder(val binding: ItemProfileActivityListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ProfileActivitiesData) {
            binding.tvCompareName.text = data.compareName

            if (data.total.toDouble() > 0)
                binding.tvCompareTime.visible()
            else
                binding.tvCompareTime.gone()

            binding.tvCompareTime.text = DateFormats.formatDateTime(
                data.compareDate,
                DateFormats.dateFormat3,
                DateFormats.dateTimeFormatWithWeekInit
            )
            if (data.betterThen.isNullOrEmpty()) {
                binding.tvDoingBetter.gone()
            } else {
                binding.tvDoingBetter.visible()
                binding.tvDoingBetter.text = data.betterThen
            }



            binding.tvTitle.text = data.title
            val color: Int
            when (data.type) {
                HealthOverViewHistoryType.Steps -> {
                    val unit = "Steps"
                    binding.tvCompareValue.text = data.max.toLong().prettyCount()
                    binding.tvValueAvg.text = data.avg.toLong().prettyCount()
                    binding.tvValue.text = data.total.toLong().prettyCount()
                    color = R.color.steps_color
                    setCompareColor(binding, color)
                    binding.tvValueAvgUnit.text = unit
                    binding.tvValueUnit.text = unit
                    binding.tvCompareValueUnit.text = unit
                }
                HealthOverViewHistoryType.Calories -> {
                    val unit = "kcal"
                    binding.tvCompareValue.text = data.max.toLong().prettyCount()
                    binding.tvValue.text = data.total.toLong().prettyCount()
                    binding.tvValueAvg.text = data.avg.toLong().prettyCount()
                    color = R.color.calories_color
                    setCompareColor(binding, color)
                    binding.tvValueAvgUnit.text = unit
                    binding.tvValueUnit.text = unit
                    binding.tvCompareValueUnit.text = unit
                }
                HealthOverViewHistoryType.Distance -> {
                    val unit = "kms"
                    color = R.color.distance_color
                    setCompareColor(binding, color)
                    binding.tvValueAvgUnit.text = unit
                    binding.tvValueUnit.text = unit
                    binding.tvCompareValue.text = data.max.toDouble().prettyCountDecimal()
                    binding.tvValueAvg.text = data.avg.toDouble().prettyCountDecimal()
                    binding.tvValue.text = data.total.toDouble().prettyCountDecimal()
                    binding.tvCompareValueUnit.text = unit
                }
            }

            StepsChartUtils.setChart(
                color,
                binding.barChart,
                GraphInterval.WEEK,
                0,
                data.weeklyData
            )

            binding.barChart.post(Runnable() {

                data.graphData?.let {
                    StepsChartUtils.setChartData(
                        it.first,
                        binding.barChart,
                        it.second,
                        it.third,
                        binding.barChart.measuredWidth.toFloat()
                    )
                }
            })


        }
    }


    private fun setCompareColor(binding: ItemProfileActivityListBinding, color: Int) {
        binding.tvCompareValue.setTextColor(
            binding.tvCompareValue.resources.getColor(
                color,
                null
            )
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemProfileActivityListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<ProfileActivitiesData>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }
}