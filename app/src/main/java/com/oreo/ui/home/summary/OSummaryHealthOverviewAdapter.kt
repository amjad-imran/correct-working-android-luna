package com.oreo.ui.home.summary

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.mikephil.charting.data.CombinedData
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.*
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.ui.dashboard.summary.DashboardBannerAction
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.dpToPx
import com.noisefit_commans.ui.*
import com.noisefit_commans.ui.custom.SleepProgressbarView
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.oreo.data.model.AlertType
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.util.graph.OCombineChartUtils
import java.lang.Math.abs
import java.lang.StringBuilder

sealed class OSummaryHealthOverviewClickEnum {
    object SleepDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object ActivityDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object ReadinessDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()


    object AutoSportsDelete : OSummaryHealthOverviewClickEnum()

    object WorkoutAlertWhatisThis : OSummaryHealthOverviewClickEnum()

    object WorkoutAlertIdentify : OSummaryHealthOverviewClickEnum()

}

class OSummaryHealthOverviewAdapter :
    RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    var devicePaired = false
    var lastPosition = -1
    var refreshPosition: Int? = null

    var items = listOf<OHealthOverview>()
        set(value) {
            try {
                field = value
                if (refreshPosition != null) {
                    if (refreshPosition != -1) {
                        notifyItemChanged(refreshPosition!!)
                    } else {
                        notifyDataSetChanged()
                    }
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
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

            R.layout.list_sleep_minimal_item -> HomeRecyclerViewHolder.SleepMinimalViewHolder(
                ListSleepMinimalItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_sleep_waiting_card_item -> HomeRecyclerViewHolder.SleepWaitingViewHolder(
                ListSleepWaitingCardItemBinding.inflate(
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

            R.layout.list_activity_minimal_item -> HomeRecyclerViewHolder.ActivityMinimalViewHolder(
                ListActivityMinimalItemBinding.inflate(
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


            R.layout.list_readiness_score_card_item -> HomeRecyclerViewHolder.ReadinessScoreViewHolder(
                ListReadinessScoreCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_readiness_minimal_card_item -> HomeRecyclerViewHolder.ReadinessMinimalViewHolder(
                ListReadinessMinimalCardItemBinding.inflate(
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


            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
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

            is HomeRecyclerViewHolder.ActivityMinimalViewHolder -> holder.bind(
                items[position] as OHealthOverview.ActivityMinimal,
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

            is HomeRecyclerViewHolder.ReadinessMinimalViewHolder -> holder.bind(
                items[position] as OHealthOverview.ReadinessMinimal,
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

            is HomeRecyclerViewHolder.SleepMinimalViewHolder -> holder.bind(
                items[position] as OHealthOverview.SleepMinimal,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.SleepWaitingViewHolder -> holder.bind()


            is HomeRecyclerViewHolder.AutoSportViewHolder -> holder.bind(
                items[position] as OHealthOverview.AutoSport,
                position,
            )

            else -> {}
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {

            is OHealthOverview.ReadinessMinimal -> R.layout.list_readiness_minimal_card_item
            is OHealthOverview.Readiness -> R.layout.list_readiness_card_item
            is OHealthOverview.ReadinessScore -> R.layout.list_readiness_score_card_item


            is OHealthOverview.SleepWaiting -> R.layout.list_sleep_waiting_card_item
            is OHealthOverview.SleepMinimal -> R.layout.list_sleep_minimal_item
            is OHealthOverview.Sleep -> R.layout.list_sleep_card_item
            is OHealthOverview.SleepActivityScore -> R.layout.list_sleep_activity_card_item


            is OHealthOverview.ActivityMinimal -> R.layout.list_activity_minimal_item
            is OHealthOverview.Activity -> R.layout.list_activity_burn_card_item


            is OHealthOverview.AutoSport -> R.layout.list_o_w_alert_card_item
            is OHealthOverview.HeartRate -> 0
        }
    }
}


sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((type: OSummaryHealthOverviewClickEnum) -> Unit)? = null

    class ReadinessMinimalViewHolder(private val binding: ListReadinessMinimalCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.ReadinessMinimal,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {
            val scoreValue = data.data.readinessScore ?: 0

            binding.tvScore.text = scoreValue.toString()
            binding.tvScoreValue.text = data.data.status

            if (data.data.nudges.isNullOrEmpty()) {
                binding.tvNudge.text = ""
            } else {
                binding.tvNudge.text = data.data.nudges.first()
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ReadinessDetailsWorkoutClick)
            }

        }
    }

    class ReadinessViewHolder(private val binding: ListReadinessCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Readiness,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            //binding.imv.loadImage(binding.imv.context, R.drawable.ic_readiness_card_bg1)

            val scoreValue = data.data.readinessScore ?: 0
            if (scoreValue == 0) {
                binding.tvValue.text = "--"
                binding.tvStatus.text = "No data"
//                binding.tvTodayDesc.text = ""
//                binding.tvTodayDesc.invisible()

            } else {
                binding.tvValue.text = scoreValue.toString()
                binding.tvStatus.text = data.data.status
//                binding.tvTodayDesc.visible()
            }

            if (data.data.nudges.isNullOrEmpty()) {
                (binding.tvTodayDesc.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = binding.tvTodayDesc.context.dpToPx(24)
                    bottomMargin = 0
                }
            } else {
                (binding.tvTodayDesc.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = binding.tvTodayDesc.context.dpToPx(24)
                    bottomMargin = binding.tvTodayDesc.context.dpToPx(26)
                }
                binding.tvTodayDesc.text = data.data.nudges.first()
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


    class SleepWaitingViewHolder(private val binding: ListSleepWaitingCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
        ) {

        }
    }

    class SleepMinimalViewHolder(private val binding: ListSleepMinimalItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.SleepMinimal,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            val scoreValue = data.data.sleepScore ?: 0

            val sleepTime = StringBuilder()
            sleepTime.append(
                DateFormats.formatDate(
                    data.sleepArray.firstOrNull()?.startTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.time12Meridian
                )
            )
            sleepTime.append(" - ")
            sleepTime.append(
                DateFormats.formatDate(
                    data.sleepArray.lastOrNull()?.endTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.time12Meridian
                )
            )
            binding.tvSleepStartEndTime.text = sleepTime.toString()
            binding.tvSleepScore.text = scoreValue.toString()
            binding.tvSleepStatus.text = data.data.status
            binding.tvLowestHr.text = if (data.data.lowestHr == null) {
                "--"
            } else {
                data.data.lowestHr.toString() + " bpm"
            }

            val (hourTimeInBed, minuteTimeInBed) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                data.data.totalSleep ?: 0
            )

            binding.tvSleepTime.text = if (hourTimeInBed == 0) {
                "$minuteTimeInBed min"
            } else {
                "$hourTimeInBed hr $minuteTimeInBed min"
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.SleepDetailsWorkoutClick)
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
            //binding.imv.loadImage(binding.imv.context, R.drawable.ic_sleep_card_bg1)
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
            binding.sleepPgbr.removeAllViews()
            binding.sleepPgbr.addView(sleepDayGraphView)
            sleepDayGraphView.setData(data.sleepArray)



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

    class ActivityMinimalViewHolder(private val binding: ListActivityMinimalItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.ActivityMinimal,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            binding.tvCurrentCalories.text = if ((data.data.activeCalories ?: 0) > 0) {
                data.data.activeCalories.toString()
            } else {
                "--"
            }

            val percent = (50 / data.caloriesGoal.toFloat()) * 100
            LOGS.d("PERCENT $percent")

            binding.pbCurrent.layoutParams = binding.pbCurrent.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight = percent
            }
            binding.pbTotal.layoutParams = binding.pbTotal.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight = (100 - percent)
            }
            binding.pbCurrent.progress = (data.data.activeCalories ?: 0) * 2//For 50 kcal only, change accordingly

            binding.tvTotalCalories.text = "${data.caloriesGoal}"

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick)
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
            //binding.imv.loadImage(binding.imv.context, R.drawable.ic_activity_card_bg1)
            val scoreValue = data.data.activityScore

            if (scoreValue == null) {
                binding.tvValue.text = "--"
                binding.tvStatus.text = "No data"
            } else {
                binding.tvValue.text = scoreValue.toString()
                binding.tvStatus.text = data.data.status
            }


            if (data.data.nudges.isNullOrEmpty()) {
                binding.tvTodayDesc.gone()
            } else {
                binding.tvTodayDesc.visible()
                binding.tvTodayDesc.text = data.data.nudges.first()
            }

            val caloriesGoalText = "/ ${data.caloriesGoal} kcal"
            binding.tvTotalCalories.text = caloriesGoalText

            binding.tvCalories.text = if ((data.data.activeCalories ?: 0) > 0) {
                data.data.activeCalories.toString()
            } else {
                "--"
            }

            if ((scoreValue ?: 0) >= 0) {
                binding.lottieAnimationView.repeatCount = 0
                binding.lottieAnimationView.setAnimation(R.raw.lottie_meter_activity)
                binding.lottieAnimationView.setMaxProgress(
                    MiscUtil.scorePercentCalculator(
                        (scoreValue ?: 0).toFloat()
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

            binding.pbSteps.apply {
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
                val trendValue = "${kotlin.math.abs(data.sleepTrend ?: 0)}%"
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

                val trendValue = "${kotlin.math.abs(data.activityTrend ?: 0)}%"
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
                val trendValue = "${kotlin.math.abs(data.trend ?: 0)}%"
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

    class AlertsAdapter(val listener: AlertClickListener) :
        RecyclerView.Adapter<AlertsAdapter.ViewHolder>() {
        private var mDataSet = ArrayList<DashAlert>()

        inner class ViewHolder(private val binding: RowDashAlertBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(data: DashAlert) {

                binding.tvAlertMessage.text = data.message
                if (data.isCancellable) {
                    binding.ivClose.visible()
                } else {
                    binding.ivClose.invisible()
                }

                binding.root.setOnClickListener {
                    listener.onAlertClicked(data.type)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                RowDashAlertBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(mDataSet[position])
        }

        override fun getItemCount(): Int = mDataSet.size

        fun setDataSet(dataSet: HashMap<AlertType, DashAlert>) {
            mDataSet.clear()
            val list = ArrayList<DashAlert>()
            dataSet.forEach {
                list.add(it.value.apply {
                    this.type = it.key
                })
            }
            mDataSet.addAll(list)
            notifyDataSetChanged()
        }
    }


}

interface AlertClickListener {
    fun onAlertClicked(alertType: AlertType)
}



