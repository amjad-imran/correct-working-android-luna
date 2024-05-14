package com.oreo.ui.home.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CardTrackFmHealthBinding
import com.noisefit.luna.databinding.ItemStressGraphBinding
import com.noisefit.luna.databinding.ListActivityBurnCardItem2Binding
import com.noisefit.luna.databinding.ListActivityBurnCardItemBinding
import com.noisefit.luna.databinding.ListActivityMinimalItemBinding
import com.noisefit.luna.databinding.ListCycleTrackerOngoingBinding
import com.noisefit.luna.databinding.ListCycleTrackerPredictionBinding
import com.noisefit.luna.databinding.ListDashNapBinding
import com.noisefit.luna.databinding.ListOWAlertCardItemBinding
import com.noisefit.luna.databinding.ListPeriodConfirmationBinding
import com.noisefit.luna.databinding.ListReadinessCardItemBinding
import com.noisefit.luna.databinding.ListReadinessMinimalCardItemBinding
import com.noisefit.luna.databinding.ListRingCareBinding
import com.noisefit.luna.databinding.ListSleepCardItemBinding
import com.noisefit.luna.databinding.ListSleepMinimalItemBinding
import com.noisefit.luna.databinding.ListSleepWaitingCardItemBinding
import com.noisefit.luna.databinding.ListVideoInfoCardBinding
import com.noisefit.luna.databinding.ListWelcomeCardBinding
import com.noisefit.luna.databinding.RowDashAlertBinding
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.dpToPx
import com.noisefit_commans.ui.custom.SleepProgressbarView
import com.noisefit_commans.ui.getColor
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.oreo.data.model.AlertType
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.VideoInfoType
import com.oreo.util.UtilClass.seriesItemWithoutInset


sealed class OSummaryHealthOverviewClickEnum {
    object SleepDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object ActivityDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object ReadinessDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object TextWelcomeRingClicked : OSummaryHealthOverviewClickEnum()
    data class OnNapClicked(val napId: String) : OSummaryHealthOverviewClickEnum()
    object StressGraphClicked : OSummaryHealthOverviewClickEnum()
    data class TextRingCareClicked(val title: String) : OSummaryHealthOverviewClickEnum()
    data class VideoInfoClicked(val type: VideoInfoType, val videoUrl: String) :
        OSummaryHealthOverviewClickEnum()


    object AutoSportsDelete : OSummaryHealthOverviewClickEnum()

    object WorkoutAlertWhatisThis : OSummaryHealthOverviewClickEnum()

    object WorkoutAlertIdentify : OSummaryHealthOverviewClickEnum()
    object TrackYourFemaleHealth : OSummaryHealthOverviewClickEnum()

}

