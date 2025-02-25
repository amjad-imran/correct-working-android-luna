package com.oreo.ui.home.summary

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CardTrackFmHealthBinding
import com.noisefit.luna.databinding.ItemStressGraphBinding
import com.noisefit.luna.databinding.LayoutChatCardDashBinding
import com.noisefit.luna.databinding.LayoutDashHealthMonitorBinding
import com.noisefit.luna.databinding.LayoutDashSleepPlannerCardBinding
import com.noisefit.luna.databinding.ListActivityBurnCardItem2Binding
import com.noisefit.luna.databinding.ListActivityBurnCardItemBinding
import com.noisefit.luna.databinding.ListActivityMinimalItemBinding
import com.noisefit.luna.databinding.ListCycleTrackerOngoingBinding
import com.noisefit.luna.databinding.ListCycleTrackerPredictionBinding
import com.noisefit.luna.databinding.ListDashGotPeriodBinding
import com.noisefit.luna.databinding.ListDashNapBinding
import com.noisefit.luna.databinding.ListOWAlertCardItemBinding
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
import com.noisefit_commans.data.model.SleepCardDashState
import com.noisefit_commans.ui.custom.SleepProgressbarView
import com.noisefit_commans.ui.getColor
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.oreo.data.model.AlertType
import com.oreo.data.model.DashAlert
import com.oreo.data.model.FemaleHealthCardState
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.VideoInfoType
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.util.DateTimeUtil
import com.oreo.util.UtilClass.seriesItemWithoutInset
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs


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


    object OnAiCardClicked : OSummaryHealthOverviewClickEnum()
    object OnSleepPlannerCardClicked : OSummaryHealthOverviewClickEnum()
    object OnSleepPlannerAlarmClicked : OSummaryHealthOverviewClickEnum()
    object OnSleepPlannerBreathingClicked : OSummaryHealthOverviewClickEnum()
    class OnHealthMonitorCardClicked(val data: HealthTrend) : OSummaryHealthOverviewClickEnum()
    object AutoSportsDelete : OSummaryHealthOverviewClickEnum()

    object WorkoutAlertWhatisThis : OSummaryHealthOverviewClickEnum()

    object WorkoutAlertIdentify : OSummaryHealthOverviewClickEnum()
    object TrackYourFemaleHealth : OSummaryHealthOverviewClickEnum()
    object TrackYourFemaleHealthRemindLater : OSummaryHealthOverviewClickEnum()
    object FemaleHealthHome : OSummaryHealthOverviewClickEnum()
    class GotPeriodClicked(val status: Boolean) : OSummaryHealthOverviewClickEnum()

}

class OSummaryHealthOverviewAdapter() : RecyclerView.Adapter<HomeRecyclerViewHolder>() {

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

