package com.oreo.ui.home.summary

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.github.mikephil.charting.data.CombinedData
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ListActivityBurnCardItemBinding
import com.noisefit.luna.databinding.ListHeartRateCardItemBinding
import com.noisefit.luna.databinding.ListOHeaderCardItemBinding
import com.noisefit.luna.databinding.ListOPairDeviceBinding
import com.noisefit.luna.databinding.ListOWAlertCardItemBinding
import com.noisefit.luna.databinding.ListOreoBatteryPercentItemBinding
import com.noisefit.luna.databinding.ListReadinessCardItemBinding
import com.noisefit.luna.databinding.ListReadinessScoreCardItemBinding
import com.noisefit.luna.databinding.ListSleepActivityCardItemBinding
import com.noisefit.luna.databinding.ListSleepCardItemBinding
import com.noisefit.luna.databinding.OreoDummyViewBinding
import com.noisefit.luna.databinding.OreoLayoutRecentActivityBinding
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.custom.SleepProgressbarView
import com.noisefit_commans.ui.getColor
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.util.graph.OCombineChartUtils

sealed class OSummaryHealthOverviewClickEnum {
    object MeasureHRClick : OSummaryHealthOverviewClickEnum()
    object PairDeviceClicked : OSummaryHealthOverviewClickEnum()
    object AddWorkoutClick : OSummaryHealthOverviewClickEnum()
    object SleepDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object ActivityDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object ReadinessDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    data class ItemWorkoutClick(val id: String, val workOutName: String, val position: Int) :
        OSummaryHealthOverviewClickEnum()

    object AutoSportsDelete : OSummaryHealthOverviewClickEnum()

    object WorkoutAlertWhatisThis : OSummaryHealthOverviewClickEnum()

    object ViewAllWorkoutClick : OSummaryHealthOverviewClickEnum()
    object WorkoutAlertIdentify : OSummaryHealthOverviewClickEnum()

//    class DemoClick(val manualMeasureType: ManualMeasureType) : OSummaryHealthOverviewClickEnum()
}

