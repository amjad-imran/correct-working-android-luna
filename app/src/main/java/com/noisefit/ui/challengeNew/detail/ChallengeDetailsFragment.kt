package com.noisefit.ui.challengeNew.detail

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.text.HtmlCompat
import androidx.core.text.color
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.model.challenge.ChallengeIds
import com.noisefit_commans.data.response.ChallengeHistory
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.databinding.FragmentChallengeDetails2Binding
import com.noisefit.ui.challenge.challengeDetail.LEAVE_COMMENT
import com.noisefit.ui.common.*
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
import me.dkzwm.widget.srl.RefreshingListenerAdapter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ChallengeDetailsFragment :
    BaseFragment<FragmentChallengeDetails2Binding>(FragmentChallengeDetails2Binding::inflate) {

    private var markerView: View? = null

    @Inject
    lateinit var screenUtils: ScreenUtils

    private val rewardsAdapter by lazy {
        RewardsAdapter()
    }

    private val leaderBoardAdapter by lazy {
        ChallengeLeaderDetailsAdapter()
    }
    private val dayViewAdapter by lazy {
        DayViewAdapter(object : OnDayClickListener {
            override fun onDayItemClick(date: DateTime) {
                updateProgressData(date)
            }

        })
    }
    private val viewModel: ChallengeDetailsViewModel by viewModels()

    private var mLastClickTime: Long = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back_arrow_new)
        binding.toolbar.setTitleTextAppearance(requireActivity(), R.style.ToolbarTextTheme)
        binding.toolbar.setNavigationOnClickListener {
            if (it.id == -1) {
                navigateUpSafe()
            }
        }
        binding.animationView.setAnimation(R.raw.loading_swipe_anim)


        arguments?.let {
            viewModel.challengeId = it.getInt("challengeId")
            viewModel.shouldReloadDetailData()
        }


        //(activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //(activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowHomeEnabled(true)

        /*with(binding.vJoin) {
            repeatCount = LottieDrawable.INFINITE
            setAnimation(R.raw.anim_join_challenge_btn)
            playAnimation()
        }*/

        if (viewModel.challengeDetails.value == null) {
            viewModel.getChallengeDetailsByID()
        } else {
            viewModel.handleChallengeModel(viewModel.challengeDetails.value!!)
        }
        setAdapter()


    }

    fun runAnimWithCallback(animationRes: Int, onAnimComplete: () -> Unit) {
        val anim = AnimationUtils.loadAnimation(context, animationRes)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                onAnimComplete()
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })
        binding.lytJoinAnim.visible()
        binding.lytJoinAnim.startAnimation(anim)
    }

    private fun myProgressBarThumb(percentage: Float) {

        removeMarkerView()
        markerView = LayoutInflater.from(context)
            .inflate(
                R.layout.custom_challenge_pg_thumb,
                binding.lytContentScrolling.lytProgress.container1,
                false
            )

        val imageView: ImageView = markerView!!.findViewById(R.id.image)
        imageView.setImageResource(viewModel.getChallengeTypeIcon())
        markerView!!.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        markerView!!.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val params =
            RelativeLayout.LayoutParams(markerView!!.measuredWidth, markerView!!.measuredHeight)
        val containerWidth = binding.lytContentScrolling.lytProgress.markerContainer.width.toFloat()
        var margin = percentage.calculateInitFromPercentage(containerWidth)
        if (percentage >= 90) {
            margin -= markerView!!.measuredWidth
        }


        params.marginStart = margin.toInt()
        markerView!!.layoutParams = params
        binding.lytContentScrolling.lytProgress.markerContainer.addView(markerView)

    }


    private fun setAdapter() {
        binding.lytContentScrolling.lytRewards.rvRewards.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = rewardsAdapter
        }

        binding.lytContentScrolling.lytLeaderboard.rvLeaderBoard.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = leaderBoardAdapter
        }

        binding.lytContentScrolling.lytCalendar.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = dayViewAdapter
        }
    }

    override fun initListener() {
        binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                super.onRefreshing()
                binding.swipeToRefresh.refreshComplete()
                if (viewModel.serverReload.value == true) {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGES_USER_ASYNC_CLICK)
                    viewModel.getChallengeDetailsByID(true)
                }
            }
        })

        binding.vJoin.setOnClickListener {
            viewModel.joinChallenge(viewModel.challengeId)
            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.CHALLENGEDETAILSPAGE_JOINCHALLENGE_CLICK,
                HashMap<String, Any>().apply {
                    this["challengename_joinchallenge"] = viewModel.challengeName
                })
        }

        binding.lytContentScrolling.clLeaveChallenge.setOnClickListener {
            showLeaveDialog()
        }

        binding.lytContentScrolling.lytShareChallenge.btnInvite.setOnClickListener {
            if (mLastClickTime != 0L) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.CHALLENGEDETAILSPAGE_INVITECHALLENGE_CLICK,
                HashMap<String, Any>().apply {
                    this["challengename_invitechallenge"] = viewModel.challengeName
                })
            onShareChallenge()
        }

        binding.ivShare.setOnClickListener {
            if (mLastClickTime != 0L) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.CHALLENGEDETAILSPAGE_SHARECHALLENGE_CLICK,
                HashMap<String, Any>().apply {
                    this["challengename_sharechallenge"] = viewModel.challengeName
                })
            onShareChallenge()
        }
    }

    fun onShareChallenge() {
        viewModel.challengeDetails.value?.shareText?.let {
            if (it.isNotBlank()) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, it.replace("\\n", "\n"))
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }
    }

    private fun removeMarkerView() {
        markerView?.let {
            binding.lytContentScrolling.lytProgress.markerContainer.removeAllViews()
            markerView = null
        }

    }

    override fun subscribeObservers() {
        viewModel.serverReload.observe(this) {
            if (it) {
                val lastSyncTime =
                    DateFormats.getRelativeTime(viewModel.challengeDetailLastSyncTime)
                        .replace("minutes", "mins").replace("minute", "min")

                binding.lytContentScrolling.tvLastSync.text = SpannableStringBuilder()
                    .append("Updated ")
                    .color(requireContext().getColor(R.color.accent_color)) { append(lastSyncTime) }
                    .append(". Pull down to refresh")

                binding.lytContentScrolling.tvLastSync.visible()
                binding.lytContentScrolling.dividerLastSync.root.visible()
            } else {
                binding.lytContentScrolling.tvLastSync.gone()
                binding.lytContentScrolling.dividerLastSync.root.gone()
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.challengeDetails.observe(this) { challengeModel ->
            challengeModel?.let {
                setData(it)
            }
        }

        viewModel.leftChallenge.observe(this) {
            it.getContent()?.let {
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.CHALLENGEDETAILSPAGE_LEAVECHALLENGE_CLICK,
                    HashMap<String, Any>().apply {
                        this["challengename_leavechallenge"] = viewModel.challengeName
                    })
                navigateUpSafe()
            }
        }

        viewModel.joinedChallenge.observe(this) {
            it.getContent()?.let {
                val count = viewModel.localDataStore.getJoinedChallengeCount()
                val newCount = count + 1
                viewModel.localDataStore.setJoinedChallengeCount(newCount)

                viewModel.getChallengeDetailsByID(true)

                runAnimWithCallback(R.anim.anim_slide_up) {

                    binding.animChallengeJoined.repeatCount = 0
                    binding.animChallengeJoined.setAnimation(R.raw.anim_join_challenge)
                    binding.animChallengeJoined.playAnimation()
                    binding.animChallengeJoined.addAnimatorListener(object :
                        Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            tryCatch {
                                runAnimWithCallback(R.anim.anim_slide_down) {
                                    binding.lytJoinAnim.gone()
                                }
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                    })
                }


            }
        }
    }

    private fun showLeaveDialog() {
        setFragmentResultListener(LEAVE_COMMENT) { key, bundle ->
            var comment = bundle.getString("comment")
            if (comment == null) {
                comment = ""
            }
            viewModel.leaveChallenge(viewModel.challengeId, comment)
        }

        navigate(
            ChallengeDetailsFragmentDirections.actionChallengeDetailsFragmentToChallengeGiveUpDialog(
                viewModel.challengeDetails.value?.title ?: ""
            )
        )
    }


    private fun openAllLeaderboard() {
        viewModel.challengeDetails.value?.let {
            navigate(
                ChallengeDetailsFragmentDirections.actionChallengeDetailsFragmentToChallengeLeaderboardFragment(
                    ChallengeIds(
                        id = it.challenge_id,
                        teamId = 0,
                        challengeType = it.type,
                        unit = viewModel.unit
                    )
                )
            )
        }
    }

    private fun setData(challenge: com.noisefit_commans.data.response.ChallengeModel) {
        with(binding.lytContentScrolling) {
            this.root.visible()
            lytChallengeDetails.tvTotalParticipants.text =
                (challenge.participants ?: 0L).numberFormatter()
            lytChallengeDetails.tvShortDesc.text = challenge.description
            lytLeaderboard.tvTotalParticipants.text =
                (challenge.participants ?: 0L).numberFormatter()
            lytAboutChallenge.tvAboutChallenge.text = HtmlCompat.fromHtml(challenge.detail ?: "", 0)

            lytLeaderboard.btnViewAll.setOnClickListener {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGEDETAILSPAGE_LEADERBOARD_VIEWALL_CLICK,
                    HashMap<String, Any>().apply {
                        this["challengename_leaderboard"] = viewModel.challengeName
                    })
                openAllLeaderboard()
            }

            with(binding.lytContentScrolling.lytChallengeDetails) {

                tvEveryDayGoal.text = challenge.condition

                textGoalHeader.text = "${challenge.type?.capitalizeWords()} Goal"

                tvDistanceValue.text = ApplicationUtils.getNumberAsPerUnit(
                    challenge.type.toString(),
                    challenge.goal ?: 0.0,
                    viewModel.unit
                )
                tvDistanceUnit.text = "${
                    ApplicationUtils.getChallengeTypeUnit(
                        challenge.type.toString(),
                        viewModel.unit
                    )
                } (Everyday)"

                val dateData = challenge.getStartEndData()
                tvDurationStartValue.text = dateData.first.first
                tvDurationStartUnit.text = "${dateData.first.second} - "

                tvDurationEndValue.text = dateData.second.first
                tvDurationEndUnit.text = dateData.second.second

                val startInData = challenge.getStartsInData()
                if (startInData.second.isEmpty()) {
                    if (startInData.first.equals("0")) {

                        tvStartsInUnit.text = ""
                        textView57.text = "Started"
                        tvStartsInValue.text = "Today"

                        /*val dateEndsData = challenge.getEndsInData()
                        if (dateEndsData.second.isEmpty()) {
                            tvStartsInUnit.text = ""
                            textView57.text = "Ends"
                            tvStartsInValue.text = dateEndsData.first
                        } else {
                            tvStartsInUnit.text = ""
                            textView57.text = "Ends in"
                            tvStartsInValue.text = "${dateEndsData.first} ${dateEndsData.second}"
                        }*/
                    } else {
                        tvStartsInUnit.text = ""
                        textView57.text = getString(R.string.text_starts)
                        tvStartsInValue.text = startInData.first
                    }
                } else {
                    tvStartsInUnit.text = startInData.second
                    textView57.text = getString(R.string.text_starts_in)
                    tvStartsInValue.text = startInData.first
                }

            }
        }

        challenge.leaderboard?.let {
            leaderBoardAdapter.setDataSet(
                viewModel.getUserId(),
                it,
                challenge.type ?: "",
                viewModel.unit
            )
        }

        challenge.rewards?.let { rewardsAdapter.setDataSet(it) }
        viewModel.challengeName = challenge.title.toString()
        binding.toolbarLayout.title = challenge.title
        binding.toolbarLayout.subtitle = challenge.subtitle?.trim()
        binding.ivChallengeBanner.loadImage(requireContext(), challenge.image_url)
        handleChallengeStatus(challenge)

    }

    private fun handleChallengeStatus(challenge: com.noisefit_commans.data.response.ChallengeModel) {

        if (challenge.isChallengeJoined()) {
            if (challenge.isDisqualified()) {
                binding.lytContentScrolling.lytDisqualified.root.visible()
                binding.lytContentScrolling.lytDisqualified.tvDisqualified.text =
                    challenge.disqualified_msg
            } else {
                binding.lytContentScrolling.lytDisqualified.root.gone()
            }

            when (challenge.status?.lowercase()) {
                "ongoing", "awaited" -> {
                    if (challenge.status.equals("awaited", true)) {
                        binding.lytContentScrolling.clLeaveChallenge.gone()
                        binding.lytContentScrolling.lytLeaderboard.lytLeadersListing.gone()
                        binding.lytContentScrolling.lytLeaderboard.textView37.gone()
                        binding.lytContentScrolling.lytLeaderboard.btnViewAll.gone()
                        binding.lytContentScrolling.lytDisqualified.apply {
                            imageView29.setImageResource(R.drawable.ic_challenge_completed)
                            tvDisqualified.text = challenge.awaited_msg
                            textView58.text = "Challenge Completed"
                            root.visible()
                        }
                        binding.ivShare.gone()
                        binding.lytContentScrolling.lytShareChallenge.root.gone()
                    } else {
                        if (viewModel.canInviteFriends()) {
                            binding.ivShare.visible()
                            binding.lytContentScrolling.lytShareChallenge.root.visible()
                        } else {
                            binding.ivShare.gone()
                            binding.lytContentScrolling.lytShareChallenge.root.gone()
                        }


                        binding.lytContentScrolling.clLeaveChallenge.visible()

                        if (challenge.isDisqualified()) {
                            binding.lytContentScrolling.lytDisqualified.root.visible()
                            binding.lytContentScrolling.lytDisqualified.tvDisqualified.text =
                                challenge.disqualified_msg

                            binding.lytContentScrolling.lytLeaderboard.textView37.gone()
                            binding.lytContentScrolling.lytLeaderboard.btnViewAll.gone()
                            binding.lytContentScrolling.lytLeaderboard.lytLeadersListing.gone()

                        } else {
                            binding.lytContentScrolling.lytDisqualified.root.gone()
                            binding.lytContentScrolling.lytLeaderboard.textView37.visible()
                            binding.lytContentScrolling.lytLeaderboard.btnViewAll.visible()

                            if ((challenge.leaderboard?.size ?: 0) > 0)
                                binding.lytContentScrolling.lytLeaderboard.lytLeadersListing.visible()
                            else
                                binding.lytContentScrolling.lytLeaderboard.lytLeadersListing.gone()
                        }


                    }
                    binding.lytContentScrolling.lytLeaderboard.root.visible()
                    binding.lytContentScrolling.lytCalendar.visible()
                    binding.lytContentScrolling.dividerCalendar.root.visible()
                    binding.lytContentScrolling.lytProgress.root.visible()
                    binding.lytContentScrolling.lytChallengeDetails.root.gone()


                    challenge.history?.let {
                        challenge.currentTime?.let { it1 ->
                            initCalendar(
                                it,
                                it1,
                                challenge.end_date
                            )
                        }
                        updateProgressData(challenge.currentTime?.let { it1 ->
                            returnCurrentDate(
                                it1,
                                challenge.end_date
                            )
                        })
                    }
                }

                "upcoming" -> {
                    binding.lytContentScrolling.clLeaveChallenge.visible()
                    binding.lytContentScrolling.lytLeaderboard.root.gone()
                    binding.lytContentScrolling.lytCalendar.gone()
                    binding.lytContentScrolling.dividerCalendar.root.gone()
                    binding.lytContentScrolling.lytProgress.root.gone()
                    binding.lytContentScrolling.lytChallengeDetails.root.visible()
                    binding.ivShare.visible()
                    binding.lytContentScrolling.lytShareChallenge.root.visible()
                    binding.ivShare.visible()
                }
            }

            binding.lytJoin.gone()
            binding.lytContentScrolling.lytRewards.root.visible()
            binding.lytContentScrolling.lytAboutChallenge.root.visible()
            binding.lytContentScrolling.vMarginBottom.gone()


            with(binding.lytContentScrolling.lytLeaderboard) {
                val dateData = challenge.getStartEndData()
                tvDurationStartValue.text = dateData.first.first
                tvDurationStartUnit.text = "${dateData.first.second} - "
                tvDurationEndValue.text = dateData.second.first
                tvDurationEndUnit.text = dateData.second.second
            }
        } else {
            binding.lytContentScrolling.lytProgress.root.gone()
            binding.lytContentScrolling.lytChallengeDetails.root.visible()
            binding.lytContentScrolling.lytRewards.root.visible()
            binding.lytContentScrolling.lytAboutChallenge.root.visible()
            binding.lytContentScrolling.lytShareChallenge.root.visible()
            binding.ivShare.visible()
            binding.lytContentScrolling.vMarginBottom.visible()
            binding.lytJoin.visible()
            binding.lytContentScrolling.lytCalendar.gone()
            binding.lytContentScrolling.dividerCalendar.root.gone()

        }
    }

    private fun initCalendar(
        history: List<com.noisefit_commans.data.response.ChallengeHistory>,
        currentDate: String,
        endDate: String?
    ) {
        val selectedPreviousDates = ArrayList<DateTime>()
        val format = DateTimeFormat.forPattern("yyyy-MM-dd")
        history.forEach { his ->
            selectedPreviousDates.add(
                format.parseDateTime(his.date)
            )
        }

        val currentDateNew = returnCurrentDate(currentDate, endDate)
        dayViewAdapter.setDataSet(history, currentDateNew)
        binding.lytContentScrolling.lytCalendar.scrollToPosition(
            viewModel.findCurrentPosition(
                history,
                currentDateNew
            )
        )
    }

    private fun returnCurrentDate(currentTime: String, endDate: String?): DateTime {
        val sdfSource =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        val cDate1 = sdfSource.parse(currentTime)
        val cDate2 = cDate1?.let { SimpleDateFormat("yyyy-MM-dd").format(it) }


        val parseFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
        val currentDate = parseFormat.parseDateTime(cDate2)

        if (!endDate.isNullOrEmpty()) {
            val endDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(sdfSource.parse(endDate))
            val endDateFormattedLocalDate = parseFormat.parseDateTime(endDateFormatted)
            if (currentDate > endDateFormattedLocalDate) {
                return endDateFormattedLocalDate
            }
        }

        return currentDate
    }

    private fun updateProgressData(dates: DateTime?) {
        //need to call api and update value
        viewModel.selectedHistoryDate = dates
        viewModel.challengeDetails.value?.let { challenge ->
            with(binding.lytContentScrolling.lytProgress) {

                val todayMsg: String = if (viewModel.convertCurrentTimeIntoLocalDates() == dates)
                    "(Today)"
                else
                    viewModel.getSelectedDateFormat(dates.toString()).toString()
                textDate.text = todayMsg

                val challengeGoal = challenge.goal ?: 0.0
                val dateData = challenge.getEndsInData()
                var endText = "Ends in ${dateData.first} ${dateData.second}"
                if (dateData.second.isEmpty()) {
                    endText = "Ends ${dateData.first}"
                }

                var endDateNum = dateData.first.toIntOrNull() ?: 0
                if (endDateNum < 0) {//awaited condition
                    endDateNum = Math.abs(endDateNum)
                    endText = "Ended $endDateNum ${if (endDateNum == 1) "day" else "days"} ago"
                }
                tvEndsIn.text = endText
                textView51.text = viewModel.getTotalChallengeTitle()


                tvTotal.text = "/${
                    ApplicationUtils.getNumberAsPerUnit(
                        challenge.type.toString(),
                        challengeGoal,
                        viewModel.unit
                    )
                } ${
                    ApplicationUtils.getChallengeTypeUnit(
                        challenge.type.toString(),
                        viewModel.unit
                    )
                }"

                var progress = 0f
                if (!challenge.history.isNullOrEmpty()) {
                    val date = viewModel.selectedHistoryDate
                    if (date == null) {
                        progress = 0f
                    } else {
                        challenge.history?.forEach {
                            val parseFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

                            val parsedDate = parseFormat.parseDateTime(it.date)

                            if (parsedDate == viewModel.selectedHistoryDate) {
                                progress = it.progress ?: 0f
                            }
                        }
                    }
                }


                val totalProgress = challenge.getTotalProgress()


                if (challenge.type.equals("distance", true)) {
                    tvCompleted.text = progress.toString()
                    tvTotalCovered.text = "$totalProgress ${
                        ApplicationUtils.getChallengeTypeUnit(
                            challenge.type.toString(),
                            viewModel.unit
                        )
                    }"

                } else {
                    tvCompleted.text = "${progress.toInt()}"
                    tvTotalCovered.text = "${
                        totalProgress.toLong()
                            .numberFormatter()
                    } ${
                        ApplicationUtils.getChallengeTypeUnit(
                            challenge.type.toString(),
                            viewModel.unit
                        )
                    }"

                }


                layoutProgress.pbSteps.setIndicatorColor1(R.color.accent_color)

                if (progress > challengeGoal) {
                    progress = 100f
                    // progress = progress.challengeCompletePercentage(challengeGoal.toFloat())
                    //  layoutProgress.pbSteps.setTrackColor1(R.color.challenge_completed_pg)
                    binding.lytContentScrolling.lytProgress.markerContainer.width {
                        myProgressBarThumb(100f)
                    }
                } else {
                    layoutProgress.pbSteps.setTrackColor1(R.color.progress_track_color)
                    progress = progress.calculatePercentage(challengeGoal.toFloat())
                    binding.lytContentScrolling.lytProgress.markerContainer.width {
                        myProgressBarThumb(progress)
                    }
                }

                layoutProgress.pbSteps.progress = progress.toInt()


            }
        }
    }

    private fun isAfterToday(date: DateTime): Boolean {
        return date > DateTime.now()
    }

}