    var itemClickListener: ((type: OSummaryHealthOverviewClickEnum) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        return when (viewType) {
            R.layout.layout_dash_health_monitor -> HomeRecyclerViewHolder.DashHealthMonitorViewHolder(
                LayoutDashHealthMonitorBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.layout_chat_card_dash -> HomeRecyclerViewHolder.AiCardViewHolder(
                LayoutChatCardDashBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_dash_nap -> HomeRecyclerViewHolder.NapWidgetCardViewHolder(
                ListDashNapBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.item_stress_graph -> HomeRecyclerViewHolder.StressGraphViewHolder(
                ItemStressGraphBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_video_info_card -> HomeRecyclerViewHolder.InfoVideoCardViewHolder(
                ListVideoInfoCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_welcome_card -> HomeRecyclerViewHolder.InfoWelcomeCardViewHolder(
                ListWelcomeCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_ring_care -> HomeRecyclerViewHolder.InfoRingCareViewHolder(
                ListRingCareBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_readiness_card_item -> HomeRecyclerViewHolder.ReadinessViewHolder(
                ListReadinessCardItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_sleep_card_item -> HomeRecyclerViewHolder.SleepViewHolder(
                ListSleepCardItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_sleep_minimal_item -> HomeRecyclerViewHolder.SleepMinimalViewHolder(
                ListSleepMinimalItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_sleep_waiting_card_item -> HomeRecyclerViewHolder.SleepWaitingViewHolder(
                ListSleepWaitingCardItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_activity_burn_card_item -> HomeRecyclerViewHolder.ActivityViewHolder(
                ListActivityBurnCardItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_activity_burn_card_item_2 -> HomeRecyclerViewHolder.ActivityViewHolder2(
                ListActivityBurnCardItem2Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_activity_minimal_item -> HomeRecyclerViewHolder.ActivityMinimalViewHolder(
                ListActivityMinimalItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_readiness_minimal_card_item -> HomeRecyclerViewHolder.ReadinessMinimalViewHolder(
                ListReadinessMinimalCardItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )


            R.layout.list_o_w_alert_card_item -> HomeRecyclerViewHolder.AutoSportViewHolder(
                ListOWAlertCardItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_cycle_tracker_prediction -> HomeRecyclerViewHolder.CycleTrackerCardSmallViewHolder(
                ListCycleTrackerPredictionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_cycle_tracker_ongoing -> HomeRecyclerViewHolder.CycleTrackerCardBigViewHolder(
                ListCycleTrackerOngoingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.card_track_fm_health -> HomeRecyclerViewHolder.TrackYourCycleViewHolder(
                CardTrackFmHealthBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.list_dash_got_period -> HomeRecyclerViewHolder.GotYourPeriodViewHolder(
                ListDashGotPeriodBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.layout_dash_sleep_planner_card -> {
                HomeRecyclerViewHolder.SleepPlannerViewHolder(
                    LayoutDashSleepPlannerCardBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

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
                items[position] as OHealthOverview.Activity, position, lastPosition, devicePaired
            )

            is HomeRecyclerViewHolder.ActivityViewHolder2 -> holder.bind(
                items[position] as OHealthOverview.Activity, position, lastPosition, devicePaired
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
                items[position] as OHealthOverview.Readiness, position, lastPosition, devicePaired
            )

            is HomeRecyclerViewHolder.SleepViewHolder -> holder.bind(
                items[position] as OHealthOverview.Sleep, position, lastPosition, devicePaired
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

            is HomeRecyclerViewHolder.CycleTrackerCardSmallViewHolder -> holder.bind(
                items[position] as OHealthOverview.CycleTrackerCardSmall,
                position,
            )

            is HomeRecyclerViewHolder.CycleTrackerCardBigViewHolder -> holder.bind(
                items[position] as OHealthOverview.CycleTrackerCardBig,
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

            is HomeRecyclerViewHolder.AiCardViewHolder -> {
                holder.bind(items[position] as OHealthOverview.LunaAiCard)
            }

            is HomeRecyclerViewHolder.SleepPlannerViewHolder -> {
                holder.bind(items[position] as OHealthOverview.SleepPlannerCard)
            }

            is HomeRecyclerViewHolder.DashHealthMonitorViewHolder -> {
                holder.bind(items[position] as OHealthOverview.HealthMonitorCard)
            }
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
            is OHealthOverview.LunaAiCard -> R.layout.layout_chat_card_dash
            is OHealthOverview.CycleTrackerCardSmall -> R.layout.list_cycle_tracker_prediction
            is OHealthOverview.CycleTrackerCardBig -> R.layout.list_cycle_tracker_ongoing
            is OHealthOverview.CardTrackFemaleHealth -> R.layout.card_track_fm_health
            is OHealthOverview.GotYourPeriod -> R.layout.list_dash_got_period
            is OHealthOverview.HealthMonitorCard -> R.layout.layout_dash_health_monitor
            is OHealthOverview.SleepPlannerCard -> R.layout.layout_dash_sleep_planner_card
        }
    }

    fun removeCycleGetStartedCard() {

        val index = (items as ArrayList).indexOfFirst {
            it is OHealthOverview.CardTrackFemaleHealth
        }
        if (index != -1) {
            (items as ArrayList).removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun removeGotPeriodCard() {
        val index = (items as ArrayList).indexOfFirst {
            it is OHealthOverview.GotYourPeriod
        }
        if (index != -1) {
            (items as ArrayList).removeAt(index)
            notifyItemRemoved(index)
        }
    }

}


sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((type: OSummaryHealthOverviewClickEnum) -> Unit)? = null


    class DashHealthMonitorViewHolder(private val binding: LayoutDashHealthMonitorBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.HealthMonitorCard,
        ) {
            val hasHealthData = hasHealthData(data.data)

            if (hasHealthData) {
                val healthData = data.data
                binding.tvNudge.visible()
                binding.tvNudge.text = data.data.nudge


                if (!healthData.bloodOxy?.status.isNullOrEmpty()) {
                    binding.imvSpo2.setImageResource(
                        getHealthTrendIcon(
                            healthData.bloodOxy?.status
                        )
                    )
                } else {
                    binding.imvSpo2.setImageResource(R.drawable.ic_hm_check_default)
                }

                if (!healthData.hrv?.status.isNullOrEmpty()) {
                    binding.imvHrv.setImageResource(getHealthTrendIcon(healthData.hrv?.status))
                } else {
                    binding.imvHrv.setImageResource(R.drawable.ic_hm_check_default)
                }

                if (!healthData.rhr?.status.isNullOrEmpty()) {
                    binding.imvRHR.setImageResource(getHealthTrendIcon(healthData.rhr?.status))
                } else {
                    binding.imvRHR.setImageResource(R.drawable.ic_hm_check_default)
                }

                if (!healthData.skinTemp?.status.isNullOrEmpty()) {
                    binding.imvSkin.setImageResource(
                        getHealthTrendIcon(
                            healthData.skinTemp?.status
                        )
                    )
                } else {
                    binding.imvSkin.setImageResource(R.drawable.ic_hm_check_default)
                }

                if (!healthData.resp?.status.isNullOrEmpty()) {
                    binding.imvResp.setImageResource(getHealthTrendIcon(healthData.resp?.status))
                } else {
                    binding.imvResp.setImageResource(R.drawable.ic_hm_check_default)
                }
            } else {
                binding.apply {
                    imvResp.setImageResource(R.drawable.ic_hm_check_default)
                    imvRHR.setImageResource(R.drawable.ic_hm_check_default)
                    imvSpo2.setImageResource(R.drawable.ic_hm_check_default)
                    imvHrv.setImageResource(R.drawable.ic_hm_check_default)
                    imvSkin.setImageResource(R.drawable.ic_hm_check_default)
                    tvNudge.visible()
                    tvNudge.text = tvNudge.context.getString(R.string.text_no_data_so_far)
                }
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.OnHealthMonitorCardClicked(
                        data.data
                    )
                )
            }
        }

        fun hasHealthData(healthTrend: HealthTrend?): Boolean {
            if (healthTrend == null) return false
            return !(healthTrend.resp?.status.isNullOrEmpty() && healthTrend.rhr?.status.isNullOrEmpty() && healthTrend.bloodOxy?.status.isNullOrEmpty() && healthTrend.hrv?.status.isNullOrEmpty() && healthTrend.skinTemp?.status.isNullOrEmpty())

        }

        fun getHealthTrendState(status: String?): Int {
            val drawable: Int = if (status.equals("warning", true)) {
                2
            } else if (status.equals("good", true)) {
                1
            } else if (status.equals("optimal", true)) {
                0
            } else {
                1
            }
            return drawable
        }

        fun getHealthTrendIcon(status: String?): Int {
            val drawable: Int = if (status.equals("warning", true)) {
                R.drawable.ic_health_warning
            } else if (status.equals("good", true)) {
                R.drawable.ic_health_good
            } else if (status.equals("optimal", true)) {
                R.drawable.ic_health_optimal
            } else if (status.equals("calibrating", true)) {
                R.drawable.ic_hm_check_default
            } else {
                R.drawable.ic_health_good
            }
            return drawable
        }
    }

    class SleepPlannerViewHolder(private val binding: LayoutDashSleepPlannerCardBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.SleepPlannerCard,
        ) {

            binding.apply {
                val plannerData = data.data.planner!!

                lytBedTime.ivIcon.setImageResource(R.drawable.ic_bedtime_gray)
                lytBedTime.tvTitle.text =
                    "Bed time"//binding.root.context.getString(R.string.text_bedtime)//"Bed time"
                lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_gray)
                lytWakeupTime.tvTitle.text =
                    "Wake time"//binding.root.context.getString(R.string.text_wake_time)//"Wake time"

                val bedTime = LocalTime.parse(
                    plannerData.planner?.bed_time ?: "22:00:00",
                    DateTimeFormatter.ofPattern("HH:mm:ss")
                )
                val wakeTime = LocalTime.parse(
                    plannerData.planner?.wake_time ?: "06:00:00",
                    DateTimeFormatter.ofPattern("HH:mm:ss")
                )

                lytBedTime.tvTime.text = bedTime.format(DateTimeFormatter.ofPattern("hh:mm"))
                lytBedTime.tvTimeUnit.text =
                    bedTime.format(
                        DateTimeFormatter.ofPattern(
                            "a",
                            Locale("en")
                        )
                    ).lowercase()

                lytWakeupTime.tvTime.text = wakeTime.format(DateTimeFormatter.ofPattern("hh:mm"))
                lytWakeupTime.tvTimeUnit.text =
                    wakeTime.format(DateTimeFormatter.ofPattern("a", Locale("en"))).lowercase()

                tvMsg.text = plannerData.planner?.nudge

                clock.setData(bedTime, wakeTime, (plannerData.planner?.debt ?: 0) / 60)

                val durationMinutes = getDurationMinutes(bedTime, wakeTime)
                val hours = durationMinutes / 60
                val minutes = durationMinutes % 60

                tvDuration.text = String.format("%d:%02d", hours, minutes)

                tvDuration.setTextColor(Color.parseColor("#FFFFFF"))
                val textShader: Shader = LinearGradient(
                    0f,
                    tvDuration.paint.measureText(tvDuration.text.toString()),
                    0f,
                    0f,
                    intArrayOf(
                        Color.parseColor("#D5B6FF"),
                        Color.parseColor("#D5B6FF"),
                        Color.parseColor("#FFFFFF"),
                    ),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                tvDuration.paint.shader = textShader

                when (data.data.dashState) {
                    SleepCardDashState.SetAlarm -> {
                        this.divider1.root.visible()
                        this.lytSetAlarm.apply {
                            ivAlarmMore.visible()
                            tvAlarmTime.gone()
                            tvSetUpAlarm.text =
                                this.root.context.getString(R.string.text_set_up_alarm)
                            tvSetUpAlarm.setTextColor(Color.parseColor("#88b0ff"))
                            root.visible()
                        }
                        this.lytBreathe.root.gone()
                    }

                    SleepCardDashState.BreathingExercise -> {
                        this.divider1.root.visible()
                        this.lytSetAlarm.root.gone()
                        this.lytBreathe.root.visible()
                    }

                    SleepCardDashState.None -> {
                        this.divider1.root.gone()
                        this.lytSetAlarm.root.gone()
                        this.lytBreathe.root.gone()
                    }

                    is SleepCardDashState.AlarmSet -> {
                        this.divider1.root.visible()
                        this.lytSetAlarm.apply {
                            ivAlarmMore.gone()
                            tvAlarmTime.visible()
                            tvSetUpAlarm.text = root.context.getString(R.string.text_alarm_set_for)
                            tvSetUpAlarm.setTextColor(Color.parseColor("#FFFFFF"))
                            tvAlarmTime.text = LocalTime.parse(
                                (data.data.dashState as SleepCardDashState.AlarmSet).data.wake_time,
                                DateTimeFormatter.ofPattern("HH:mm:ss")
                            ).format(DateTimeFormatter.ofPattern("hh:mm a")).lowercase()
                            root.visible()
                        }
                        this.lytBreathe.root.gone()
                    }
                }

                root.visible()
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnSleepPlannerCardClicked)
            }
            binding.lytSetAlarm.root.setOnClickListener {
                if (data.data.dashState !is SleepCardDashState.AlarmSet) {
                    itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnSleepPlannerAlarmClicked)
                }
            }
            binding.lytBreathe.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnSleepPlannerBreathingClicked)
            }
        }

        fun getDurationMinutes(start: LocalTime, end: LocalTime): Long {
            return if (end.isAfter(start)) {
                Duration.between(start, end).toMinutes()
            } else {
                val dayEnd = LocalTime.of(23, 59)
                val dayStart = LocalTime.of(0, 0)
                Duration.between(start, dayEnd).toMinutes() + 1 + Duration.between(dayStart, end)
                    .toMinutes()
            }
        }
    }

    class AiCardViewHolder(private val binding: LayoutChatCardDashBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.LunaAiCard,
        ) {
            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnAiCardClicked)
            }
        }
    }

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
                binding.ivBack.context, when (data.type) {
                    VideoInfoType.SLEEP -> R.drawable.back_info_sleep
                    VideoInfoType.READINESS -> R.drawable.back_info_readiness
                    VideoInfoType.ACTIVITY -> R.drawable.back_info_activity
                }
            )

            binding.root.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.VideoInfoClicked(
                        data.type, data.data.url
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

            binding.tvStressValue.gone()
            binding.tvStressStatus.gone()
            binding.tvLastUpdate.gone()

            /*if (data.value == 0) {
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
                            binding.tvLastUpdate.context.getString(
                                R.string.text_updated_value,
                                DateTimeUtil.getRelativeTime(
                                    lastUpdatedTimestamp,
                                    resourcesProvider = null,
                                    context = binding.tvLastUpdate.context
                                )
                            )
                    }
                } else {
                    binding.tvLastUpdate.text = ""
                }
            }*/

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
            val statusColor = ContextCompat.getColor(
                binding.root.context,
                getStatusColors(data.data.statusCode ?: "")
            )

            binding.tvScoreValue.setTextColor(statusColor)
            binding.tvScoreValue.text = data.data.status

            if (data.data.nudges.isNullOrEmpty()) {
                binding.tvNudge.text = ""
            } else {
                val nudge = data.data.nudges.firstOrNull()
                binding.tvDayStatus.text = nudge?.label ?: ""
                binding.tvNudge.text = nudge?.message ?: ""
            }

            if (data.data.totalScoreImpact == null || data.data.totalScoreImpact == 0) {
                binding.lytNapLabel.root.gone()
            } else {
                val napCount = data.data.noOfNaps ?: 0
                val sleepCount = data.data.noOfSleeps ?: 0
                if ((napCount == 1 && sleepCount == 0) || (sleepCount == 1 && napCount == 0)) {
                    binding.lytNapLabel.root.gone()
                } else {
                    binding.lytNapLabel.root.visible()
                    binding.lytNapLabel.tvNapUpdatedScore.text =
                        if ((data.data.totalScoreImpact ?: 0) >= 0) {
                            "+${data.data.totalScoreImpact}"
                        } else {
                            "${data.data.totalScoreImpact}"
                        }
                    binding.lytNapLabel.tvNapUpdatedScore.setTextColor(
                        binding.lytNapLabel.tvNapUpdatedScore.context.getColor(
                            R.color.nap_dash_readiness_score
                        )
                    )


                    val string = StringBuilder()
                    string.append(binding.root.context.getString(R.string.text_after))
                    string.append(" ")

                    if (napCount > 0) {
                        if (sleepCount == 0) {
                            string.append("${(napCount - 1)} ")
                            string.append(
                                binding.root.context.getString(R.string.text_nap).lowercase()
                            )
                        } else {
                            string.append("$napCount ")
                            string.append(
                                binding.root.context.getString(R.string.text_nap).lowercase()
                            )
                        }
                    }

                    if (sleepCount > 1) {
                        if (napCount > 0) {
                            string.append(" & ")
                        }
                        string.append("${(sleepCount - 1)} ")
                        string.append(
                            binding.root.context.getString(R.string.text_sleep).lowercase()
                        )
                    }

                    binding.lytNapLabel.tvNapCountMsg.text = string.toString()
                }


            }

            val impact = data.data.impact

            if (impact != null) {
                binding.lytTrend.apply {
                    if (impact >= 0) {
                        ivTrend.setImageResource(R.drawable.ic_trend_up_dash)
                        tvPercent.setTextColor(Color.parseColor("#0EF377"))
                    } else {
                        ivTrend.setImageResource(R.drawable.ic_trend_down_dash)
                        tvPercent.setTextColor(Color.parseColor("#FF426F"))
                    }
                    tvPercent.text = "${abs(impact)}%"
                    root.visible()
                }
            } else {
                binding.lytTrend.root.gone()
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ReadinessDetailsWorkoutClick)
            }

        }
    }

    class ReadinessViewHolder(private val binding: ListReadinessCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Readiness, position: Int, lastPosition: Int, devicePaired: Boolean
        ) {

            //binding.imv.loadImage(binding.imv.context, R.drawable.ic_readiness_card_bg1)

            val scoreValue = data.data.readinessScore ?: 0
            if (scoreValue == 0) {
                binding.tvValue.text = "--"
                binding.tvStatus.text = binding.tvStatus.context.getString(R.string.text_no_data)
//                binding.tvTodayDesc.text = ""
//                binding.tvTodayDesc.invisible()

            } else {
                binding.tvValue.text = scoreValue.toString()
                val statusColor = ContextCompat.getColor(
                    binding.root.context,
                    getStatusColors(data.data.statusCode ?: "")
                )

                binding.tvStatus.setTextColor(statusColor)
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

            if (data.data.totalScoreImpact == null || data.data.totalScoreImpact == 0) {
                binding.lytNapLabel.root.gone()
            } else {
                val napCount = data.data.noOfNaps ?: 0
                val sleepCount = data.data.noOfSleeps ?: 0

                if ((napCount == 1 && sleepCount == 0) || (sleepCount == 1 && napCount == 0)) {
                    binding.lytNapLabel.root.gone()
                } else {
                    binding.lytNapLabel.root.visible()
                    binding.lytNapLabel.tvNapUpdatedScore.text =
                        if ((data.data.totalScoreImpact ?: 0) >= 0) {
                            "+${data.data.totalScoreImpact}"
                        } else {
                            "${data.data.totalScoreImpact}"
                        }
                    binding.lytNapLabel.tvNapUpdatedScore.setTextColor(
                        binding.lytNapLabel.tvNapUpdatedScore.context.getColor(
                            R.color.nap_dash_readiness_score
                        )
                    )


                    val string = StringBuilder()
                    string.append(binding.root.context.getString(R.string.text_after))
                    string.append(" ")

                    if (napCount > 0) {
                        if (sleepCount == 0) {
                            string.append("${(napCount - 1)} ")
                            string.append(
                                binding.root.context.getString(R.string.text_nap).lowercase()
                            )
                        } else {
                            string.append("$napCount ")
                            string.append(
                                binding.root.context.getString(R.string.text_nap).lowercase()
                            )
                        }
                    }

                    if (sleepCount > 1) {
                        if (napCount > 0) {
                            string.append(" & ")
                        }
                        string.append("${(sleepCount - 1)} ")
                        string.append(
                            binding.root.context.getString(R.string.text_sleep).lowercase()
                        )
                    }

                    binding.lytNapLabel.tvNapCountMsg.text = string.toString()
                }


            }

            val impact = data.data.impact

            if (impact != null) {
                binding.lytTrend.apply {
                    if (impact >= 0) {
                        ivTrend.setImageResource(R.drawable.ic_trend_up_dash)
                        tvPercent.setTextColor(Color.parseColor("#0EF377"))
                    } else {
                        ivTrend.setImageResource(R.drawable.ic_trend_down_dash)
                        tvPercent.setTextColor(Color.parseColor("#FF426F"))
                    }
                    tvPercent.text = "${abs(impact)}%"
                    root.visible()
                }
            } else {
                binding.lytTrend.root.gone()
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
            val statusColor = ContextCompat.getColor(
                binding.root.context,
                getStatusColors(data.data.statusCode ?: "")
            )

            binding.tvSleepStatus.setTextColor(statusColor)

            val (hourTimeInBed, minuteTimeInBed) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                data.data.totalSleep ?: 0
            )
            binding.tvSleepTime.text = if (hourTimeInBed == 0) {
                "$minuteTimeInBed min"
            } else {
                "$hourTimeInBed hr $minuteTimeInBed min"
            }

            if (data.data.totalScoreImpact == null || data.data.totalScoreImpact == 0) {
                binding.lytNapLabel.root.gone()
            } else {
                val napCount = data.data.noOfNaps ?: 0
                val sleepCount = data.data.noOfSleeps ?: 0

                if ((napCount == 1 && sleepCount == 0) || (sleepCount == 1 && napCount == 0)) {
                    binding.lytNapLabel.root.gone()
                } else {
                    binding.lytNapLabel.root.visible()
                    binding.lytNapLabel.tvNapUpdatedScore.text =
                        if ((data.data.totalScoreImpact ?: 0) >= 0) {
                            "+${data.data.totalScoreImpact}"
                        } else {
                            "${data.data.totalScoreImpact}"
                        }
                    binding.lytNapLabel.tvNapUpdatedScore.setTextColor(
                        binding.lytNapLabel.tvNapUpdatedScore.context.getColor(
                            R.color.nap_dash_sleep_score
                        )
                    )


                    val string = StringBuilder()
                    string.append(binding.root.context.getString(R.string.text_after))
                    string.append(" ")

                    if (napCount > 0) {
                        if (sleepCount == 0) {
                            string.append("${(napCount - 1)} ")
                            string.append(
                                binding.root.context.getString(R.string.text_nap).lowercase()
                            )
                        } else {
                            string.append("$napCount ")
                            string.append(
                                binding.root.context.getString(R.string.text_nap).lowercase()
                            )
                        }
                    }

                    if (sleepCount > 1) {
                        if (napCount > 0) {
                            string.append(" & ")
                        }
                        string.append("${(sleepCount - 1)} ")
                        string.append(
                            binding.root.context.getString(R.string.text_sleep).lowercase()
                        )
                    }


                    binding.lytNapLabel.tvNapCountMsg.text = string.toString()
                }

            }

            val sleepDayGraphView = SleepProgressbarView(binding.sleepPgbr.context)
            binding.sleepPgbr.removeAllViews()
            binding.sleepPgbr.addView(sleepDayGraphView)

            sleepDayGraphView.setData(data.sleepArray)

            binding.tvSleepStart.text = DateFormats.formatDate(
                data.data.startTime, DateFormats.dateTimeFormat5(), DateFormats.time12Meridian()
            )
            binding.tvSleepEnd.text = DateFormats.formatDate(
                data.data.endTime, DateFormats.dateTimeFormat5(), DateFormats.time12Meridian()
            )

            val impact = data.impact

            if (impact != null) {
                binding.lytTrend.apply {
                    if (impact >= 0) {
                        ivTrend.setImageResource(R.drawable.ic_trend_up_dash)
                        tvPercent.setTextColor(Color.parseColor("#0EF377"))
                    } else {
                        ivTrend.setImageResource(R.drawable.ic_trend_down_dash)
                        tvPercent.setTextColor(Color.parseColor("#FF426F"))
                    }
                    tvPercent.text = "${abs(impact)}%"
                    root.visible()
                }
            } else {
                binding.lytTrend.root.gone()
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.SleepDetailsWorkoutClick)
            }

        }
    }


    class SleepViewHolder(private val binding: ListSleepCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Sleep, position: Int, lastPosition: Int, devicePaired: Boolean
        ) {
            //binding.imv.loadImage(binding.imv.context, R.drawable.ic_sleep_card_bg1)
            val scoreValue = data.data.sleepScore ?: 0
            if (scoreValue <= 0) {
                binding.tvSleepStart.text = binding.root.context.getString(R.string.text_start_time)
                binding.tvSleepEnd.text = binding.root.context.getString(R.string.text_end_time)
                binding.tvSleepStart.setTextColor(R.color.white_80.getColor())
                binding.tvSleepEnd.setTextColor(R.color.white_80.getColor())
                binding.tvValue.text = "--"
                binding.tvStatus.text = binding.root.context.getString(R.string.text_no_data)
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
                    data.startTime, DateFormats.dateTimeFormat5(), DateFormats.time12Meridian()
                )
                binding.tvSleepEnd.text = DateFormats.formatDate(
                    data.endTime, DateFormats.dateTimeFormat5(), DateFormats.time12Meridian()
                )

                binding.tvHrValue.text =
                    if (data.data.restingHr == null || data.data.restingHr == 0) {
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
                val statusColor = ContextCompat.getColor(
                    binding.root.context,
                    getStatusColors(data.data.statusCode ?: "")
                )

                binding.tvStatus.setTextColor(statusColor)

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

            if (data.data.totalScoreImpact == null || data.data.totalScoreImpact == 0) {
                binding.lytNapLabel.root.gone()
            } else {

                val napCount = data.data.noOfNaps ?: 0
                val sleepCount = data.data.noOfSleeps ?: 0

                if ((napCount == 1 && sleepCount == 0) || (sleepCount == 1 && napCount == 0)) {
                    binding.lytNapLabel.root.gone()
                } else {
                    binding.lytNapLabel.root.visible()
                    binding.lytNapLabel.tvNapUpdatedScore.text =
                        if ((data.data.totalScoreImpact ?: 0) >= 0) {
                            "+${data.data.totalScoreImpact}"
                        } else {
                            "${data.data.totalScoreImpact}"
                        }
                    binding.lytNapLabel.tvNapUpdatedScore.setTextColor(
                        binding.lytNapLabel.tvNapUpdatedScore.context.getColor(
                            R.color.nap_dash_sleep_score
                        )
                    )

                    val string = StringBuilder()
                    string.append(binding.root.context.getString(R.string.text_after))
                    string.append(" ")

                    if (napCount > 0) {
                        if (sleepCount == 0) {
                            string.append("${(napCount - 1)} ")
                            string.append(
                                binding.root.context.getString(R.string.text_nap).lowercase()
                            )
                        } else {
                            string.append("$napCount ")
                            string.append(
                                binding.root.context.getString(R.string.text_nap).lowercase()
                            )

                        }
                    }

                    if (sleepCount > 1) {
                        if (napCount > 0) {
                            string.append(" & ")
                        }
                        string.append("${(sleepCount - 1)} ")
                        string.append(
                            binding.root.context.getString(R.string.text_sleep).lowercase()
                        )
                    }

                    binding.lytNapLabel.tvNapCountMsg.text = string.toString()
                }

            }


            val impact = data.impact

            if (impact != null) {
                binding.lytTrend.apply {
                    if (impact >= 0) {
                        ivTrend.setImageResource(R.drawable.ic_trend_up_dash)
                        tvPercent.setTextColor(Color.parseColor("#0EF377"))
                    } else {
                        ivTrend.setImageResource(R.drawable.ic_trend_down_dash)
                        tvPercent.setTextColor(Color.parseColor("#FF426F"))
                    }
                    tvPercent.text = "${abs(impact)}%"
                    root.visible()
                }
            } else {
                binding.lytTrend.root.gone()
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

            val impact = data.data.impact
            if (impact != null) {
                binding.lytTrend.apply {
                    if (impact >= 0) {
                        ivTrend.setImageResource(R.drawable.ic_trend_up_dash)
                        tvPercent.setTextColor(Color.parseColor("#0EF377"))
                    } else {
                        ivTrend.setImageResource(R.drawable.ic_trend_down_dash)
                        tvPercent.setTextColor(Color.parseColor("#FF426F"))
                    }
                    tvPercent.text = "${abs(impact)}%"
                    root.visible()
                }
            } else {
                binding.lytTrend.root.gone()
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick)
            }

        }
    }

    class ActivityViewHolder2(private val binding: ListActivityBurnCardItem2Binding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Activity, position: Int, lastPosition: Int, devicePaired: Boolean
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
                DecoEvent.Builder(caloriesPercent).setIndex(distanceIndex).setDuration(1000L)
                    .build()
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


            val impact = data.data.impact

            if (impact != null) {
                binding.lytTrend.apply {
                    if (impact >= 0) {
                        ivTrend.setImageResource(R.drawable.ic_trend_up_dash)
                        tvPercent.setTextColor(Color.parseColor("#0EF377"))
                    } else {
                        ivTrend.setImageResource(R.drawable.ic_trend_down_dash)
                        tvPercent.setTextColor(Color.parseColor("#FF426F"))
                    }
                    tvPercent.text = "${abs(impact)}%"
                    root.visible()
                }
            } else {
                binding.lytTrend.root.gone()
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick)
            }
        }
    }

    class ActivityViewHolder(private val binding: ListActivityBurnCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.Activity, position: Int, lastPosition: Int, devicePaired: Boolean
        ) {
            //binding.imv.loadImage(binding.imv.context, R.drawable.ic_activity_card_bg1)
            val scoreValue = data.data.activityScore

            if (scoreValue == null) {
                binding.tvValue.text = "--"
                binding.tvStatus.text = binding.tvStatus.context.getString(R.string.text_no_data)
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
            caloriesProgress =
                (if (caloriesProgress > caloriesGoal) 100f else caloriesProgress.calculatePercentage(
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
            var title =
                binding.root.context.getString(R.string.text_value_workouts_detected, data.count)

            if (data.count <= 1) {
                title =
                    binding.root.context.getString(R.string.text_value_workout_detected, data.count)
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
            val view = RowDashAlertBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
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


    class CycleTrackerCardSmallViewHolder(private val binding: ListCycleTrackerPredictionBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.CycleTrackerCardSmall,
            position: Int,
        ) {
            binding.textView3.text = data.data.title
            binding.tvOvlInDays.text = data.data.days.toString()
            binding.textView1.text = data.data.bottomText
            binding.tvPredictionDays.text = data.data.predictionDate
            binding.tvOvlDaysCurrent.text = "Day ${data.data.currentCycleDay}"
            binding.tvOvlDaysLeft.text = "of ${data.data.totalCycleDay}"
            binding.imv.setBackgroundResource(data.data.background)

            binding.tvDesc.text = data.data.nudge

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.FemaleHealthHome)
            }
        }
    }

    class CycleTrackerCardBigViewHolder(private val binding: ListCycleTrackerOngoingBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.CycleTrackerCardBig,
            position: Int,
        ) {
            binding.textView3.text = data.data.title
            binding.tvOvlInDays.text = data.data.subTitle
            binding.tvCurrentDay.text = "Day ${data.data.days}"
            binding.tvDaysLeft.text = "of ${data.data.totalCycleDay}"
            binding.tvDesc.text = data.data.nudge
            binding.tvValue.text = if (data.data.temperatureVariation == null) {
                "-"
            } else {
                if (data.data.temperatureVariation > 0) {
                    "+${data.data.temperatureVariation}"
                } else {
                    "${data.data.temperatureVariation}"
                }
            }
            binding.imv.setBackgroundResource(data.data.background)

            binding.tvPeriodicPeriod.text = data.data.predictionString
            binding.tvDays.text = data.data.predictionDate

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.FemaleHealthHome)
            }
        }
    }

    class TrackYourCycleViewHolder(private val binding: CardTrackFmHealthBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.CardTrackFemaleHealth,
            position: Int,
        ) {
            when (data.state) {
                FemaleHealthCardState.TRACK -> {
                    binding.btnGetStarted.text =
                        binding.btnGetStarted.context.getString(R.string.text_get_started)
                    binding.textView92.text =
                        binding.textView92.context.getString(R.string.text_track_your_cycle_desc)

                }

                FemaleHealthCardState.LOG -> {
                    binding.btnGetStarted.text =
                        binding.btnGetStarted.context.getString(R.string.text_log_period)
                    binding.textView92.text =
                        binding.textView92.context.getString(R.string.text_log_text)
                }
            }



            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.TrackYourFemaleHealth)
            }
            binding.btnGetStarted.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.TrackYourFemaleHealth)
            }
            binding.tvRemindMeLater.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.TrackYourFemaleHealthRemindLater)
            }
            binding.ivCross.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.TrackYourFemaleHealthRemindLater)
            }
        }
    }

    class GotYourPeriodViewHolder(private val binding: ListDashGotPeriodBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.GotYourPeriod,
            position: Int,
        ) {
            binding.tvPredictedDay.text = data.title ?: ""

            binding.bYes.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.GotPeriodClicked(true))
            }
            binding.bNo.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.GotPeriodClicked(false))
            }
        }
    }

    fun getStatusColors(status: String): Int {
        val color: Int = if (status.equals("warning", true)) {
            R.color.sleep_warning
        } else if (status.equals("good", true)) {
            R.color.sleep_good
        } else if (status.equals("optimal", true)) {
            R.color.sleep_optimal
        } else if (status.equals("fair", true)) {
            R.color.color_fair
        } else {
            R.color.white_12_72
        }
        return color
    }

}

interface AlertClickListener {
    fun onAlertClicked(alertType: AlertType)
}