class OSummaryHealthOverviewAdapter :
    RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    var devicePaired = false
    var lastPosition = -1
    var refreshPosition: Int? = null

    var items = listOf<OHealthOverview>()
        set(value) {
            field = value
            if (refreshPosition != null) {
                if (refreshPosition != -1) {
                    notifyItemChanged(refreshPosition!!)
                } else {
                    notifyDataSetChanged()
                }
            }


        }

    var itemClickListener: ((type: OSummaryHealthOverviewClickEnum) -> Unit)? =
        null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        return when (viewType) {
            R.layout.list_readiness_card_item -> HomeRecyclerViewHolder.ReadinessViewHolder(
                ListReadinessCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_sleep_card_item -> HomeRecyclerViewHolder.SleepViewHolder(
                ListSleepCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_activity_burn_card_item -> HomeRecyclerViewHolder.ActivityViewHolder(
                ListActivityBurnCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_sleep_activity_card_item -> HomeRecyclerViewHolder.SleepActivityViewHolder(
                ListSleepActivityCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_heart_rate_card_item -> HomeRecyclerViewHolder.HeartRateViewHolder(
                ListHeartRateCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_readiness_score_card_item -> HomeRecyclerViewHolder.ReadinessScoreViewHolder(
                ListReadinessScoreCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_oreo_battery_percent_item -> HomeRecyclerViewHolder.OreoBatteryPercentViewHolder(
                ListOreoBatteryPercentItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.oreo_layout_recent_activity -> HomeRecyclerViewHolder.TodayWorkoutPercentViewHolder(
                OreoLayoutRecentActivityBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_o_header_card_item -> HomeRecyclerViewHolder.HeaderViewHolder(
                ListOHeaderCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_o_pair_device -> HomeRecyclerViewHolder.PairDeviceViewHolder(
                ListOPairDeviceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_o_w_alert_card_item -> HomeRecyclerViewHolder.AutoSportViewHolder(
                ListOWAlertCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

//            R.layout.list_o_w_demo_card_item -> HomeRecyclerViewHolder.DemoViewHolder(
//                ListOWDemoCardItemBinding.inflate(
//                    LayoutInflater.from(parent.context),
//                    parent,
//                    false
//                )
//            )
            R.layout.oreo_dummy_view -> HomeRecyclerViewHolder.OreoDummyViewHolder(
                OreoDummyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onViewRecycled(holder: HomeRecyclerViewHolder) {
        when (holder) {
            is HomeRecyclerViewHolder.ActivityViewHolder -> {
                LOGS.d("onViewRecycled ActivityViewHolder")
            }

            is HomeRecyclerViewHolder.HeartRateViewHolder -> {
                LOGS.d("onViewRecycled HeartRateViewHolder")
            }

            is HomeRecyclerViewHolder.ReadinessScoreViewHolder -> {
                LOGS.d("onViewRecycled ReadinessScoreViewHolder")
            }


            is HomeRecyclerViewHolder.OreoDummyViewHolder -> {}
            else -> {}
        }
        super.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: HomeRecyclerViewHolder): Boolean {
        LOGS.d("onViewRecycled failed")
        return super.onFailedToRecycleView(holder)
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener
        when (holder) {
            is HomeRecyclerViewHolder.ActivityViewHolder -> holder.bind(
                items[position] as OHealthOverview.Activity,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.HeartRateViewHolder -> holder.bind(
                items[position] as OHealthOverview.HeartRate,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.ReadinessScoreViewHolder -> holder.bind(
                items[position] as OHealthOverview.ReadinessScore,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.ReadinessViewHolder -> holder.bind(
                items[position] as OHealthOverview.Readiness,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.SleepActivityViewHolder -> holder.bind(
                items[position] as OHealthOverview.SleepActivityScore,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.SleepViewHolder -> holder.bind(
                items[position] as OHealthOverview.Sleep,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.TodayWorkoutPercentViewHolder -> holder.bind(
                items[position] as OHealthOverview.TodayWorkout,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.OreoBatteryPercentViewHolder -> holder.bind(

            )

            is HomeRecyclerViewHolder.HeaderViewHolder -> holder.bind(
                items[position] as OHealthOverview.Header,
                position,
            )

            is HomeRecyclerViewHolder.PairDeviceViewHolder -> holder.bind(
                position
            )

            is HomeRecyclerViewHolder.AutoSportViewHolder -> holder.bind(
                items[position] as OHealthOverview.AutoSport,
                position,
            )

//            is HomeRecyclerViewHolder.DemoViewHolder -> holder.bind(
//                items[position] as OHealthOverview.Demo,
//                position,
//            )
            is HomeRecyclerViewHolder.OreoDummyViewHolder -> {}
            else -> {}
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is OHealthOverview.Readiness -> R.layout.list_readiness_card_item
            is OHealthOverview.Activity -> R.layout.list_activity_burn_card_item
            is OHealthOverview.FitnessOverView -> R.layout.list_health_overview_card_item
            is OHealthOverview.HeartRate -> R.layout.list_heart_rate_card_item
            is OHealthOverview.OreoBattery -> R.layout.list_oreo_battery_percent_item
            is OHealthOverview.ReadinessScore -> R.layout.list_readiness_score_card_item
            is OHealthOverview.Sleep -> R.layout.list_sleep_card_item
            is OHealthOverview.SleepActivityScore -> R.layout.list_sleep_activity_card_item
            is OHealthOverview.TodayWorkout -> R.layout.oreo_layout_recent_activity
            is OHealthOverview.Header -> R.layout.list_o_header_card_item
            is OHealthOverview.PairDevice -> R.layout.list_o_pair_device
            is OHealthOverview.AutoSport -> R.layout.list_o_w_alert_card_item
            is OHealthOverview.Dummy -> R.layout.oreo_dummy_view
        }
    }
}


sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((type: OSummaryHealthOverviewClickEnum) -> Unit)? = null

    class ReadinessViewHolder(private val binding: ListReadinessCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Readiness,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            binding.imv.loadImage(binding.imv.context, R.drawable.ic_readiness_card_bg1)

            val scoreValue = data.data.readinessScore ?: 0
            if (scoreValue == 0) {
                binding.tvValue.text = "--"
                binding.tvStatus.text = "No data"
                binding.tvTodayDesc.text = ""
                binding.tvTodayDesc.invisible()

            } else {
                binding.tvValue.text = scoreValue.toString()
                binding.tvStatus.text = data.data.status
                binding.tvTodayDesc.visible()
            }

            if (scoreValue >= 0) {
                binding.lottieAnimationView.repeatCount = 0
                binding.lottieAnimationView.setAnimation(R.raw.lottie_meter_readiness)
                binding.lottieAnimationView.setMaxProgress(
                    MiscUtil.scorePercentCalculator(
                        scoreValue.toFloat()
                    )
                )
                binding.lottieAnimationView.playAnimation()

            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ReadinessDetailsWorkoutClick)
            }
        }
    }

    class SleepViewHolder(private val binding: ListSleepCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Sleep,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {
            binding.imv.loadImage(binding.imv.context, R.drawable.ic_sleep_card_bg1)
            val scoreValue = data.data.sleepScore ?: 0
            if (scoreValue <= 0) {
                binding.tvSleepStart.text = "Start time"
                binding.tvSleepEnd.text = "End time"
                binding.tvSleepStart.setTextColor(R.color.white_80.getColor())
                binding.tvSleepEnd.setTextColor(R.color.white_80.getColor())
                binding.tvValue.text = "--"
                binding.tvStatus.text = "No data"
                binding.tvHrValue.text = "--"
                binding.tvHrUnit.gone()
                binding.tvSleepMinute.text = "--"
                binding.tvSleepHour.gone()
                binding.textHour.gone()
                binding.textMins.gone()
            } else {
                val (hourTimeInBed, minuteTimeInBed) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    data.data.totalSleep ?: 0
                )

//                LOGS.d("sdasadjsdajlksadjlkdsa ${ data.sleepArray.first().startTime} ${Gson().toJson(data.sleepArray)}")
                binding.tvSleepStart.text = DateFormats.formatDate(
                    data.sleepArray.first().startTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.time12Meridian
                )
                binding.tvSleepEnd.text = DateFormats.formatDate(
                    data.sleepArray.last().endTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.time12Meridian
                )

                binding.tvHrValue.text = if (data.data.lowestHr == null) {
                    "--"
                } else {
                    data.data.lowestHr.toString()
                }

                binding.tvSleepHour.text = hourTimeInBed.toString()
                binding.tvSleepMinute.text = minuteTimeInBed.toString()
                binding.tvSleepHour.visible()
                binding.textHour.visible()
                binding.textMins.visible()

                binding.tvValue.text = scoreValue.toString()
                binding.tvStatus.text = data.data.status
                binding.tvSleepStart.setTextColor(R.color.white.getColor())
                binding.tvSleepEnd.setTextColor(R.color.white.getColor())
                binding.tvHrUnit.visible()
                binding.tvSleepStart.visible()

            }

            val sleepDayGraphView = SleepProgressbarView(binding.sleepPgbr.context)
            binding.sleepPgbr.addView(sleepDayGraphView)
            sleepDayGraphView.setData(data.sleepArray)

            LOGS.d("sleep_____update")


            if (scoreValue >= 0) {
                binding.lottieAnimationView.repeatCount = 0
                binding.lottieAnimationView.setAnimation(R.raw.lottie_meter_sleep)
                binding.lottieAnimationView.setMaxProgress(
                    MiscUtil.scorePercentCalculator(
                        scoreValue.toFloat()
                    )
                )
                binding.lottieAnimationView.playAnimation()

            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.SleepDetailsWorkoutClick)
            }
        }
    }

    class ActivityViewHolder(private val binding: ListActivityBurnCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Activity,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {
            binding.imv.loadImage(binding.imv.context, R.drawable.ic_activity_card_bg1)
            val scoreValue = data.data.activityScore ?: 0
            val caloriesGoalText = "/ ${data.caloriesGoal}"
            if (scoreValue <= 0) {
                binding.tvValue.text = "--"
                binding.tvStatus.text = "No data"
                binding.tvTodayDesc.text = ""
                binding.tvCalories.text = "--"
                binding.tvTotalCalories.text = caloriesGoalText
            } else {
                binding.tvValue.text = scoreValue.toString()
                binding.tvStatus.text = data.data.status
                binding.tvCalories.text = data.data.activeCalories.toString()
                binding.tvTotalCalories.text = caloriesGoalText
                if (!data.data.nudge.isNullOrEmpty()) {
                    binding.tvTodayDesc.text = data.data.nudge
                }
            }


            if (scoreValue >= 0) {
                binding.lottieAnimationView.repeatCount = 0
                binding.lottieAnimationView.setAnimation(R.raw.lottie_meter_readiness)
                binding.lottieAnimationView.setMaxProgress(
                    MiscUtil.scorePercentCalculator(
                        scoreValue.toFloat()
                    )
                )
                binding.lottieAnimationView.playAnimation()

            }


            var caloriesProgress = data.data.activeCalories?.toFloat() ?: 0f
            val caloriesGoal = data.caloriesGoal.toFloat()
            caloriesProgress = (if (caloriesProgress > caloriesGoal) 100f else
                caloriesProgress.calculatePercentage(
                    caloriesGoal
                ))

            binding.ltProgresso.pbSteps.apply {
                progress = caloriesProgress.toInt()
                setIndicatorColor(
                    R.color.medium_movement.getColor()
                )
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick)
            }
        }
    }


    class SleepActivityViewHolder(private val binding: ListSleepActivityCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.SleepActivityScore,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {
            if (data.sleepScore != null && data.sleepScore >= 0) {
                binding.tvSleepScore.text = data.sleepScore.toString()
                binding.tvAvgThisWeek.gone()
                binding.tvDaysAvg.visible()
                binding.sleepLineChart.visible()
                binding.sleepLine.root.visible()
                val trendValue = "${data.sleepTrend}%"
                if (data.sleepTrend != null && data.sleepTrend > 0) {
                    binding.sleepTrendValue.text = trendValue
                    binding.sleepTrendValue.setTextColor(Color.parseColor("#29cc74"))
                    binding.sleepTrendImv.loadImage(
                        binding.sleepTrendImv.context,
                        R.drawable.ic_trend_up
                    )
                    binding.sleepTrendImv.visible()
                    binding.sleepTrendValue.visible()
                    binding.tvSleepFromLast.visible()
                } else if (data.sleepTrend != null && data.sleepTrend < 0) {
                    binding.sleepTrendValue.text = trendValue
                    binding.sleepTrendImv.loadImage(
                        binding.sleepTrendImv.context,
                        R.drawable.ic_trend_down
                    )
                    binding.sleepTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                    binding.sleepTrendImv.visible()
                    binding.sleepTrendValue.visible()
                    binding.tvSleepFromLast.visible()
                } else {
                    binding.sleepTrendImv.invisible()
                    binding.sleepTrendValue.invisible()
                    binding.tvSleepFromLast.invisible()
                }


//                LOGS.d("dsjasdlkjasdljaskldjldsa ${Gson().toJson(data.sleepValue)}")
                binding.sleepLineChart.updateDataWithMaxMin(
                    data.sleepValue,
                    ArrayList(),
                    ArrayList(),
                    20,
                    true
                )
            } else {

                binding.tvAvgThisWeek.visible()
                binding.tvSleepScore.text = "--"

                binding.tvDaysAvg.gone()
                binding.sleepLineChart.gone()
                binding.sleepLine.root.gone()
                binding.sleepTrendImv.gone()
                binding.sleepTrendValue.gone()
                binding.tvSleepFromLast.gone()
            }

            if (data.activityScore != null && data.activityScore >= 0) {

                binding.tvActivityScore.text = data.activityScore.toString()
                binding.activityLineChart.updateDataWithMaxMin(
                    data.activityValue,
                    ArrayList(),
                    ArrayList(),
                    20,
                    true
                )
                binding.tvActAvgThisWeek.gone()
                binding.tvDaysAvg1.visible()
                binding.activityLineChart.visible()
                binding.activityLine.root.visible()
                binding.activityTrendImv.visible()
                binding.activityTrendValue.visible()
                binding.tvActivityFrom.visible()

                val trendValue = "${data.activityTrend}%"
                if (data.activityTrend != null && data.activityTrend > 0) {
                    binding.activityTrendValue.text = trendValue
                    binding.activityTrendValue.setTextColor(Color.parseColor("#29cc74"))
                    binding.activityTrendImv.loadImage(
                        binding.sleepTrendImv.context,
                        R.drawable.ic_trend_up
                    )
                    binding.activityTrendImv.visible()
                    binding.activityTrendValue.visible()
                    binding.tvActivityFrom.visible()
                } else if (data.activityTrend != null && data.activityTrend < 0) {
                    binding.activityTrendValue.text = trendValue
                    binding.activityTrendImv.loadImage(
                        binding.sleepTrendImv.context,
                        R.drawable.ic_trend_down
                    )
                    binding.activityTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                    binding.activityTrendImv.visible()
                    binding.activityTrendValue.visible()
                    binding.tvActivityFrom.visible()
                } else {
                    binding.activityTrendImv.invisible()
                    binding.activityTrendValue.invisible()
                    binding.tvActivityFrom.invisible()
                }
//                binding.activityLineChart.updateDataWithMax(data.activityValue, ArrayList(), ArrayList())
            } else {
                binding.tvActivityScore.text = "--"


                binding.tvActAvgThisWeek.visible()
                binding.tvDaysAvg1.gone()
                binding.activityLineChart.gone()
                binding.activityLine.root.gone()
                binding.activityTrendImv.gone()
                binding.activityTrendValue.gone()
                binding.tvActivityFrom.gone()
            }


            binding.root.setOnClickListener {
                //   itemClickListener?.invoke(it, data, position)
            }
        }
    }

    class HeartRateViewHolder(private val binding: ListHeartRateCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.HeartRate,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            val chart = binding.candleChart

            OCombineChartUtils.setChart(chart, data.xLabelList, data.axisMinimum, data.average)

            val combinedData = CombinedData()

            binding.tvHeartValue.text = data.value
            binding.tvLastMeasure.text = data.lastTime

            if (data.lineData.first.isNotEmpty() && data.lineData.first.size > 1) {
                combinedData.setData(
                    OCombineChartUtils.generateLineData(
                        data.lineData.first,
                        binding.candleChart,
                        data.lineData.second,
                        data.axisMinimum
                    )
                )
                combinedData.setData(
                    OCombineChartUtils.generateCandleData(
                        data.candleValue,
                        R.color.o_heart_bg
                    )
                )
                chart.data = combinedData
                chart.invalidate()
            }

            if (data.isMeasuring) {
                binding.lottieAnimView.visible()
                binding.imvHrMeasure.invisible()
            } else {
                binding.lottieAnimView.invisible()
                binding.imvHrMeasure.visible()
            }

            binding.imvHrMeasure.setOnClickListener {
                if (binding.tvLastMeasure.text == "measuring") {
                    return@setOnClickListener
                }

                binding.tvHeartValue.text = "--"
                binding.tvHeartValue.visible()
                binding.tvHeartUnit.visible()
                binding.tvLastMeasure.visible()
                binding.tvEmptyConnect.gone()
                binding.tvLastMeasure.text = "measuring"

                binding.lottieAnimView.visible()
                binding.imvHrMeasure.invisible()

                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.MeasureHRClick)
            }

            if (data.errorMessage.isNullOrEmpty()) {
                binding.tvEmptyConnect.gone()
            } else {
                binding.tvEmptyConnect.text = data.errorMessage
                binding.tvEmptyConnect.visible()
            }
            if (data.value.toInt() > 0) {
                binding.tvEmptyConnect.gone()
                binding.tvHeartValue.visible()
                binding.tvHeartUnit.visible()
                binding.tvLastMeasure.visible()
            } else {
                binding.tvHeartValue.gone()
                binding.tvHeartUnit.gone()
                binding.tvLastMeasure.gone()
            }


            binding.root.setOnClickListener {
//                   itemClickListener?.invoke(it, data, position)
            }
        }
    }


    class ReadinessScoreViewHolder(private val binding: ListReadinessScoreCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.ReadinessScore,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            if (data.score != null && data.score >= 0) {
                binding.tvSleepScore.text = data.score.toString()
                binding.tvAvgThisWeek.gone()
                binding.tvDaysAvg.visible()
                binding.tvSleepScore.visible()
                binding.tvEmpty.gone()
                binding.lineChart.visible()
                val trendValue = "${data.trend}%"
                if (data.trend != null && data.trend > 0) {
                    binding.sleepTrendValue.text = trendValue
                    binding.sleepTrendValue.setTextColor(Color.parseColor("#29cc74"))
                    binding.sleepTrendImv.loadImage(
                        binding.sleepTrendImv.context,
                        R.drawable.ic_trend_up
                    )
                    binding.sleepTrendImv.visible()
                    binding.sleepTrendValue.visible()
                    binding.tvSleepFromLast.visible()
                } else if (data.trend != null && data.trend < 0) {
                    binding.sleepTrendValue.text = trendValue
                    binding.sleepTrendImv.loadImage(
                        binding.sleepTrendImv.context,
                        R.drawable.ic_trend_down
                    )
                    binding.sleepTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                    binding.sleepTrendImv.visible()
                    binding.sleepTrendValue.visible()
                    binding.tvSleepFromLast.visible()
                } else {
                    binding.sleepTrendImv.invisible()
                    binding.sleepTrendValue.invisible()
                    binding.tvSleepFromLast.invisible()
                }



                binding.lineChart.updateDataWithMaxMin(
                    data.value,
                    ArrayList(),
                    ArrayList(),
                    20,
                    true
                )
            } else {

                binding.tvAvgThisWeek.visible()
                binding.tvSleepScore.text = "--"
                binding.tvSleepScore.gone()
                binding.tvEmpty.visible()
                binding.tvDaysAvg.gone()
                binding.lineChart.gone()
                binding.sleepTrendImv.gone()
                binding.sleepTrendValue.gone()
                binding.tvSleepFromLast.gone()
            }




            binding.root.setOnClickListener {
                //   itemClickListener?.invoke(it, data, position)
            }
        }
    }

    class OreoBatteryPercentViewHolder(private val binding: ListOreoBatteryPercentItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(

        ) {


        }
    }

    class TodayWorkoutPercentViewHolder(private val binding: OreoLayoutRecentActivityBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            todayWorkout: OHealthOverview.TodayWorkout,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            binding.tvEmptyMsg.gone()
            binding.rvWorkouts.layoutManager =
                LinearLayoutManager(binding.rvWorkouts.context, LinearLayoutManager.VERTICAL, false)
            val adapter1 = OreoRWorkoutAdapter(object : OreoRWorkoutAdapter.OnItemClickListener {
                override fun onItemClick(data: OActivityListModal, position: Int) {
                    itemClickListener?.invoke(
                        OSummaryHealthOverviewClickEnum.ItemWorkoutClick(
                            data.id ?: "",
                            data.getFormattedActivityName(),
                            position
                        )
                    )
                }

            })
            binding.rvWorkouts.apply {
                adapter = adapter1
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }

//            var todayWorkText = "Workouts"
//            todayWorkout.listData.forEach {
//                val date = it.createdDate
//                if (date == DateFormats.getCurrentDate(DateFormats.dateFormat6)) {
//                    todayWorkText = "Today’s Workouts"
//                    return@forEach
//                }
//            }
//            binding.textView66.text = todayWorkText
            adapter1.setData(todayWorkout.listData)
            binding.viewAddWorkout.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.AddWorkoutClick)

            }

            binding.ivViewAll.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ViewAllWorkoutClick)

            }
//            if (todayWorkout.listData.isNotEmpty()) {
//                binding.ivViewAll.visible()
//            } else
//                binding.ivViewAll.invisible()

        }
    }

    class OreoDummyViewHolder(private val binding: OreoDummyViewBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(

        ) {


        }
    }

    class HeaderViewHolder(private val binding: ListOHeaderCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Header,
            position: Int,
        ) {
            binding.tvGreeting.text = data.greeting
            binding.tvDate.text = data.date

        }
    }

    class PairDeviceViewHolder(private val binding: ListOPairDeviceBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            position: Int,
        ) {

            binding.btnPairDevice.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.PairDeviceClicked)
            }


        }
    }

    class AutoSportViewHolder(private val binding: ListOWAlertCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.AutoSport,
            position: Int,
        ) {
            var title = "${data.count} workouts detected"
            if (data.count <= 1) {
                title = "${data.count} workout detected"
            }
            binding.tvTitle.text = title

            binding.tvDesc.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.WorkoutAlertIdentify)
            }

            binding.tvWhatIsThis.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.WorkoutAlertWhatisThis)
            }

            binding.btnCancel.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.AutoSportsDelete)
            }

        }
    }


//    class DemoViewHolder(private val binding: ListOWDemoCardItemBinding) :
//        HomeRecyclerViewHolder(binding) {
//        fun bind(
//            data: OHealthOverview.Demo,
//            position: Int,
//        ) {
//
//            binding.tvStressValue.text = data.stressValue.toString()
//            binding.tvBodyTempValue.text = data.bodyTempValue.toString()
//            binding.tvHrValue.text = data.hrValue.toString()
//            binding.tvSpoValue.text = data.spo2Value.toString()
//
//            binding.tvBodyTempValue.setOnClickListener {
//                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.WorkoutAlertIdentify)
//            }
//
//            binding.tvHrValue.setOnClickListener {
//                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.WorkoutAlertWhatisThis)
//            }
//
//            binding.tvSpoValue.setOnClickListener {
//                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.AutoSportsDelete)
//            }
//
//            binding.tvStressValue.setOnClickListener {
//                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.AutoSportsDelete)
//            }
//
//        }
//    }


}