class OSummaryHealthOverviewAdapter() :
    RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    var devicePaired = false
    var lastPosition = -1
    var refreshPosition: Int? = null

    var items = listOf<OHealthOverview>()
        set(value) {
            try {
                field = value
                notifyDataSetChanged()
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }

    var itemClickListener: ((type: OSummaryHealthOverviewClickEnum) -> Unit)? =
        null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        return when (viewType) {
            R.layout.list_dash_nap -> HomeRecyclerViewHolder.NapWidgetCardViewHolder(
                ListDashNapBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_stress_graph -> HomeRecyclerViewHolder.StressGraphViewHolder(
                ItemStressGraphBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_video_info_card -> HomeRecyclerViewHolder.InfoVideoCardViewHolder(
                ListVideoInfoCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_welcome_card -> HomeRecyclerViewHolder.InfoWelcomeCardViewHolder(
                ListWelcomeCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_ring_care -> HomeRecyclerViewHolder.InfoRingCareViewHolder(
                ListRingCareBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

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

            R.layout.list_activity_burn_card_item_2 -> HomeRecyclerViewHolder.ActivityViewHolder2(
                ListActivityBurnCardItem2Binding.inflate(
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

            R.layout.list_cycle_tracker_prediction -> HomeRecyclerViewHolder.CycleTrackerPredictViewHolder(
                ListCycleTrackerPredictionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_cycle_tracker_ongoing -> HomeRecyclerViewHolder.CycleTrackerOngoingViewHolder(
                ListCycleTrackerOngoingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.card_track_fm_health -> HomeRecyclerViewHolder.TrackYourCycleViewHolder(
                CardTrackFmHealthBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.list_period_confirmation -> HomeRecyclerViewHolder.GotYourPeriodViewHolder(
                ListPeriodConfirmationBinding.inflate(
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
            is HomeRecyclerViewHolder.StressGraphViewHolder -> holder.bind(items[position] as OHealthOverview.StressGraph)
            is HomeRecyclerViewHolder.NapWidgetCardViewHolder -> holder.bind(items[position] as OHealthOverview.NapDashCard)
            is HomeRecyclerViewHolder.InfoWelcomeCardViewHolder -> holder.bind(items[position] as OHealthOverview.InfoRingWelcome)
            is HomeRecyclerViewHolder.InfoRingCareViewHolder -> holder.bind(items[position] as OHealthOverview.InfoRingCare)
            is HomeRecyclerViewHolder.InfoVideoCardViewHolder -> holder.bind(
                items[position] as OHealthOverview.InfoVideo,
            )


            is HomeRecyclerViewHolder.ActivityViewHolder -> holder.bind(
                items[position] as OHealthOverview.Activity,
                position,
                lastPosition,
                devicePaired
            )

            is HomeRecyclerViewHolder.ActivityViewHolder2 -> holder.bind(
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

            is HomeRecyclerViewHolder.CycleTrackerPredictViewHolder -> holder.bind(
                items[position] as OHealthOverview.CycleTrackerPredict,
                position,
            )

            is HomeRecyclerViewHolder.CycleTrackerOngoingViewHolder -> holder.bind(
                items[position] as OHealthOverview.CycleTrackerOngoing,
                position,
            )

            is HomeRecyclerViewHolder.TrackYourCycleViewHolder -> holder.bind(
                items[position] as OHealthOverview.CardTrackFemaleHealth,
                position,
            )

            is HomeRecyclerViewHolder.GotYourPeriodViewHolder -> holder.bind(
                items[position] as OHealthOverview.GotYourPeriod,
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
            is OHealthOverview.Activity -> R.layout.list_activity_burn_card_item_2


            is OHealthOverview.AutoSport -> R.layout.list_o_w_alert_card_item
            is OHealthOverview.HeartRateDataModel -> 0
            is OHealthOverview.InfoVideo -> R.layout.list_video_info_card
            is OHealthOverview.StressGraph -> R.layout.item_stress_graph
            is OHealthOverview.InfoRingCare -> R.layout.list_ring_care
            is OHealthOverview.InfoRingWelcome -> R.layout.list_welcome_card
            is OHealthOverview.NapDashCard -> R.layout.list_dash_nap
            is OHealthOverview.CycleTrackerPredict -> R.layout.list_cycle_tracker_prediction
            is OHealthOverview.CycleTrackerOngoing -> R.layout.list_cycle_tracker_ongoing
            is OHealthOverview.CardTrackFemaleHealth -> R.layout.card_track_fm_health
            is OHealthOverview.GotYourPeriod -> R.layout.list_period_confirmation
        }
    }
}


sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((type: OSummaryHealthOverviewClickEnum) -> Unit)? = null

    class NapWidgetCardViewHolder(private val binding: ListDashNapBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.NapDashCard,
        ) {

            binding.rvNap.layoutManager = LinearLayoutManager(binding.rvNap.context)
            binding.rvNap.adapter = DashNapAdapter(data.naps, data.date).apply {

                this.setOnNapSelectedListener(object : OnNapSelectedAction {
                    override fun onNapSelected(napId: String) {
                        itemClickListener?.invoke(
                            OSummaryHealthOverviewClickEnum.OnNapClicked(
                                napId
                            )
                        )
                    }
                })
            }
        }
    }

    class InfoVideoCardViewHolder(private val binding: ListVideoInfoCardBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.InfoVideo,
        ) {

            binding.tvTitle.text = data.data.title
            binding.tvMessage.text = data.data.time


            binding.ivBack.loadImage(
                binding.ivBack.context,
                when (data.type) {
                    VideoInfoType.SLEEP -> R.drawable.back_info_sleep
                    VideoInfoType.READINESS -> R.drawable.back_info_readiness
                    VideoInfoType.ACTIVITY -> R.drawable.back_info_activity
                }
            )

            binding.root.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.VideoInfoClicked(
                        data.type,
                        data.data.url
                    )
                )
            }

        }
    }

    class InfoWelcomeCardViewHolder(private val binding: ListWelcomeCardBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(data: OHealthOverview.InfoRingWelcome) {

            binding.tvTitle.text = data.data.title
            binding.tvMessage.text = data.data.content


            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.TextWelcomeRingClicked)
            }

        }
    }

    class StressGraphViewHolder(private val binding: ItemStressGraphBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.StressGraph,
        ) {
            binding.graphStress.updateData(data.data)

            binding.tvBeta.setVisibilityByCondition(data.isBeta)
            binding.ivBackBeta.setVisibilityByCondition(data.isBeta)


            if (data.value == 0) {
                binding.tvStressValue.gone()
                binding.tvStressStatus.gone()
                binding.tvLastUpdate.gone()
            } else {
                binding.tvStressValue.visible()
                binding.tvStressStatus.visible()
                binding.tvLastUpdate.visible()

                binding.tvStressValue.text = "${data.value}"
                binding.tvStressStatus.text = data.valueStatus

                if (data.isToday) {
                    val lastUpdatedTimestamp = data.timeStamp
                    if (lastUpdatedTimestamp == 0L) {
                        binding.tvLastUpdate.text = ""
                    } else {
                        binding.tvLastUpdate.text =
                            "Updated ${DateFormats.getRelativeTime(lastUpdatedTimestamp)}"
                    }
                } else {
                    binding.tvLastUpdate.text = ""
                }
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.StressGraphClicked)
            }

        }
    }

    class InfoRingCareViewHolder(private val binding: ListRingCareBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(data: OHealthOverview.InfoRingCare) {
            binding.tvTitle.text = data.data.title
            binding.tvMessage.text = data.data.content

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.TextRingCareClicked(data.data.title))
            }

        }
    }

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
                val nudge = data.data.nudges.firstOrNull()
                binding.tvDayStatus.text = nudge?.label ?: ""
                binding.tvNudge.text = nudge?.message ?: ""
            }

            if (data.data.readinessNapScoreImpact == null || data.data.readinessNapScoreImpact == 0) {
                binding.lytNapLabel.root.gone()
            } else {
                binding.lytNapLabel.root.visible()
                binding.lytNapLabel.tvNapUpdatedScore.text =
                    if ((data.data.readinessNapScoreImpact ?: 0) >= 0) {
                        "+${data.data.readinessNapScoreImpact}"
                    } else {
                        "${data.data.readinessNapScoreImpact}"
                    }
                binding.lytNapLabel.tvNapUpdatedScore.setTextColor(
                    binding.lytNapLabel.tvNapUpdatedScore.context.getColor(
                        R.color.nap_dash_readiness_score
                    )
                )
                binding.lytNapLabel.tvNapCountMsg.text = "after ${data.data.noOfNaps} nap"
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
                binding.tvTitle.gone()
                binding.tvTodayDesc.text = ""
                (binding.tvTodayDesc.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = binding.tvTodayDesc.context.dpToPx(24)
                    bottomMargin = 0
                }
            } else {
                binding.tvTitle.visible()
                (binding.tvTodayDesc.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = binding.tvTodayDesc.context.dpToPx(8)
                    bottomMargin = binding.tvTodayDesc.context.dpToPx(26)
                }
                val nudge = data.data.nudges.firstOrNull()
                binding.tvTodayDesc.text = nudge?.message ?: ""
                binding.tvTitle.text = nudge?.label ?: ""
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

            if (data.data.readinessNapScoreImpact == null || data.data.readinessNapScoreImpact == 0) {
                binding.lytNapLabel.root.gone()
            } else {
                binding.lytNapLabel.root.visible()
                binding.lytNapLabel.tvNapUpdatedScore.text =
                    if ((data.data.readinessNapScoreImpact ?: 0) >= 0) {
                        "+${data.data.readinessNapScoreImpact}"
                    } else {
                        "${data.data.readinessNapScoreImpact}"
                    }
                binding.lytNapLabel.tvNapUpdatedScore.setTextColor(
                    binding.lytNapLabel.tvNapUpdatedScore.context.getColor(
                        R.color.nap_dash_readiness_score
                    )
                )
                binding.lytNapLabel.tvNapCountMsg.text = "after ${data.data.noOfNaps} nap"
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

            binding.tvSleepScore.text = scoreValue.toString()
            binding.tvSleepStatus.text = data.data.status

            val (hourTimeInBed, minuteTimeInBed) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                data.data.totalSleep ?: 0
            )
            binding.tvSleepTime.text = if (hourTimeInBed == 0) {
                "$minuteTimeInBed min"
            } else {
                "$hourTimeInBed hr $minuteTimeInBed min"
            }

            if (data.data.sleepNapScoreImpact == null || data.data.sleepNapScoreImpact == 0) {
                binding.lytNapLabel.root.gone()
            } else {
                binding.lytNapLabel.root.visible()
                binding.lytNapLabel.tvNapUpdatedScore.text =
                    if ((data.data.sleepNapScoreImpact ?: 0) >= 0) {
                        "+${data.data.sleepNapScoreImpact}"
                    } else {
                        "${data.data.sleepNapScoreImpact}"
                    }
                binding.lytNapLabel.tvNapUpdatedScore.setTextColor(
                    binding.lytNapLabel.tvNapUpdatedScore.context.getColor(
                        R.color.nap_dash_sleep_score
                    )
                )
                binding.lytNapLabel.tvNapCountMsg.text = "after ${data.data.noOfNaps} nap"
            }

            val sleepDayGraphView = SleepProgressbarView(binding.sleepPgbr.context)
            binding.sleepPgbr.removeAllViews()
            binding.sleepPgbr.addView(sleepDayGraphView)

            sleepDayGraphView.setData(data.sleepArray)

            binding.tvSleepStart.text = DateFormats.formatDate(
                data.data.startTime,
                DateFormats.dateTimeFormat5,
                DateFormats.time12Meridian
            )
            binding.tvSleepEnd.text = DateFormats.formatDate(
                data.data.endTime,
                DateFormats.dateTimeFormat5,
                DateFormats.time12Meridian
            )

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
                    data.startTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.time12Meridian
                )
                binding.tvSleepEnd.text = DateFormats.formatDate(
                    data.endTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.time12Meridian
                )

                binding.tvHrValue.text = if (data.data.restingHr == null) {
                    "--"
                } else {
                    data.data.restingHr.toString()
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

            if (data.data.sleepNapScoreImpact == null || data.data.sleepNapScoreImpact == 0) {
                binding.lytNapLabel.root.gone()
            } else {
                binding.lytNapLabel.root.visible()
                binding.lytNapLabel.tvNapUpdatedScore.text =
                    if ((data.data.sleepNapScoreImpact ?: 0) >= 0) {
                        "+${data.data.sleepNapScoreImpact}"
                    } else {
                        "${data.data.sleepNapScoreImpact}"
                    }
                binding.lytNapLabel.tvNapUpdatedScore.setTextColor(
                    binding.lytNapLabel.tvNapUpdatedScore.context.getColor(
                        R.color.nap_dash_sleep_score
                    )
                )
                binding.lytNapLabel.tvNapCountMsg.text = "after ${data.data.noOfNaps} nap"
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
            binding.pbCurrent.progress =
                (data.data.activeCalories ?: 0) * 2//For 50 kcal only, change accordingly

            binding.tvTotalCalories.text = "${data.caloriesGoal}"

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick)
            }

        }
    }

    class ActivityViewHolder2(private val binding: ListActivityBurnCardItem2Binding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Activity,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {
            val scoreValue = data.data.activityScore

            if (scoreValue == null) {
                binding.tvActivityScore.text = "--"
            } else {
                binding.tvActivityScore.text = scoreValue.toString()
            }


            if (data.data.nudges.isNullOrEmpty()) {
                binding.tvNudge.gone()
            } else {
                binding.tvNudge.visible()
                binding.tvNudge.text = data.data.nudges.firstOrNull()?.message ?: ""
            }

            val caloriesGoalText = "${data.caloriesGoal}"
            binding.tvTotalCalories.text = caloriesGoalText

            binding.tvActiveCalories.text = if ((data.data.activeCalories ?: 0) > 0) {
                data.data.activeCalories.toString()
            } else {
                "--"
            }

            binding.dynamicArcView.deleteAll()

            binding.dynamicArcView.configureAngles(180, 0)
            binding.dynamicArcView.addSeries(
                seriesItemWithoutInset(
                    binding.dynamicArcView.context, 100f, 100f, R.color.activity_track_back, 18f
                )
            )

            val distanceIndex: Int = binding.dynamicArcView.addSeries(
                seriesItemWithoutInset(
                    binding.dynamicArcView.context, 0f, 100f, R.color.activity_arc, 18f
                )
            )

            var caloriesPercent =
                ((data.data.activeCalories ?: 0).toFloat() / data.caloriesGoal.toFloat()) * 100

            if (caloriesPercent > 100) {
                caloriesPercent = 100f
            }

            binding.dynamicArcView.addEvent(
                DecoEvent.Builder(caloriesPercent).setIndex(distanceIndex)
                    .setDuration(1000L).build()
            )

            binding.tvSteps.text = if (data.data.steps == 0) "-" else data.data.steps.toString()

            /* val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(
                 data.data.inactiveMinutes ?: 0
             )*/

            /* if (hour > 0) {
                 binding.tvHr.visible()
                 binding.textHr.visible()
                 binding.tvMin.visible()
                 binding.textMin.visible()

                 binding.tvHr.text = "$hour"
                 binding.tvMin.text = "$minute"
             } else if (minute > 0) {
                 binding.tvHr.gone()
                 binding.textHr.gone()
                 binding.tvMin.visible()
                 binding.textMin.visible()

                 binding.tvMin.text = "$minute"
             } else {
                 binding.tvHr.gone()
                 binding.textHr.gone()
                 binding.tvMin.visible()
                 binding.textMin.gone()

                 binding.tvMin.text = "-"
             }*/


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
                binding.tvTodayDesc.text = data.data.nudges.firstOrNull()?.message ?: ""
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

    class CycleTrackerPredictViewHolder(private val binding: ListCycleTrackerPredictionBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.CycleTrackerPredict,
            position: Int,
        ) {
        }
    }

    class CycleTrackerOngoingViewHolder(private val binding: ListCycleTrackerOngoingBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.CycleTrackerOngoing,
            position: Int,
        ) {
        }
    }

    class TrackYourCycleViewHolder(private val binding: CardTrackFmHealthBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.CardTrackFemaleHealth,
            position: Int,
        ) {
            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.TrackYourFemaleHealth)
            }
            binding.btnGetStarted.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.TrackYourFemaleHealth)
            }
        }
    }

    class GotYourPeriodViewHolder(private val binding: ListPeriodConfirmationBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.GotYourPeriod,
            position: Int,
        ) {
        }
    }


}

interface AlertClickListener {
    fun onAlertClicked(alertType: AlertType)
}



