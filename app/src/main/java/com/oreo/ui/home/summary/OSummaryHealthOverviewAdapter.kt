package com.oreo.ui.home.summary

import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.ImageView
import android.transition.AutoTransition
import android.transition.Transition
import android.transition.TransitionManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CardTrackFmHealthBinding
import com.noisefit.luna.databinding.ItemSevenDayTrendsCardBinding
import com.noisefit.luna.databinding.ItemStressGraphBinding
import com.noisefit.luna.databinding.LayoutCardCaffeineDashBinding
import com.noisefit.luna.databinding.LayoutChatCardDashBinding
import com.noisefit.luna.databinding.LayoutDashHealthMonitorBinding
import com.noisefit.luna.databinding.LayoutDashSleepPlannerCardBinding
import com.noisefit.luna.databinding.LayoutNotificationCardBinding
import com.noisefit.luna.databinding.LayoutStressDashMeasureBinding
import com.noisefit.luna.databinding.ListActivityBurnCardItem2Binding
import com.noisefit.luna.databinding.ListActivityBurnCardItemBinding
import com.noisefit.luna.databinding.ListActivityMinimalItemBinding
import com.noisefit.luna.databinding.ListCycleTrackerOngoingBinding
import com.noisefit.luna.databinding.ListCycleTrackerPredictionBinding
import com.noisefit.luna.databinding.ListDashGotPeriodBinding
import com.noisefit.luna.databinding.ListDashNapBinding
import com.noisefit.luna.databinding.ListHeartRateCardItemBinding
import com.noisefit.luna.databinding.ListOWAlertCardItemBinding
import com.noisefit.luna.databinding.ListReadinessCardItemBinding
import com.noisefit.luna.databinding.ListReadinessMinimalCardItemBinding
import com.noisefit.luna.databinding.ListRingCareBinding
import com.noisefit.luna.databinding.ListSleepCardItemBinding
import com.noisefit.luna.databinding.ListSleepMinimalItemBinding
import com.noisefit.luna.databinding.ListSleepWaitingCardItemBinding
import com.noisefit.luna.databinding.ListVideoInfoCardBinding
import com.noisefit.luna.databinding.ListWelcomeCardBinding
import com.noisefit.luna.databinding.OreoLayoutRecentActivityBinding
import com.noisefit.luna.databinding.RowDashAlertBinding
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.dpToPx
import com.oreo.data.model.NotificationGoals
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
import com.oreo.data.model.CaffeineWindowData
import com.oreo.data.model.ChartModel
import com.oreo.data.model.DashAlert
import com.oreo.data.model.FemaleHealthCardState
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.VideoInfoType
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.ui.custom.HighlightState
import com.oreo.ui.home.summary.paginate.NotificationGoal
import com.oreo.util.DateTimeUtil
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import androidx.core.graphics.toColorInt
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.databinding.ItemTimelineDashBinding
import com.noisefit.luna.databinding.LayoutCaffeineCalibratingBinding
import com.noisefit.luna.databinding.LayoutCircadianOnboardingDashBinding
import com.noisefit.luna.databinding.LayoutDashCircadianBinding
import com.noisefit.luna.databinding.LayoutDashNoSleepStatesCircadianBinding
import com.noisefit.luna.databinding.LayoutTimelineCardDashBinding
import com.noisefit.luna.databinding.LayoutOneTapVitalsCardBinding
import com.noisefit_commans.data.model.circadian.CircadianGraphData
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.playAnimation
import com.oreo.data.model.OHealthOverview.VitalsType
import com.oreo.ui.chatGpt.SummaryStates
import com.oreo.ui.circadianAlignment.CircadianAlignmentViewModel
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.SYMPTOM_KEY
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.math.exp
import kotlin.math.pow


sealed class OSummaryHealthOverviewClickEnum {
    object SleepDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object ActivityDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object ReadinessDetailsWorkoutClick : OSummaryHealthOverviewClickEnum()
    object TextWelcomeRingClicked : OSummaryHealthOverviewClickEnum()
    data class OnNapClicked(val napId: String) : OSummaryHealthOverviewClickEnum()
    object StressCardClicked : OSummaryHealthOverviewClickEnum()
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

    //
    data class OnHeartMeasureImvClicked(val data: OHealthOverview.HeartRateDataModel) :
        OSummaryHealthOverviewClickEnum()

    data class OnStressMeasureImvClicked(val data: OHealthOverview.StressCard) :
        OSummaryHealthOverviewClickEnum()

    object OnHeartRateCardClicked : OSummaryHealthOverviewClickEnum()

    object OnViewAddWorkout : OSummaryHealthOverviewClickEnum()
    object OnWorkoutsHistoryCardClicked : OSummaryHealthOverviewClickEnum()
    data class OnWorkoutsHistoryCardOworkoutAdapterItemClicked(
        val data: OActivityListModal,
        val position: Int
    ) : OSummaryHealthOverviewClickEnum()

    data class OnIvNotificationStepsClicked(val position: Int) : OSummaryHealthOverviewClickEnum()
    data class OnIvNotificationHydrateClicked(val position: Int) : OSummaryHealthOverviewClickEnum()
    object OnIvHydrateMinusClicked : OSummaryHealthOverviewClickEnum()
    object OnIvHydratePlusClicked : OSummaryHealthOverviewClickEnum()
    object OnEditGoalsCardEditClicked : OSummaryHealthOverviewClickEnum()

    data class OnCaffeineDashCardClicked(val data: CaffeineWindowData) :
        OSummaryHealthOverviewClickEnum()

    object OnDailyDigestMainCardClicked : OSummaryHealthOverviewClickEnum()

    object OnCircadianAlignmentCardClicked : OSummaryHealthOverviewClickEnum()
    object OnGetStartedCircadianOnboardingClicked : OSummaryHealthOverviewClickEnum()

    object OnTimelineCardClicked : OSummaryHealthOverviewClickEnum()
    class OnLogActivityClicked(val key: String?) : OSummaryHealthOverviewClickEnum()
    //

    // One Tap Vitals
    data class OnOneTapVitalsItemClicked(val type: OHealthOverview.VitalsType) :
        OSummaryHealthOverviewClickEnum()

    object OnOneTapVitalsCollapsed : OSummaryHealthOverviewClickEnum()
    data class UpdateOneTapVitalsCardState(val measureState: TapMeasureState?) :
        OSummaryHealthOverviewClickEnum()

}

class OSummaryHealthOverviewAdapter() : RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    var devicePaired = false
    var lastPosition = -1
    var refreshPosition: Int? = null

    private val items = ArrayList<OHealthOverview>()

    fun updateDataSet(dataSet: List<OHealthOverview>) {
        items.clear()
        items.addAll(dataSet)
        notifyDataSetChanged()

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

            R.layout.layout_card_caffeine_dash -> HomeRecyclerViewHolder.CaffeineViewHolder(
                LayoutCardCaffeineDashBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.layout_caffeine_calibrating -> HomeRecyclerViewHolder.CaffeineRestrictedViewHolder(
                LayoutCaffeineCalibratingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.layout_dash_circadian -> HomeRecyclerViewHolder.CircadianAlignmentViewHolder(
                LayoutDashCircadianBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.layout_dash_no_sleep_states_circadian -> HomeRecyclerViewHolder.CircadianLockedNoSleepCardViewHolder(
                LayoutDashNoSleepStatesCircadianBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.layout_circadian_onboarding_dash -> HomeRecyclerViewHolder.CircadianOnboardingViewHolder(
                LayoutCircadianOnboardingDashBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.layout_timeline_card_dash -> HomeRecyclerViewHolder.TimelineCardViewHolder(
                LayoutTimelineCardDashBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.layout_one_tap_vitals_card -> HomeRecyclerViewHolder.OneTapVitalsViewHolder(
                LayoutOneTapVitalsCardBinding.inflate(
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

            //
            R.layout.list_heart_rate_card_item -> {
                HomeRecyclerViewHolder.HeartRateViewHolder(
                    ListHeartRateCardItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            R.layout.layout_notification_card -> {
                HomeRecyclerViewHolder.DailyGoalsViewHolder(
                    LayoutNotificationCardBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            R.layout.oreo_layout_recent_activity -> {
                HomeRecyclerViewHolder.WorkoutsHistoryViewHolder(
                    OreoLayoutRecentActivityBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            R.layout.item_seven_day_trends_card -> {
                HomeRecyclerViewHolder.SevenDayTrendsCardViewHolder(
                    ItemSevenDayTrendsCardBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            R.layout.layout_stress_dash_measure -> {
                HomeRecyclerViewHolder.StressCardViewHolder(
                    LayoutStressDashMeasureBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            R.layout.item_stress_graph -> HomeRecyclerViewHolder.StressGraphViewHolder(
                ItemStressGraphBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            //

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

            //
            is HomeRecyclerViewHolder.HeartRateViewHolder -> holder.bind(items[position] as OHealthOverview.HeartRateDataModel)

            is HomeRecyclerViewHolder.DailyGoalsViewHolder -> holder.bind(
                items[position] as OHealthOverview.DailyGoalsCardData,
                position
            )

            is HomeRecyclerViewHolder.WorkoutsHistoryViewHolder -> holder.bind(
                items[position] as OHealthOverview.WorkoutHistoryCardData
            )

            is HomeRecyclerViewHolder.SevenDayTrendsCardViewHolder -> holder.bind(
                items[position] as OHealthOverview.SevenDayTrendsCard
            )

            is HomeRecyclerViewHolder.StressCardViewHolder -> holder.bind(
                items[position] as OHealthOverview.StressCard
            )
            //

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

            is HomeRecyclerViewHolder.CaffeineViewHolder -> holder.bind(
                items[position] as OHealthOverview.CaffeineWindow,
            )

            is HomeRecyclerViewHolder.CaffeineRestrictedViewHolder -> holder.bind(
                items[position] as OHealthOverview.CaffeineWindowCalibrating,
            )

            is HomeRecyclerViewHolder.CircadianAlignmentViewHolder -> holder.bind(
                items[position] as OHealthOverview.CircadianAlignment,
            )

            is HomeRecyclerViewHolder.CircadianLockedNoSleepCardViewHolder -> holder.bind(
                items[position] as OHealthOverview.CircadianLockedOrNoSleepCard,
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

            is HomeRecyclerViewHolder.CircadianOnboardingViewHolder -> {
                holder.bind(items[position] as OHealthOverview.CircadianAlignmentOnboarding)
            }

            is HomeRecyclerViewHolder.TimelineCardViewHolder -> {
                holder.bind(items[position] as OHealthOverview.TimelineDash)
            }

            is HomeRecyclerViewHolder.OneTapVitalsViewHolder -> {
                holder.bind(items[position] as OHealthOverview.OneTapVitals)
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
            is OHealthOverview.HeartRateDataModel -> R.layout.list_heart_rate_card_item
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
            is OHealthOverview.CaffeineWindow -> R.layout.layout_card_caffeine_dash
            is OHealthOverview.HealthMonitorCard -> R.layout.layout_dash_health_monitor
            is OHealthOverview.SleepPlannerCard -> R.layout.layout_dash_sleep_planner_card

            //
            is OHealthOverview.WorkoutHistoryCardData -> R.layout.oreo_layout_recent_activity
            is OHealthOverview.DailyGoalsCardData -> R.layout.layout_notification_card
            is OHealthOverview.SevenDayTrendsCard -> R.layout.item_seven_day_trends_card
            is OHealthOverview.StressCard -> R.layout.layout_stress_dash_measure
            //
            is OHealthOverview.CaffeineWindowCalibrating -> R.layout.layout_caffeine_calibrating

            is OHealthOverview.CircadianAlignment -> R.layout.layout_dash_circadian
            OHealthOverview.CircadianAlignmentOnboarding -> R.layout.layout_circadian_onboarding_dash
            is OHealthOverview.CircadianLockedOrNoSleepCard -> R.layout.layout_dash_no_sleep_states_circadian

            is OHealthOverview.TimelineDash -> R.layout.layout_timeline_card_dash
            is OHealthOverview.OneTapVitals -> R.layout.layout_one_tap_vitals_card
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

    fun updateData(heathOverViewData: OHealthOverview?) {
        if (heathOverViewData is OHealthOverview.DailyGoalsCardData) {
            val index = items.indexOfFirst { it is OHealthOverview.DailyGoalsCardData }
            if (index == -1) return
            items[index] = heathOverViewData
            notifyItemChanged(index)
        } else if (heathOverViewData is OHealthOverview.HeartRateDataModel) {
            val index = items.indexOfFirst { it is OHealthOverview.HeartRateDataModel }
            if (index == -1) return
            items[index] = heathOverViewData
            notifyItemChanged(index)
        } else if (heathOverViewData is OHealthOverview.StressCard) {
            val index = items.indexOfFirst { it is OHealthOverview.StressCard }
            if (index == -1) return
            items[index] = heathOverViewData
            notifyItemChanged(index)
        } else if (heathOverViewData is OHealthOverview.LunaAiCard) {
            val index = items.indexOfFirst { it is OHealthOverview.LunaAiCard }
            if (index == -1) return
            items[index] = heathOverViewData
            notifyItemChanged(index)
        } else if (heathOverViewData is OHealthOverview.CircadianAlignment) {
            val index = items.indexOfFirst { it is OHealthOverview.CircadianAlignment }
            if (index == -1) return
            items[index] = heathOverViewData
            notifyItemChanged(index)
        } else if (heathOverViewData is OHealthOverview.OneTapVitals) {
            val index = items.indexOfFirst { it is OHealthOverview.OneTapVitals }
            if (index == -1) return
            items[index] = heathOverViewData
            notifyItemChanged(index)
        } else if (heathOverViewData is OHealthOverview.TimelineDash) {
            val index = items.indexOfFirst { it is OHealthOverview.TimelineDash }
            if (index == -1) return
            items[index] = heathOverViewData
            notifyItemChanged(index)
        }
    }

    fun updateDailyToggle(goal: NotificationGoal) {
        val index = items.indexOfFirst { it is OHealthOverview.DailyGoalsCardData }
        if (index == -1) return
        val oldData = (items[index] as OHealthOverview.DailyGoalsCardData).notificationGoals.copy()
        (items[index] as OHealthOverview.DailyGoalsCardData).notificationGoals = oldData.apply {
            if (goal == NotificationGoal.STEPS) {
                this.notificationToggleModel?.steps_notification =
                    (this.notificationToggleModel?.steps_notification ?: false).not()
                if (this.notificationToggleModel?.steps_notification ?: false) {
                    this.notificationToggleModel?.master_notification = true
                }
                this.showStepsFade = true
            } else if (goal == NotificationGoal.HYDRATE) {
                this.notificationToggleModel?.hydrate_notification =
                    (this.notificationToggleModel?.hydrate_notification ?: false).not()
                if (this.notificationToggleModel?.hydrate_notification ?: false) {
                    this.notificationToggleModel?.master_notification = true
                }
                this.showHydrateFade = true
            }

        }
        notifyItemChanged(index)
    }

}


sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((type: OSummaryHealthOverviewClickEnum) -> Unit)? = null

    //
    class TimelineCardViewHolder(private val binding: LayoutTimelineCardDashBinding) :
        HomeRecyclerViewHolder(binding) {

        class TimelineAdapter(
            private val listData: List<ItemTimelineResponseModel>
        ) : RecyclerView.Adapter<TimelineAdapter.ItemTimelineViewHolder>() {

            inner class ItemTimelineViewHolder(private val binding: ItemTimelineDashBinding) :
                RecyclerView.ViewHolder(binding.root) {
                fun bind(data: ItemTimelineResponseModel, position: Int) {
                    binding.tvTitle.text = data.title
                    data.titleColor?.let { binding.tvTitle.setTextColor(it) }

                    binding.tvDesc.text = data.desc

                    if (data.event.equals(SYMPTOM_KEY, true)) {
                        binding.tvTime.invisible()
                    } else {
                        binding.tvTime.apply {
                            text = data.displayTime
                            visible()
                        }
                    }

                    binding.divider.root.setVisibilityByCondition(position != listData.size - 1)
                }
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ItemTimelineViewHolder {
                val binding = ItemTimelineDashBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return ItemTimelineViewHolder(binding)
            }

            override fun getItemCount(): Int {
                return listData.size
            }

            override fun onBindViewHolder(holder: ItemTimelineViewHolder, position: Int) {
                holder.bind(listData[position], position)
            }
        }

        fun bind(data: OHealthOverview.TimelineDash) {

            if (data.listData.isNullOrEmpty()) {
                binding.rvActivities.gone()
                binding.lytNoData.apply {
                    imageView102.setBackgroundResource(R.drawable.ic_noactivity_timeline)
                    root.visible()
                }
            } else {
                val adapter = TimelineAdapter(data.listData)
                binding.rvActivities.apply {
                    this.layoutManager = LinearLayoutManager(binding.root.context)
                    this.adapter = adapter
                    binding.lytNoData.root.gone()
                    visible()
                }
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnTimelineCardClicked)
            }
            binding.btnLogAnActivity.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnLogActivityClicked(null))
            }
        }

    }

    class CircadianOnboardingViewHolder(private val binding: LayoutCircadianOnboardingDashBinding) :
        HomeRecyclerViewHolder(binding) {

        fun bind(data: OHealthOverview.CircadianAlignmentOnboarding) {

            binding.btnGetStarted.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnGetStartedCircadianOnboardingClicked)
            }
        }

    }

    class OneTapVitalsViewHolder(private val binding: LayoutOneTapVitalsCardBinding) :
        HomeRecyclerViewHolder(binding) {

        private var currentAnimator: android.animation.ValueAnimator? = null
        private var expandedTile: ViewGroup? = null

        fun bind(data: OHealthOverview.OneTapVitals) {

            binding.itemHR.setVisibilityByCondition(data.featureConfig.showHR && expandedTile == null)
            binding.imageBackHr.setVisibilityByCondition(data.featureConfig.showHR && expandedTile == null)
            binding.itemStress.setVisibilityByCondition(data.featureConfig.showStress && expandedTile == null)
            binding.imageBackStress.setVisibilityByCondition(data.featureConfig.showStress && expandedTile == null)
            binding.itemSpO2.setVisibilityByCondition(data.featureConfig.showSpO2 && expandedTile == null)
            binding.imageBackSpo2.setVisibilityByCondition(data.featureConfig.showSpO2 && expandedTile == null)
            binding.itemSkinTemp.setVisibilityByCondition(data.featureConfig.showSkinTemp && expandedTile == null)
            binding.imageBackSkinTemp.setVisibilityByCondition(data.featureConfig.showSkinTemp && expandedTile == null)
            binding.anchorImageView.visibility = View.INVISIBLE

            binding.lytHrValue.apply {
                tvValue.text = data.hrValue?.let { it } ?: "--"
                tvUnit.text = data.hrValue?.let {"BPM"}?:""
                tvUnit.setTextColor("#FF4E5C".toColorInt())
            }
            binding.tvHrAgo.text = data.hrLastTime ?: "-"

            binding.lytStressValue.apply {
                tvValue.text = data.stressValue?.let { it } ?: "--"
                tvUnit.gone()
            }
            binding.tvStressAgo.text = data.stressLastTime ?: "-"

            binding.lytSpO2Value.apply {
                tvValue.text = data.spo2Value?.let { it } ?: "--"
                tvUnit.text = data.spo2Value?.let { "%" } ?: ""
                tvUnit.setTextColor("#7BBCFE".toColorInt())
            }
            binding.tvSpO2Ago.text = data.spo2LastTime ?: "-"

            binding.lytSkinValue.apply {
                tvValue.text =
                    data.skinTempValue?.let { it } ?: "--"
                tvUnit.text = data.skinTempValue?.let { if(data.isMetric) "°C" else "°F" } ?: ""
                tvUnit.setTextColor("#6AAF93".toColorInt())
            }
            binding.tvSkinAgo.text = data.skinTempLastTime ?: "-"

            if (data.expandedType != null) {
                expandTile(data)
            } else {
                collapseTiles(data)
            }

            binding.itemHR.setOnClickListener {
                if(expandedTile!=null) return@setOnClickListener
                onItemClicked(
                    OHealthOverview.VitalsType.HR,
                    data,
                    binding.itemHR
                )
            }
            binding.itemStress.setOnClickListener {
                if(expandedTile!=null) return@setOnClickListener
                onItemClicked(
                    OHealthOverview.VitalsType.STRESS,
                    data,
                    binding.itemStress
                )
            }
            binding.itemSpO2.setOnClickListener {
                if(expandedTile!=null) return@setOnClickListener
                onItemClicked(
                    OHealthOverview.VitalsType.SPO2,
                    data,
                    binding.itemSpO2
                )
            }
            binding.itemSkinTemp.setOnClickListener {
                if(expandedTile!=null) return@setOnClickListener
                onItemClicked(
                    OHealthOverview.VitalsType.SKIN_TEMP,
                    data,
                    binding.itemSkinTemp
                )
            }
        }

        private fun onItemClicked(
            type: OHealthOverview.VitalsType,
            data: OHealthOverview.OneTapVitals,
            tile: ViewGroup
        ) {
            itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnOneTapVitalsItemClicked(type))
            //data.expandedType = type
            //data.measuring = true
            //expandTile(data, tile)
        }

        private fun collapseTiles(data: OHealthOverview.OneTapVitals) {
            stopHeartAnimation()
            expandedTile = null

            binding.itemHR.setVisibilityByCondition(data.featureConfig.showHR)
            binding.imageBackHr.setVisibilityByCondition(data.featureConfig.showHR)
            binding.itemStress.setVisibilityByCondition(data.featureConfig.showStress)
            binding.imageBackStress.setVisibilityByCondition(data.featureConfig.showStress)
            binding.itemSpO2.setVisibilityByCondition(data.featureConfig.showSpO2)
            binding.imageBackSpo2.setVisibilityByCondition(data.featureConfig.showSpO2)
            binding.itemSkinTemp.setVisibilityByCondition(data.featureConfig.showSkinTemp)
            binding.imageBackSkinTemp.setVisibilityByCondition(data.featureConfig.showSkinTemp)

            listOf(
                binding.itemHR,
                binding.itemStress,
                binding.itemSpO2,
                binding.itemSkinTemp
            ).forEach { t ->
                for (i in 0 until t.childCount) {
                    val child = t.getChildAt(i)
                    if (child.id == R.id.tileMeasuringRoot) {
                        child.visibility = View.GONE
                    } else {
                        child.visibility = View.VISIBLE
                    }
                }
            }

            binding.lytCollapsed.visibility = View.VISIBLE
            binding.lytCollapsed.alpha = 1f
        }

        private fun expandTile(data: OHealthOverview.OneTapVitals) {
            val tile = when (data.expandedType) {
                OHealthOverview.VitalsType.HR -> binding.itemHR
                OHealthOverview.VitalsType.STRESS -> binding.itemStress
                OHealthOverview.VitalsType.SPO2 -> binding.itemSpO2
                OHealthOverview.VitalsType.SKIN_TEMP -> binding.itemSkinTemp
                else -> binding.itemHR
            }
            expandedTile = tile

            val context = binding.root.context
            val parent = binding.lytCollapsed


            for (i in 0 until tile.childCount) {
                val child = tile.getChildAt(i)
                if (child.id != R.id.tileMeasuringRoot) child.visibility = View.GONE
            }

            binding.imageBackHr.gone()
            binding.imageBackStress.gone()
            binding.imageBackSpo2.gone()
            binding.imageBackSkinTemp.gone()

            var transition = AutoTransition().apply { duration = 200 }

            val measuringRoot = tile.findViewById<View>(R.id.tileMeasuringRoot)
            if (data.measureState != TapMeasureState.LAST_MEASURED) {
                transition = AutoTransition().apply { duration = 200 }
                measuringRoot.visibility = View.GONE
                measuringRoot.alpha = 0f
                measuringRoot.visibility = View.VISIBLE
                measuringRoot.animate().alpha(1f).setDuration(600).start()
            } else {
                transition = AutoTransition().apply { duration = 0 }
                measuringRoot.visibility = View.VISIBLE
                measuringRoot.alpha = 1f
            }

            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {
                    transition.removeListener(this)
                }

                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionResume(transition: Transition) {}
            })
            TransitionManager.beginDelayedTransition(parent, transition)
            listOf(
                binding.itemHR,
                binding.itemStress,
                binding.itemSpO2,
                binding.itemSkinTemp
            ).forEach {
                it.visibility = if (it == tile) View.VISIBLE else View.GONE
            }

            expandedTile = tile

            val titleTextViewExp = measuringRoot.findViewById<TextView>(R.id.tvMeasuringTitle)
            val hintTextViewExp = measuringRoot.findViewById<TextView>(R.id.tvMeasuringHint)

            val progressBar = measuringRoot.findViewById<ProgressBar>(R.id.pbMeasuring)

            val lytSuccess = measuringRoot.findViewById<LinearLayout>(R.id.lytMeasureSuccess)
            val tvSuccessVal = measuringRoot.findViewById<TextView>(R.id.tvMeasurementVal)

            val retryBtn = measuringRoot.findViewById<ImageView>(R.id.ivRetry)
            val btnCancel = measuringRoot.findViewById<ImageView>(R.id.ivCancel)
            val lottieView = measuringRoot.findViewById<LottieAnimationView>(R.id.lottieView)

            when (data.measureState) {
                TapMeasureState.LAST_MEASURED -> {
                    lottieView.gone()
                    lottieView.cancelAnimation()
                    progressBar.gone()
                    retryBtn.gone()
                    btnCancel.gone()
                    titleTextViewExp.text = getMeasuringTextByType(data.expandedType, context)
                    hintTextViewExp.text =
                        context.getString(R.string.text_measuring_may_take_30_sec)
                    when (data.expandedType) {
                        OHealthOverview.VitalsType.HR -> {
                            tvSuccessVal.text = data.hrValue
                        }

                        OHealthOverview.VitalsType.STRESS -> {
                            tvSuccessVal.text = data.stressValue
                        }

                        OHealthOverview.VitalsType.SPO2 -> {
                            tvSuccessVal.text = data.spo2Value
                        }

                        OHealthOverview.VitalsType.SKIN_TEMP -> {
                            tvSuccessVal.text = data.skinTempValue
                        }

                        null -> {}
                    }
                    lytSuccess.visible()
                    itemClickListener?.invoke(
                        OSummaryHealthOverviewClickEnum.UpdateOneTapVitalsCardState(
                            null
                        )
                    )
                    /*collapseTiles(data.apply {
                        this.expandedType = null
                        this.measuring = false
                        this.measureState = null
                    })*/
                }

                TapMeasureState.ERROR -> {
                    progressBar.gone()
                    lottieView.gone()
                    lottieView.cancelAnimation()
                    lytSuccess.gone()
                    retryBtn.apply {
                        setOnClickListener {
                            data.expandedType?.let { type ->
                                data.measureState = null
                                data.measuring = false
                                data.isRetry = true
                                lytSuccess.gone()
                                retryBtn.gone()
                                progressBar.visible()
                                itemClickListener?.invoke(
                                    OSummaryHealthOverviewClickEnum.OnOneTapVitalsItemClicked(type)
                                )
                            }
                        }
                        visible()
                    }

                    btnCancel.apply {
                        setOnClickListener {
                            binding.anchorImageView.invisible()
                            data.expandedType?.let { type ->
                                data.measureState = null
                                data.measuring = false
                                data.isRetry = false
                                collapseTiles(data)
                            }
                        }
                        visible()
                    }
                    titleTextViewExp.text =
                        context.getString(R.string.text_something_went_wrong_single)
                    hintTextViewExp.text = context.getString(R.string.text_unable_to_track)
                }

                else -> {

                    lottieView.visible()
                    lottieView.playAnimation(LottieDrawable.INFINITE,R.raw.anim_measure_hr)

                    lytSuccess.gone()
                    retryBtn.gone()
                    btnCancel.gone()
                    progressBar.visible()
                    titleTextViewExp.text = getMeasuringTextByType(data.expandedType, context)
                    hintTextViewExp.text =
                        context.getString(R.string.text_measuring_may_take_30_sec)
                }
            }

            if (data.measureState != TapMeasureState.LAST_MEASURED) {
                animateTileExpand(tile)
            }

            val ivAnchor =
                binding.anchorImageView//measuringRoot.findViewById<ImageView>(R.id.ivHeartAnim)

            if (data.measureState == TapMeasureState.MEASURING) {
                val srcView = when (data.expandedType) {
                    VitalsType.HR -> binding.ivHrIcon
                    VitalsType.STRESS -> binding.ivStressIcon
                    VitalsType.SPO2 -> binding.ivSpO2Icon
                    VitalsType.SKIN_TEMP -> binding.ivSkinIcon
                    null -> binding.ivHrIcon
                }
                ivAnchor.setImageResource(getIcon(data.expandedType))
                if(data.isRetry.not()){
                    animateImageView(ivAnchor, srcView,{
                        if (data.expandedType == OHealthOverview.VitalsType.HR) {
                            val hrVal = data.hrValue?.toIntOrNull()?:60
                            startHeartAnimation(ivAnchor, hrVal)
                        }else {
                            stopHeartAnimation(ivAnchor)
                        }
                    })
                }else{ ivAnchor.visible()
                    if (data.expandedType == OHealthOverview.VitalsType.HR) {
                        val hrVal = data.hrValue?.toIntOrNull()?:60
                        startHeartAnimation(ivAnchor, hrVal)
                    }else {
                        stopHeartAnimation(ivAnchor)
                    }
                }
            }else if(data.measureState== TapMeasureState.ERROR){
                ivAnchor.visible()
                stopHeartAnimation(ivAnchor)
            } else {
                ivAnchor.invisible()
                stopHeartAnimation(ivAnchor)
            }

            tile.setOnClickListener {

                return@setOnClickListener

                val t = expandedTile ?: tile
                val parent = binding.lytCollapsed
                val measuring = t.findViewById<View>(R.id.tileMeasuringRoot)

                // Begin width shrink via ChangeBounds while fading out measuring
                val transition = AutoTransition().apply { duration = 220 }
                TransitionManager.beginDelayedTransition(parent, transition)
                binding.itemHR.visibility =
                    if (data.featureConfig.showHR) View.VISIBLE else View.GONE
                binding.itemStress.visibility =
                    if (data.featureConfig.showStress) View.VISIBLE else View.GONE
                binding.itemSpO2.visibility =
                    if (data.featureConfig.showSpO2) View.VISIBLE else View.GONE
                binding.itemSkinTemp.visibility =
                    if (data.featureConfig.showSkinTemp) View.VISIBLE else View.GONE

                measuring.visibility = View.GONE
                measuring?.animate()?.alpha(0f)?.setDuration(220)?.withEndAction {
                    for (i in 0 until t.childCount) {
                        val child = t.getChildAt(i)
                        if (child.id != R.id.tileMeasuringRoot) child.visibility = View.VISIBLE
                    }
                }?.start()

                data.expandedType = null
                data.measuring = false
                expandedTile = null

                // restore expansion listeners immediately
                binding.itemHR.setOnClickListener {
                    if (data.measureState == TapMeasureState.ERROR) onItemClicked(
                        OHealthOverview.VitalsType.HR,
                        data,
                        binding.itemHR
                    )
                }
                binding.itemStress.setOnClickListener {
                    if (data.measureState == TapMeasureState.ERROR) onItemClicked(
                        OHealthOverview.VitalsType.STRESS,
                        data,
                        binding.itemStress
                    )
                }
                binding.itemSpO2.setOnClickListener {
                    if (data.measureState == TapMeasureState.ERROR) onItemClicked(
                        OHealthOverview.VitalsType.SPO2,
                        data,
                        binding.itemSpO2
                    )
                }
                binding.itemSkinTemp.setOnClickListener {
                    if (data.measureState == TapMeasureState.ERROR) onItemClicked(
                        OHealthOverview.VitalsType.SKIN_TEMP,
                        data,
                        binding.itemSkinTemp
                    )
                }
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnOneTapVitalsCollapsed)
            }
        }

        private fun animateImageView(
            ivAnchor: ImageView,
            ivHrIcon: ImageView,
            onFinish:()-> Unit,
        ) {
            ivAnchor.invisible()
            ivAnchor.post {
                val startLocation = IntArray(2)
                val endLocation = IntArray(2)

                ivHrIcon.getLocationOnScreen(startLocation)
                ivAnchor.getLocationOnScreen(endLocation)

                val startX = startLocation[0].toFloat()
                val startY = startLocation[1].toFloat()
                val endX = endLocation[0].toFloat()
                val endY = endLocation[1].toFloat()

                // Scale differences
                val scaleX = ivHrIcon.width.toFloat() / ivAnchor.width.toFloat()
                val scaleY = ivHrIcon.height.toFloat() / ivAnchor.height.toFloat()

                // Reset ivAnchor to start position & scale
                ivAnchor.scaleX = scaleX
                ivAnchor.scaleY = scaleY
                ivAnchor.translationX = startX - endX
                ivAnchor.translationY = startY - endY

                ivAnchor.visible()

                ivAnchor.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(0f)
                    .translationY(0f)
                    .setDuration(500)
                    .setListener(object : Animator.AnimatorListener{
                        override fun onAnimationStart(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {
                            onFinish()
                        }

                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationRepeat(animation: Animator) {

                        }

                    })
                    .start()

            }

        }

        private fun getIcon(type: VitalsType?): Int {
            return when (type) {
                VitalsType.HR -> R.drawable.ic_hr_measure
                VitalsType.STRESS -> R.drawable.ic_stress_measure
                VitalsType.SPO2 -> R.drawable.ic_spo2_measure
                VitalsType.SKIN_TEMP -> R.drawable.ic_temp_measure
                null -> R.drawable.ic_hr_measure
            }
        }

        private fun getMeasuringTextByType(type: VitalsType?, context: Context): String? {
            return when (type) {
                OHealthOverview.VitalsType.HR -> context.getString(R.string.text_measuring_heart_rate)
                OHealthOverview.VitalsType.STRESS -> context.getString(R.string.text_measuring_stress)
                OHealthOverview.VitalsType.SPO2 -> context.getString(R.string.text_measuring_spo2)
                OHealthOverview.VitalsType.SKIN_TEMP -> context.getString(R.string.text_measuring_skin_temp)
                else -> "Measuring..."
            }

        }

        private fun animateTileExpand(source: View) {
            source.animate().cancel()
            source.animate().scaleX(1f).scaleY(1f).setDuration(1000)
                .withEndAction {
                    source.animate().scaleX(1f).scaleY(1f).setDuration(1000).start()
                }.start()
        }

        private fun startHeartAnimation(target: ImageView, bpm: Int) {
            stopHeartAnimation(target)
            val safeBpm = bpm.coerceIn(40, 180)
            val beatDurationMs = (60000f / safeBpm).toLong()
            val animator = android.animation.ValueAnimator.ofFloat(1f, 1.2f)
            animator.duration = beatDurationMs / 2
            animator.repeatCount = android.animation.ValueAnimator.INFINITE
            animator.repeatMode = android.animation.ValueAnimator.REVERSE
            animator.addUpdateListener {
                val scale = it.animatedValue as Float
                target.scaleX = scale
                target.scaleY = scale
            }
            animator.start()
            currentAnimator = animator
        }

        private fun stopHeartAnimation(target: ImageView? = null) {
            currentAnimator?.cancel()
            currentAnimator = null
            target?.let {
                it.scaleX = 1f
                it.scaleY = 1f
            }
        }
    }

    class StressCardViewHolder(private val binding: LayoutStressDashMeasureBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(allData: OHealthOverview.StressCard) {
            setStressCardUi(allData)
        }

        private fun setStressCardUi(
            allData: OHealthOverview.StressCard
        ) {
            val data = allData.data

            val lytStress = binding
            lytStress.root.visible()
            lytStress.graphStress.updateData(data?.data)

            val isGen2 = false//allData.ringGeneration == 2//will not be visible now
            val context = binding.root.context

            if (isGen2) {

                when (data?.measureState) {
                    TapMeasureState.NO_DEVICE -> {
                        lytStress.lottieAnimView.invisible()
                        lytStress.imvHrMeasure.visible()
                        lytStress.tvUnableToMeasure.gone()

                        lytStress.groupValue.gone()
                        lytStress.tvEmptyConnect.visible()
                        lytStress.tvEmptyConnect.text =
                            lytStress.tvEmptyConnect.context.getString(R.string.text_connect_your_device_to_measure)

                    }

                    TapMeasureState.LAST_MEASURED -> {
                        lytStress.lottieAnimView.invisible()
                        lytStress.imvHrMeasure.visible()
                        lytStress.tvUnableToMeasure.gone()

                        lytStress.groupValue.visible()
                        lytStress.tvEmptyConnect.apply {
                            visible()
                            setTextColor(Color.parseColor("#ffffff"))
                            text = context.getString(R.string.text_tap_to_measure)
                        }

                        lytStress.tvHeartValue.text =
                            if (data.value != null) "${data.value}" else ""
                        val (displayValue, displayColor) = allData.stressStatus
                        /*lytStress.tvHeartUnit.text = displayValue*/

                        lytStress.tvLastMeasure.apply {
                            setTextColor(Color.parseColor("#a3ffffff"))
                            text = data.lastTime
                        }
                    }

                    TapMeasureState.MEASURING -> {
                        lytStress.lottieAnimView.visible()
                        lytStress.imvHrMeasure.invisible()
                        lytStress.tvUnableToMeasure.gone()

                        lytStress.groupValue.gone()
                        lytStress.tvEmptyConnect.visible()

                        lytStress.tvEmptyConnect.apply {
                            setTextColor(resources.getColor(R.color.white))
                            text = context.getString(R.string.text_measuring_dots)
                        }
                    }

                    TapMeasureState.DEFAULT -> {
                        lytStress.lottieAnimView.invisible()
                        lytStress.imvHrMeasure.visible()
                        lytStress.tvUnableToMeasure.gone()

                        lytStress.groupValue.gone()
                        lytStress.tvEmptyConnect.visible()
                        lytStress.tvEmptyConnect.apply {
                            setTextColor(Color.parseColor("#ffffff"))
                            text = context.getString(R.string.text_tap_to_measure)
                        }
                    }

                    TapMeasureState.ERROR -> {
                        lytStress.lottieAnimView.invisible()
                        lytStress.imvHrMeasure.visible()

                        lytStress.groupValue.visible()
                        lytStress.tvEmptyConnect.gone()
                        lytStress.tvHeartValue.gone()

                        lytStress.tvLastMeasure.apply {
                            setTextColor(Color.parseColor("#88b0ff"))
                            text = context.getString(R.string.text_try_again)
                        }
                        /*lytStress.tvHeartUnit.text = context.getString(R.string.text_unable_to_measure)*/
                        lytStress.tvUnableToMeasure.visible()

                    }

                    TapMeasureState.HIDE -> {
                        lytStress.lottieAnimView.gone()
                        lytStress.imvHrMeasure.gone()

                        lytStress.groupValue.gone()
                        lytStress.tvEmptyConnect.gone()
                        lytStress.tvHeartValue.gone()
                        lytStress.tvUnableToMeasure.gone()
                    }

                    null -> {}
                }
                lytStress.imvHrMeasure.setOnClickListener {
                    itemClickListener?.invoke(
                        OSummaryHealthOverviewClickEnum.OnStressMeasureImvClicked(allData)
                    )
                }


                /*binding.graphStress.updateData(data.data)
            binding.tvBeta.setVisibilityByCondition(data.isBeta)
            binding.ivBackBeta.setVisibilityByCondition(data.isBeta)*/
            } else {
                lytStress.tvUnableToMeasure.gone()
                lytStress.lottieAnimView.gone()
                lytStress.imvHrMeasure.gone()
                lytStress.tvLastMeasure.gone()
                lytStress.tvHeartValue.gone()
                lytStress.tvEmptyConnect.gone()
                lytStress.groupValue.gone()

//            val (lastMeasuredValue, lastMeasuredIndex) = viewModel.getLastMeasuredValue(data.listData)
                val (lastMeasuredValue, lastMeasuredIndex) = allData.lastMeasuredValue


                if (lastMeasuredValue == 0) {
                    /*if (lastMeasuredValue == 0 || allData.isRingPaired!=true) {
                        lytStress.tvStressValue.gone()
                        lytStress.tvStressStatus.gone()
                        lytStress.tvLastUpdate.gone()*/
                    lytStress.lytTrend.root.gone()
                } else {
                    /*lytStress.tvStressValue.visible()
                    lytStress.tvStressStatus.visible()
                    lytStress.tvLastUpdate.visible()

                    lytStress.tvStressValue.text = "$lastMeasuredValue"
//                val (displayValue, displayColor) = viewModel.getStressStatus(lastMeasuredValue)
                    val (displayValue, displayColor) = allData.stressStatus
                    lytStress.tvStressStatus.text = displayValue
                    lytStress.tvStressStatus.setTextColor(displayColor)*/


                    val lastUpdatedTimestamp =
                        DateTimeUtil.getTodayMidnightTimestamp() + (lastMeasuredIndex + 1) * 15 * 60 * 1000


                    val currentTimeStamp = DateFormats.getTimeStamp()
                    val timeDiff = currentTimeStamp - lastUpdatedTimestamp
                    if (timeDiff <= (15 * 60 * 1000)) {

//                    val trendPercent = viewModel.getStressTrend(data.listData, lastMeasuredIndex)
                        val trendPercent = allData.stressTrend

                        if (trendPercent != null && trendPercent != 0) {
                            if (trendPercent > 0) {
                                lytStress.lytTrend.apply {
                                    ivTrend.setImageResource(R.drawable.ic_trend_dash_red)
                                    backLayer.setBackgroundColor(Color.parseColor("#4DFF4365"))
                                    tvPercent.text = "$trendPercent%"
                                    tvPercent.setTextColor(Color.parseColor("#FF426F"))
                                    root.visible()
                                }

                            } else {
                                lytStress.lytTrend.apply {
                                    ivTrend.setImageResource(R.drawable.ic_trend_dash_green)
                                    backLayer.setBackgroundColor(Color.parseColor("#6629CC74"))
                                    tvPercent.text = "${abs(trendPercent)}%"
                                    tvPercent.setTextColor(Color.parseColor("#00FF66"))
                                    root.visible()
                                }
                            }
                        } else {
                            lytStress.lytTrend.root.gone()
                        }
                    } else {
                        lytStress.lytTrend.root.gone()
                    }


                    /*if (lastUpdatedTimestamp == 0L) {
                        lytStress.tvLastUpdate.text = ""
                    } else {
                        lytStress.tvLastUpdate.text =
                            lytStress.tvLastUpdate.context.getString(
                                R.string.text_updated_value,
                                DateTimeUtil.getRelativeTime(
                                    lastUpdatedTimestamp,
                                    allData.resourcesProvider
                                ).lowercase()
                            )
                    }*/
                }
            }

            //
            binding.root.setOnClickListener {
                Log.d("hjcacajc", "Stress Clicked: In Adapter")
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.StressCardClicked)
            }
        }
    }

    class SevenDayTrendsCardViewHolder(private val binding: ItemSevenDayTrendsCardBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(data: OHealthOverview.SevenDayTrendsCard) {
//            Log.d("yashlogii", "setWorkoutUI: BIND ${data.trendsData?.toString()}")
            updateSleepAvgUi(
                Pair(
                    data.trendsData?.sleepScoreAvg, data.trendsData?.activityScoreAvg
                ),
                data.chartModelSleep,
                data.chartModelActivity,
                data.chartModelEmpty
            )
            updateReadinessAvgUi(
                data.trendsData?.readinessScoreAvg,
                data.chartModelReadiness,
                data.chartModelEmpty
            )
        }

        private fun updateSleepAvgUi(
            data: Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>,
            chartModelSleep: List<ChartModel>,
            chartModelActivity: List<ChartModel>,
            chartModelEmpty: List<ChartModel>
        ) {
            /*if(data.first == null || data.second == null){
                return
            }*/
            val lytSleepAvg = binding.lytSleepAvg

            val sleep = data.first
            val activity = data.second

            if (sleep?.sleepScore != null && sleep.sleepScore >= 0) {
                lytSleepAvg.tvSleepScore.visible()
                lytSleepAvg.tvSleepScore.text = sleep.sleepScore.toString()
                lytSleepAvg.tvDaysAvg.visible()
                lytSleepAvg.sleepLineChart.visible()
                lytSleepAvg.sleepLine.root.visible()
                val trendValue = "${kotlin.math.abs(sleep?.trend ?: 0)}%"
                if (sleep?.trend != null && sleep.trend > 0) {
                    lytSleepAvg.sleepTrendValue.text = trendValue
                    lytSleepAvg.sleepTrendValue.setTextColor(Color.parseColor("#29cc74"))
                    lytSleepAvg.sleepTrendImv.loadImage(
                        lytSleepAvg.sleepTrendImv.context, R.drawable.ic_trend_up
                    )
                    lytSleepAvg.sleepTrendImv.visible()
                    lytSleepAvg.sleepTrendValue.visible()
                    lytSleepAvg.tvSleepFromLast.visible()
                } else if (sleep.trend != null && sleep.trend < 0) {
                    lytSleepAvg.sleepTrendValue.text = trendValue
                    lytSleepAvg.sleepTrendImv.loadImage(
                        lytSleepAvg.sleepTrendImv.context, R.drawable.ic_trend_down
                    )
                    lytSleepAvg.sleepTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                    lytSleepAvg.sleepTrendImv.visible()
                    lytSleepAvg.sleepTrendValue.visible()
                    lytSleepAvg.tvSleepFromLast.visible()
                } else {
                    lytSleepAvg.sleepTrendImv.invisible()
                    lytSleepAvg.sleepTrendValue.invisible()
                    lytSleepAvg.tvSleepFromLast.invisible()
                }


                lytSleepAvg.sleepLineChart.updateDataWithMaxMin(
                    chartModelSleep, ArrayList(), ArrayList(), 20, true
                )
            } else {
                lytSleepAvg.sleepLineChart.updateDataWithMaxMin(
                    chartModelEmpty,
                    ArrayList(),
                    ArrayList(),
                    20,
                    true
                )

                lytSleepAvg.tvSleepScore.text = "--"
                lytSleepAvg.tvDaysAvg.visible()
                lytSleepAvg.sleepTrendImv.invisible()
                lytSleepAvg.sleepTrendValue.invisible()
                lytSleepAvg.tvSleepFromLast.invisible()
                lytSleepAvg.sleepLine.root.invisible()
            }

            if (activity?.activityScore != null && activity.activityScore >= 0) {

                lytSleepAvg.tvActivityScore.text = activity.activityScore.toString()
                lytSleepAvg.activityLineChart.updateDataWithMaxMin(
                    chartModelActivity, ArrayList(), ArrayList(), 20, true
                )
                lytSleepAvg.tvDaysAvg1.visible()
                lytSleepAvg.activityLineChart.visible()
                lytSleepAvg.activityLine.root.visible()
                lytSleepAvg.activityTrendImv.visible()
                lytSleepAvg.activityTrendValue.visible()
                lytSleepAvg.tvActivityFrom.visible()

                val trendValue = "${kotlin.math.abs(activity.trend ?: 0)}%"
                if (activity.trend != null && activity.trend > 0) {
                    lytSleepAvg.activityTrendValue.text = trendValue
                    lytSleepAvg.activityTrendValue.setTextColor(Color.parseColor("#29cc74"))
                    lytSleepAvg.activityTrendImv.loadImage(
                        lytSleepAvg.sleepTrendImv.context, R.drawable.ic_trend_up
                    )
                    lytSleepAvg.activityTrendImv.visible()
                    lytSleepAvg.activityTrendValue.visible()
                    lytSleepAvg.tvActivityFrom.visible()
                } else if (activity.trend != null && activity.trend < 0) {
                    lytSleepAvg.activityTrendValue.text = trendValue
                    lytSleepAvg.activityTrendImv.loadImage(
                        lytSleepAvg.sleepTrendImv.context, R.drawable.ic_trend_down
                    )
                    lytSleepAvg.activityTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                    lytSleepAvg.activityTrendImv.visible()
                    lytSleepAvg.activityTrendValue.visible()
                    lytSleepAvg.tvActivityFrom.visible()
                } else {
                    lytSleepAvg.activityTrendImv.invisible()
                    lytSleepAvg.activityTrendValue.invisible()
                    lytSleepAvg.tvActivityFrom.invisible()
                }
                //                binding.activityLineChart.updateDataWithMax(data.activityValue, ArrayList(), ArrayList())
            } else {
                lytSleepAvg.tvActivityScore.text = "--"
                lytSleepAvg.activityLineChart.updateDataWithMaxMin(
                    chartModelEmpty,
                    ArrayList(),
                    ArrayList(),
                    20,
                    true
                )

                lytSleepAvg.tvDaysAvg1.visible()
                //lytSleepAvg.activityLineChart.gone()
                lytSleepAvg.activityLine.root.invisible()
                lytSleepAvg.activityTrendImv.invisible()
                lytSleepAvg.activityTrendValue.invisible()
                lytSleepAvg.tvActivityFrom.invisible()
            }


            binding.root.setOnClickListener {
                //   itemClickListener?.invoke(it, data, position)
            }
        }

        private fun updateReadinessAvgUi(
            data: ODashboardReadinessScoreModel?,
            chartModelReadiness: List<ChartModel>,
            chartModelEmpty: List<ChartModel>
        ) {
            val lytReadinessAvg = binding.lytReadinessAvg
            if (data?.readinessScore != null && data.readinessScore >= 0) {
                lytReadinessAvg.tvSleepScore.text = data.readinessScore.toString()
                lytReadinessAvg.tvDaysAvg.visible()
                lytReadinessAvg.tvSleepScore.visible()
                lytReadinessAvg.lineChart.visible()
                val trendValue = "${kotlin.math.abs(data.trend ?: 0)}%"
                if (data.trend != null && data.trend > 0) {
                    lytReadinessAvg.sleepTrendValue.text = trendValue
                    lytReadinessAvg.sleepTrendValue.setTextColor(Color.parseColor("#29cc74"))
                    lytReadinessAvg.sleepTrendImv.loadImage(
                        lytReadinessAvg.sleepTrendImv.context, R.drawable.ic_trend_up
                    )
                    lytReadinessAvg.sleepTrendImv.visible()
                    lytReadinessAvg.sleepTrendValue.visible()
                    lytReadinessAvg.tvSleepFromLast.visible()
                } else if (data.trend != null && data.trend < 0) {
                    lytReadinessAvg.sleepTrendValue.text = trendValue
                    lytReadinessAvg.sleepTrendImv.loadImage(
                        lytReadinessAvg.sleepTrendImv.context, R.drawable.ic_trend_down
                    )
                    lytReadinessAvg.sleepTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                    lytReadinessAvg.sleepTrendImv.visible()
                    lytReadinessAvg.sleepTrendValue.visible()
                    lytReadinessAvg.tvSleepFromLast.visible()
                } else {
                    lytReadinessAvg.sleepTrendImv.invisible()
                    lytReadinessAvg.sleepTrendValue.invisible()
                    lytReadinessAvg.tvSleepFromLast.invisible()
                }



                lytReadinessAvg.lineChart.updateDataWithMaxMin(
                    chartModelReadiness, ArrayList(), ArrayList(), 20, true
                )
            } else {
                lytReadinessAvg.lineChart.updateDataWithMaxMin(
                    chartModelEmpty,
                    ArrayList(),
                    ArrayList(),
                    20,
                    true
                )

                lytReadinessAvg.tvSleepScore.text = "--"
                lytReadinessAvg.tvDaysAvg.visible()
                lytReadinessAvg.lineChart.visible()
                lytReadinessAvg.sleepTrendImv.invisible()
                lytReadinessAvg.sleepTrendValue.invisible()
                lytReadinessAvg.tvSleepFromLast.invisible()
            }
        }
    }

    class WorkoutsHistoryViewHolder(private val binding: OreoLayoutRecentActivityBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(data: OHealthOverview.WorkoutHistoryCardData) {
            setWorkoutUI(data.workouts)
        }

        private fun setWorkoutUI(workouts: List<OActivityListModal>?) {
            val lytWorkouts = binding
            val context = lytWorkouts.root.context
            lytWorkouts.root.visible()

            if (workouts.isNullOrEmpty()) {
                lytWorkouts.tvEmptyMsg.visible()
                lytWorkouts.tvEmptyMsg.text = context.getString(R.string.text_tap_plus_workout)

            } else {
                lytWorkouts.tvEmptyMsg.gone()
            }
            lytWorkouts.rvWorkouts.layoutManager = LinearLayoutManager(
                lytWorkouts.rvWorkouts.context, LinearLayoutManager.VERTICAL, false
            )
            val adapter1 = OreoRWorkoutAdapter(object : OreoRWorkoutAdapter.OnItemClickListener {
                override fun onItemClick(data: OActivityListModal, position: Int) {
                    itemClickListener?.invoke(
                        OSummaryHealthOverviewClickEnum.OnWorkoutsHistoryCardOworkoutAdapterItemClicked(
                            data,
                            position
                        )
                    )
                }
            })

            lytWorkouts.rvWorkouts.apply {
                adapter = adapter1
            }
            adapter1.setData(workouts ?: ArrayList())
            lytWorkouts.viewAddWorkout.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.OnViewAddWorkout
                )
            }

            lytWorkouts.root.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.OnWorkoutsHistoryCardClicked
                )
            }

        }
    }

    class DailyGoalsViewHolder(private val binding: LayoutNotificationCardBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            notificationGoal: OHealthOverview.DailyGoalsCardData,
            position: Int
        ) {
            setNotificationGoalsCardData(notificationGoal.notificationGoals)
            initListener(position, binding, notificationGoal)
        }

        private fun setNotificationGoalsCardData(notificationGoal: NotificationGoals) {

            binding.apply {
                root.visible()
                val stepsGoal = notificationGoal.steps_required ?: 5000

                tvSteps.text =
                    if (notificationGoal.steps == null) "0" else notificationGoal.steps.toString()
                tvStepsGoal.text = "/$stepsGoal"

                val userSteps = notificationGoal.steps ?: 0
                val percent = (userSteps.toFloat() / stepsGoal.toFloat()) * 100

                progressSteps.progress = percent

                if (percent >= 100) {
                    textStepsGoalAchieved.visible()
                } else {
                    textStepsGoalAchieved.gone()
                }

                val hydrateGoal = notificationGoal.hydration_required ?: 3000
                val hydrate = notificationGoal.hydration ?: 0


                val hydrationText = StringBuilder()
                var hydratePercent = notificationGoal.hydratePercent ?: 0f

                if (notificationGoal.isMetric == true) {
                    hydrationText.append((hydrate.toFloat() / 1000))
                    hydrationText.append("/")
                    hydrationText.append((hydrateGoal.toFloat() / 1000))
                    hydrationText.append("L")


                    hydratePercent = (hydrate.toFloat() / hydrateGoal.toFloat()) * 100

                } else {

                    val convertedHydrate = hydrate.toFloat() * 0.033814
                    hydrationText.append(String.format("%.1f", convertedHydrate))
                    hydrationText.append("/")

                    val convertedHydrateGoal =
                        notificationGoal.convertedHydrateGoal

                    hydrationText.append("$convertedHydrateGoal")
                    hydrationText.append("oz")

                    if (convertedHydrateGoal != null) {
                        hydratePercent =
                            (convertedHydrate.toFloat() / convertedHydrateGoal.toFloat()) * 100
                    }

                }
                progressHydrate.progress = hydratePercent

                if (hydratePercent >= 100) {
                    textHydrateGoalAchieved.visible()
                } else {
                    textHydrateGoalAchieved.gone()
                }

                tvHydration.text = hydrationText

                notificationGoal.glassImage?.let { ivGlassImage.setImageResource(it) }


                LOGS.d("sdfkljsdfklhjsdfkj  received value ${notificationGoal.notificationToggleModel?.hydrate_notification}")
                if (notificationGoal.notificationToggleModel?.hydrate_notification == true &&
                    notificationGoal.notificationToggleModel?.master_notification == true
                ) {
                    ivNotificationHydrate.setImageResource(R.drawable.ic_hydrate_notify_on)
                } else {
                    ivNotificationHydrate.setImageResource(R.drawable.ic_hydrate_notify_off)
                }

                if (notificationGoal.notificationToggleModel?.steps_notification == true &&
                    notificationGoal.notificationToggleModel?.steps_notification == true
                ) {
                    ivNotificationSteps.setImageResource(R.drawable.ic_steps_notify_on)
                } else {
                    ivNotificationSteps.setImageResource(R.drawable.ic_steps_notify_off)
                }

                if (notificationGoal.showHydrateFade) {
                    notificationGoal.showHydrateFade = false
                    handleMessageFade(
                        binding.textView153, binding.textHydrateReminderMessage,
                        binding.textView89, binding.textStepsReminderMessage,
                        Pair(
                            NotificationGoal.HYDRATE,
                            notificationGoal.notificationToggleModel?.hydrate_notification ?: false
                        )
                    )
                }
                if (notificationGoal.showStepsFade) {
                    notificationGoal.showStepsFade = false
                    handleMessageFade(
                        binding.textView153, binding.textHydrateReminderMessage,
                        binding.textView89, binding.textStepsReminderMessage,
                        Pair(
                            NotificationGoal.STEPS,
                            notificationGoal.notificationToggleModel?.steps_notification ?: false
                        )
                    )
                }
            }
        }

        fun initListener(
            position: Int,
            binding: LayoutNotificationCardBinding,
            notificationGoal: OHealthOverview.DailyGoalsCardData
        ) {
            binding.apply {
                ivNotificationSteps.setOnClickListener {
                    if (ApplicationUtils.isInternetConnected().not()) {
                        binding.root.context.showShortToast(binding.root.context.getString(R.string.text_no_internet_connection))
                        return@setOnClickListener
                    }

                    itemClickListener?.invoke(
                        OSummaryHealthOverviewClickEnum.OnIvNotificationStepsClicked(
                            position
                        )
                    )

                }

                ivNotificationHydrate.setOnClickListener {
                    if (ApplicationUtils.isInternetConnected().not()) {
                        binding.root.context.showShortToast(binding.root.context.getString(R.string.text_no_internet_connection))
                        return@setOnClickListener
                    }

                    itemClickListener?.invoke(
                        OSummaryHealthOverviewClickEnum.OnIvNotificationHydrateClicked(
                            position
                        )
                    )
                }

                ivHydrateMinus.setOnClickListener {
                    itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnIvHydrateMinusClicked)
                }

                ivHydratePlus.setOnClickListener {
                    itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnIvHydratePlusClicked)
                }

                tvEdit.setOnClickListener {
                    itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnEditGoalsCardEditClicked)
                }
            }
        }

        fun handleMessageFade(
            hydrateTextViewMain: TextView,
            hydrateTextViewMessage: TextView,
            stepsTextViewMain: TextView,
            stepsTextViewMessage: TextView,
            pair: Pair<NotificationGoal, Boolean>
        ) {
            when (pair.first) {
                NotificationGoal.HYDRATE -> {
                    if (pair.second) {
                        hydrateTextViewMessage.text =
                            binding.root.context.getString(
                                R.string.text_reminder_active
                            )
                    } else {
                        hydrateTextViewMessage.text =
                            binding.root.context.getString(
                                R.string.text_reminder_silent
                            )
                    }
                    notificationTextFade(
                        hydrateTextViewMain,
                        hydrateTextViewMessage
                    )
                }

                NotificationGoal.STEPS -> {
                    if (pair.second) {
                        stepsTextViewMessage.text =
                            binding.root.context.getString(
                                R.string.text_reminder_active
                            )
                    } else {
                        stepsTextViewMessage.text =
                            binding.root.context.getString(
                                R.string.text_reminder_silent
                            )
                    }
                    notificationTextFade(
                        stepsTextViewMain,
                        stepsTextViewMessage
                    )
                }
            }
        }

        private fun notificationTextFade(textView1: TextView, textView2: TextView) {
            textView1.visibility = View.VISIBLE
            textView2.visibility = View.INVISIBLE

            val fadeIn: Animation =
                AnimationUtils.loadAnimation(textView1.context, R.anim.fade_in_goal)
            val fadeOut: Animation =
                AnimationUtils.loadAnimation(textView1.context, R.anim.fade_out_goal)

            textView1.startAnimation(fadeOut)
            textView1.visibility = View.INVISIBLE
            textView2.visibility = View.VISIBLE
            textView2.startAnimation(fadeIn)

            Handler(Looper.getMainLooper()).postDelayed({
                textView2.startAnimation(fadeOut)
                textView2.visibility = View.INVISIBLE
                textView1.visibility = View.VISIBLE
                textView1.startAnimation(fadeIn)
            }, 1500)

        }
    }

    class HeartRateViewHolder(private val binding: ListHeartRateCardItemBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: OHealthOverview.HeartRateDataModel
        ) {
            setHearRateCardUi(data)
        }

        private fun setHearRateCardUi(data: OHealthOverview.HeartRateDataModel) {
            val lytHeartRate = binding
            lytHeartRate.root.visible()
            val heartRateText = binding.root.context.getString(R.string.text_heart_rate)
            val heartRateShortText = if (heartRateText.length <= 10) heartRateText
            else "${heartRateText.take(10)}.."

            lytHeartRate.candleChart.enableInteractiveMode(false)
            lytHeartRate.candleChart.updateData(
                data.hrCombineModel,
                3, data.minValues, data.maxValues
            )

            val isGen1 = data.ringGeneration == 1
            val context = binding.root.context

            if (isGen1) {
                val lastMeasuredValue = data.lastMeasuredValue
                val lastMeasuredIndex = data.lastMeasuredIndex

                if (lastMeasuredValue == 0) {
                    lytHeartRate.lytTrend.root.gone()
                    binding.textView.text = heartRateText
                } else {
                    val lastUpdatedTimestamp =
                        DateTimeUtil.getTodayMidnightTimestamp() + (lastMeasuredIndex + 1) * 5 * 60 * 1000


                    val currentTimeStamp = DateFormats.getTimeStamp()
                    val timeDiff = currentTimeStamp - lastUpdatedTimestamp
                    if (timeDiff <= (5 * 60 * 1000)) {

                        val trendPercent = data.trendPercent

                        if (trendPercent != null && trendPercent != 0) {
                            if (trendPercent > 0) {
                                lytHeartRate.lytTrend.apply {
                                    ivTrend.setImageResource(R.drawable.ic_trend_dash_red)
                                    backLayer.setBackgroundColor(Color.parseColor("#4DFF4365"))
                                    tvPercent.text = "$trendPercent%"
                                    tvPercent.setTextColor(Color.parseColor("#FF426F"))
                                    binding.textView.text = heartRateShortText
                                    root.visible()
                                }
                            } else {
                                lytHeartRate.lytTrend.apply {
                                    ivTrend.setImageResource(R.drawable.ic_trend_dash_green)
                                    backLayer.setBackgroundColor(Color.parseColor("#6629CC74"))
                                    tvPercent.text = "${abs(trendPercent)}%"
                                    tvPercent.setTextColor(Color.parseColor("#00FF66"))
                                    binding.textView.text = heartRateShortText
                                    root.visible()
                                }
                            }
                        } else {
                            lytHeartRate.lytTrend.root.gone()
                            binding.textView.text = heartRateText
                        }
                    } else {
                        lytHeartRate.lytTrend.root.gone()
                        binding.textView.text = heartRateText
                    }
                }



                when (data.measureState) {
                    TapMeasureState.NO_DEVICE -> {
                        lytHeartRate.lottieAnimView.invisible()
                        lytHeartRate.imvHrMeasure.visible()

                        lytHeartRate.groupValue.gone()
                        lytHeartRate.tvEmptyConnect.visible()
                        lytHeartRate.tvEmptyConnect.text =
                            lytHeartRate.tvEmptyConnect.context.getString(R.string.text_connect_your_device_to_measure)

                    }

                    TapMeasureState.LAST_MEASURED -> {
                        lytHeartRate.lottieAnimView.invisible()
                        lytHeartRate.imvHrMeasure.visible()

                        lytHeartRate.groupValue.visible()
                        lytHeartRate.tvEmptyConnect.visible()
                        lytHeartRate.tvEmptyConnect.apply {
                            text = context.getString(R.string.text_tap_to_measure)
                        }

                        lytHeartRate.tvHeartValue.text = data.value
                        lytHeartRate.tvHeartUnit.text =
                            binding.root.context.getString(R.string.text_bpm_small)

                        lytHeartRate.tvLastMeasure.apply {
                            setTextColor(Color.parseColor("#a3ffffff"))
                            text = data.lastTime
                        }

                    }

                    TapMeasureState.MEASURING -> {
                        lytHeartRate.lottieAnimView.visible()
                        lytHeartRate.imvHrMeasure.invisible()

                        lytHeartRate.groupValue.gone()
                        lytHeartRate.tvEmptyConnect.visible()

                        lytHeartRate.tvEmptyConnect.apply {
                            setTextColor(resources.getColor(R.color.white))
                            text = context.getString(R.string.text_measuring_dots)
                        }
                    }

                    TapMeasureState.DEFAULT -> {
                        lytHeartRate.lottieAnimView.invisible()
                        lytHeartRate.imvHrMeasure.visible()

                        lytHeartRate.groupValue.gone()
                        lytHeartRate.tvEmptyConnect.visible()
                        lytHeartRate.tvEmptyConnect.apply {
                            /*setTextColor(Color.parseColor("#88b0ff"))*/
                            text = context.getString(R.string.text_tap_to_measure)
                        }
                    }

                    TapMeasureState.ERROR -> {
                        lytHeartRate.lottieAnimView.invisible()
                        lytHeartRate.imvHrMeasure.visible()

                        lytHeartRate.groupValue.visible()
                        lytHeartRate.tvEmptyConnect.gone()
                        lytHeartRate.tvHeartValue.gone()

                        lytHeartRate.tvLastMeasure.apply {
                            setTextColor(Color.parseColor("#88b0ff"))
                            text = context.getString(R.string.text_try_again)
                        }
                        lytHeartRate.tvHeartUnit.text =
                            binding.root.context.getString(R.string.text_unable_to_measure)

                    }

                    TapMeasureState.HIDE -> {
                        lytHeartRate.lottieAnimView.invisible()
                        lytHeartRate.imvHrMeasure.invisible()

                        lytHeartRate.groupValue.invisible()
                        lytHeartRate.tvEmptyConnect.gone()
                        lytHeartRate.tvHeartValue.gone()
                    }
                }
            } else {
                lytHeartRate.lottieAnimView.gone()
                lytHeartRate.imvHrMeasure.gone()

                lytHeartRate.groupValue.gone()
                lytHeartRate.tvEmptyConnect.gone()
                lytHeartRate.tvHeartValue.gone()
            }

            lytHeartRate.root.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.OnHeartRateCardClicked
                )
            }

            lytHeartRate.imvHrMeasure.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.OnHeartMeasureImvClicked(
                        data
                    )
                )
            }


            if (data.alertCount == 0) {
                binding.lytHrSpike.root.gone()
                binding.bInfo.visible()
            } else {
                binding.lytHrSpike.root.visible()
                binding.bInfo.gone()
            }
        }
    }

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

            val context = binding.root.context

            binding.lytTalkToLunaAi.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnAiCardClicked)
            }

            if (data.dailyHealthDigestCardState == SummaryStates.DATA_AVAILABLE) {
                binding.lytDailyHealthDigest.mainCard.setBackgroundResource(R.drawable.bg_daily_health_digest_main_dash)
                binding.lytDailyHealthDigest.root.isClickable = true
            } else {
                binding.lytDailyHealthDigest.mainCard.setBackgroundResource(R.drawable.bg_daily_health_digest_other_dash)
                binding.lytDailyHealthDigest.root.isClickable = false
            }

            when (data.dailyHealthDigestCardState) {
                SummaryStates.NO_DEVICE -> {
                    binding.lytDailyHealthDigest.imageView11.gone()
                    binding.lytDailyHealthDigest.tvTitle.text =
                        context.getString(R.string.text_ring_not_connected)
                }

                SummaryStates.NO_DATA -> {
                    binding.lytDailyHealthDigest.imageView11.gone()
                    binding.lytDailyHealthDigest.tvTitle.text =
                        context.getString(R.string.text_no_data_found)
                }

                SummaryStates.GENERATING -> {
                    binding.lytDailyHealthDigest.imageView11.gone()
                    binding.lytDailyHealthDigest.tvTitle.text =
                        context.getString(R.string.text_generating)
                }

                SummaryStates.DATA_AVAILABLE -> {
                    binding.lytDailyHealthDigest.imageView11.visible()
                    binding.lytDailyHealthDigest.tvTitle.text =
                        context.getString(R.string.text_daily_nhealth_digest)
                    binding.lytDailyHealthDigest.root.setOnClickListener {
                        itemClickListener?.invoke(
                            OSummaryHealthOverviewClickEnum.OnDailyDigestMainCardClicked
                        )
                    }
                }

                SummaryStates.NONE, null -> {
                    binding.lytDailyHealthDigest.tvTitle.text =
                        context.getString(R.string.text_no_data_found)
                }
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
                Log.d("hjcacajc", "Stress Clicked: In Adapter")
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.StressCardClicked)
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

            if (data.data.alertCount == 0) {
                binding.lytHrvSpike.root.gone()
                binding.imageView14.visible()
            } else {

                binding.lytHrvSpike.tvText.text =
                    binding.lytHrvSpike.root.context.getString(R.string.text_hrv)
                binding.lytHrvSpike.imgUpDown.setImageResource(R.drawable.ic_arrow_red_down)
                binding.lytHrvSpike.root.visible()
                binding.imageView14.gone()
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

            if (data.data.alertCount == 0) {
                binding.lytHrvSpike.root.gone()
                binding.imageView14.visible()
            } else {
                binding.lytHrvSpike.tvText.text =
                    binding.lytHrvSpike.root.context.getString(R.string.text_hrv)
                binding.lytHrvSpike.imgUpDown.setImageResource(R.drawable.ic_arrow_red_down)
                binding.lytHrvSpike.root.visible()
                binding.imageView14.gone()
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
                //binding.tvActivityScore.text = "--"
                binding.tvValue.text = "--"
                binding.tvStatus.text = binding.tvStatus.context.getString(R.string.text_no_data)

            } else {
                binding.tvValue.text = scoreValue.toString()
                //binding.tvActivityScore.text = scoreValue.toString()
                val statusColor = ContextCompat.getColor(
                    binding.root.context,
                    getStatusColors(data.data.statusCode ?: "")
                )

                binding.tvStatus.setTextColor(statusColor)
                binding.tvStatus.text = data.data.status
            }


            if (data.data.nudges.isNullOrEmpty()) {
                binding.tvNudge.gone()
            } else {
                binding.tvNudge.visible()
                binding.tvNudge.text = data.data.nudges.firstOrNull()?.message ?: ""
            }

            //binding.tvTotalCalories.text = caloriesGoalText

            /*binding.tvActiveCalories.text = if ((data.data.activeCalories ?: 0) > 0) {
                data.data.activeCalories.toString()
            } else {
                "--"
            }*/

            val caloriesGoalText = "${data.caloriesGoal}"
            binding.tvActivityScore.text = if ((data.data.activeCalories ?: 0) > 0) {
                data.data.activeCalories.toString() + "/$caloriesGoalText"
            } else {
                "--/$caloriesGoalText"
            }

            if ((scoreValue ?: 0) >= 0) {
                binding.dynamicArcView.repeatCount = 0
                binding.dynamicArcView.setAnimation(R.raw.lottie_meter_activity)
                binding.dynamicArcView.setMaxProgress(
                    MiscUtil.scorePercentCalculator(
                        (scoreValue ?: 0).toFloat()
                    )
                )
                binding.dynamicArcView.playAnimation()

            }


            var caloriesPercent =
                ((data.data.activeCalories ?: 0).toFloat() / data.caloriesGoal.toFloat()) * 100

            if (caloriesPercent > 100) {
                caloriesPercent = 100f
            }

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

    class CaffeineRestrictedViewHolder(private val binding: LayoutCaffeineCalibratingBinding) :
        HomeRecyclerViewHolder(binding) {

        fun bind(data: OHealthOverview.CaffeineWindowCalibrating) {

            binding.tvTitle.text = data.title
            binding.tvMessage.text = data.message

        }


    }

    class CircadianLockedNoSleepCardViewHolder(private val binding: LayoutDashNoSleepStatesCircadianBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(data: OHealthOverview.CircadianLockedOrNoSleepCard) {
            val context = binding.root.context
            if (data.isLocked == false) {
                binding.lytLockedState.gone()
                binding.lytNoSleepData.visible()
                binding.textView182.text =
                    context.getString(R.string.text_we_don_t_have_your_sleep_data_from_last_night_but_no_worries_you_can_quickly_log_your_sleep_now_to_unlock_today_s_rhythm_guide)
            } else {
                binding.lytNoSleepData.gone()
                binding.lytLockedState.visible()
                binding.textView182.text =
                    context.getString(R.string.text_your_sleep_for_yesterday_has_not_been_recorded_please_log_your_sleep_data_to_see_this_card_active)
            }

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnCircadianAlignmentCardClicked)
            }

            binding.llLytLog.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.OnLogActivityClicked(
                        CircadianAlignmentViewModel.sleep_key
                    )
                )
            }
        }
    }

    class CircadianAlignmentViewHolder(private val binding: LayoutDashCircadianBinding) :
        HomeRecyclerViewHolder(binding) {

        fun bind(data: OHealthOverview.CircadianAlignment) {
            /*val context = binding.root.context

            val tvWindow = binding.tvWindow
            val tvDesc = binding.tvDesc*/

            LOGS.d("CircadianAlignmentViewHolder ${data.title}")

            if (data.title.isNullOrEmpty()) {
                binding.tvGeneratingNudge.visible()
                binding.tvWindow.gone()
                binding.tvDesc.gone()
            } else {
                binding.tvGeneratingNudge.gone()
                binding.tvWindow.visible()
                binding.tvDesc.visible()
                binding.tvWindow.text = data.title ?: "-"
                binding.tvDesc.text = data.description ?: "-"
            }

            /*if(
                data.title != null &&
                data.description != null
            ){
                tvWindow.text = data.title
                tvDesc.text = data.description
            }else{
                if(data.isLockedCircularView == true){
                    tvWindow.text = context.getString(R.string.text_start_fresh_today)
                    tvDesc.text = context.getString(R.string.text_focus_window_desc1)
                }else{
                    tvWindow.text = context.getString(R.string.text_guidance_resumes_soon)
                    tvDesc.text = context.getString(R.string.text_focus_window_desc2)
                }
            }*/

            // Graph
            setCircadianGraph(data)

            binding.root.setOnClickListener {
                itemClickListener?.invoke(OSummaryHealthOverviewClickEnum.OnCircadianAlignmentCardClicked)
            }
        }

        private fun setCircadianGraph(data: OHealthOverview.CircadianAlignment) {
            binding.lytGraphView.graphView.isScrollLocked = true
            binding.lytGraphView.graphView.graphStartTime = LocalTime.of(6, 0)
            binding.lytGraphView.graphView.graphEndTime = LocalTime.of(23, 0)

            data.startTime?.let {
                binding.lytGraphView.graphView.graphStartTime = it
            }

            data.endTime?.let {
                binding.lytGraphView.graphView.graphEndTime = it
            }

            data.timeWindow?.let {
                binding.lytGraphView.graphView.setDataSet(
                    it,
                    data.energyGraph
                )
            }

            binding.lytGraphView.graphView.redraw()
        }

    }

    class CaffeineViewHolder(private val binding: LayoutCardCaffeineDashBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(data: OHealthOverview.CaffeineWindow) {

            val dataa = data.data

            initListener(dataa)

            val context = binding.root.context

            val fullText = dataa.message ?: ""
            val splitIndex = fullText.indexOf(':')

            if (splitIndex != -1) {
                val startingWords = "${fullText.substring(0, splitIndex)}:"
                val remainingText = fullText.substring(splitIndex + 1)

                val spannableString = SpannableString(fullText)

                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    startingWords.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
//                spannableString.setSpan(
//                    ForegroundColorSpan(Color.WHITE),
//                    0,
//                    firstTwoWords.length,
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                )

                binding.tvMessage.text = spannableString
            }

            val graphStart =
                LocalTime.parse(dataa.wakeUpTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val graphEnd = LocalTime.parse(dataa.bedTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val caffeineStart =
                LocalTime.parse(dataa.caffeineStartTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val caffeineEnd =
                LocalTime.parse(dataa.caffeineEndTime, DateTimeFormatter.ofPattern("HH:mm:ss"))


            val todayDate = LocalDate.now()
            val now = LocalDateTime.now()

            val graphStartDate = LocalDateTime.of(todayDate, graphStart)
            val caffeineStartDate = LocalDateTime.of(todayDate, caffeineStart)
            val caffeineEndDate = LocalDateTime.of(todayDate, caffeineEnd)
            val graphEndDate = if (caffeineEnd <= graphEnd) {//same day case
                LocalDateTime.of(todayDate, graphEnd)
            } else {//Next day case
                LocalDateTime.of(todayDate.plusDays(1L), graphEnd)
            }

            val startOffset = graphStartDate.minusHours(3)
            when {
                now in startOffset..caffeineStartDate -> {
                    binding.tvState.apply {
                        text = context.getString(R.string.text_restricted)
                        setTextColor("#FF6389".toColorInt())
                    }
                }

                now in caffeineStartDate..caffeineEndDate -> {
                    binding.tvState.apply {
                        text = context.getString(R.string.text_open)
                        setTextColor("#0EF377".toColorInt())
                    }
                }

                now in caffeineEndDate..graphEndDate -> {
                    binding.tvState.apply {
                        text = context.getString(R.string.text_restricted)
                        setTextColor("#FF6389".toColorInt())
                    }
                }

                else -> {
                    binding.tvState.apply {
                        text = context.getString(R.string.text_restricted)
                        setTextColor("#FF6389".toColorInt())
                    }
                }
            }

            binding.caffeineGraphView.updateData(dataa)
        }

        private fun initListener(data: CaffeineWindowData) {
            binding.root.setOnClickListener {
                itemClickListener?.invoke(
                    OSummaryHealthOverviewClickEnum.OnCaffeineDashCardClicked(
                        data
                    )
                )
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
