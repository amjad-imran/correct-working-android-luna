package com.noisefit.ui.npl.dashboard

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.MainViewModel
import com.noisefit.R
import com.noisefit.data.model.Faces
import com.noisefit.data.model.ScoreCardData
import com.noisefit.data.model.WatchFaces
import com.noisefit.databinding.FragmentNPLDashboardBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.web.WebViewActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.data.response.NplLeague
import com.noisefit_commans.data.response.PrizeInfo
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.numberFormatter
import com.noisefit_commans.ui.playAnimation
import com.noisefit_commans.ui.setIndicatorColor1
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.Presets
import com.noisefit_commans.utils.prettyCountDecimal
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NPLDashboardFragment :
    BaseFragment<FragmentNPLDashboardBinding>(FragmentNPLDashboardBinding::inflate) {
    private val viewModel: NPLDashboardViewModel by viewModels()
    private val collectViewModel: NplCollectSharedViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()


    private val mPWAdapter: NPLWinAdapter by lazy {
        NPLWinAdapter(object : PredictWinnerActions {
            override fun onShareClicked() {
                context?.let {
                    ShareUtil.shareText(
                        it,
                        "Predict cricket matches & win amazing rewards on the Noise Premiere League\n" +
                                "\n" +
                                "Android link: https://play.google.com/store/apps/details?id=com.noisefit \n" +
                                "\n" +
                                "iOS link: https://apps.apple.com/us/app/noisefit-health-fitness/id1498457147"
                    )
                }
            }

            override fun onPredictClicked(matchData: LiveMatch) {
                val teamNamePre: String =
                    if (matchData.userSelectedTeamId == matchData.teamA?.teamId) {
                        matchData.teamA?.getTeamNames() ?: ""
                    } else {
                        matchData.teamB?.getTeamNames() ?: ""
                    }
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.NPL_CONFIRM_TEAM_CLICK,
                    HashMap<String, Any>().apply {
                        this["team_name"] = teamNamePre
                        this["match_id"] = matchData.match_id?.toInt() ?: 0
                        this["match_date"] = DateFormats.formatTimeNpl(matchData.startAt)
                    })
                viewModel.predictWinner(matchData.match_id, matchData.userSelectedTeamId)
            }

            override fun onTeamSelection(matchData: LiveMatch) {
                val teamNamePre: String =
                    if (matchData.userSelectedTeamId == matchData.teamA?.teamId) {
                        matchData.teamA?.getTeamNames() ?: ""
                    } else {
                        matchData.teamB?.getTeamNames() ?: ""
                    }
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.NPL_TEAM_CHECKBOX_CLICK,
                    HashMap<String, Any>().apply {
                        this["team_name"] = teamNamePre
                        this["match_id"] = matchData.match_id?.toInt() ?: 0
                        this["match_date"] = DateFormats.formatTimeNpl(matchData.startAt)
                    })
            }

        })
    }
    private val statesManiaSliderAdapter: NplBannerSliderAdapter by lazy {
        NplBannerSliderAdapter(object : NplBannerAction {
            override fun imageLoadedSuccessfully() {
                tryCatch {
                    nullableBinding?.lytNplStates?.vpImageSlider?.post {
                        nullableBinding?.lytNplStates?.vpImageSlider?.requestLayout()
                        nullableBinding?.lytNplStates?.vpImageSlider?.requestTransform()
                    }
                }

            }
        })
    }
    private val hallOfFameSliderAdapter: NplBannerSliderAdapter by lazy {
        NplBannerSliderAdapter(object : NplBannerAction {
            override fun imageLoadedSuccessfully() {
                tryCatch {
                    nullableBinding?.lytHallOfFame?.vpImageSlider?.post {
                        nullableBinding?.lytHallOfFame?.vpImageSlider?.requestLayout()
                        nullableBinding?.lytHallOfFame?.vpImageSlider?.requestTransform()
                    }
                }

            }
        })
    }
    private val imageTransformer = CompositePageTransformer().apply {
        addTransformer(MarginPageTransformer(40))
        addTransformer { page, position ->
            nullableBinding?.lytNplStates?.vpImageSlider?.let {
                updatePagerHeightForChild(page, it)
            }
        }
    }
    private val imageTransformerHoF = CompositePageTransformer().apply {
        addTransformer(MarginPageTransformer(40))
        addTransformer { page, position ->
            nullableBinding?.lytHallOfFame?.vpImageSlider?.let {
                updatePagerHeightForChild(page, it)
            }
        }
    }
    private val nplWatchFaceAdapter: NplWatchFaceAdapter by lazy {
        NplWatchFaceAdapter(object : NplWatchFaceAdapter.OnWatchFaceClickListener {
            override fun onWatchFaceItemClick(watchFace: Faces) {
                if (viewModel.localDataStore.getConnectedDevice() != null) {
                    navigate(R.id.watchFaceUpdateFragment, Bundle().apply {
                        watchFace.watchfaceId?.let { putInt("watchFaceId", it.toInt()) }
                    })
                } else {
                    Toast.makeText(requireContext(), "Please pair your watch", Toast.LENGTH_SHORT)
                        .show()
                }

            }

        })
    }

    private fun updatePagerHeightForChild(view: View, pager: ViewPager2) {
        view.post {
            val wMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(wMeasureSpec, hMeasureSpec)
            pager.layoutParams = (pager.layoutParams).also { lp -> lp.height = view.measuredHeight }
            pager.invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_PAGE_VISIT)
        if (mainViewModel.playNplAnim) {
            binding.lytToolbar.tvTitle.gone()
            binding.lytToolbar.toolbarAnimView.visible()
            binding.lytToolbar.toolbarAnimView.playAnimation(0, R.raw.anim_npl_toolbar)
            mainViewModel.playNplAnim = false
        } else {
            binding.lytToolbar.tvTitle.visible()
            binding.lytToolbar.toolbarAnimView.gone()
            binding.lytToolbar.tvTitle.text = getString(R.string.text_nplt_title)
        }
        setRecycler()
        setImageSlider()
        setImageSliderHoF()
        viewModel.getScoreCard()
        viewModel.getDashboardMatches()
        /*if (!viewModel.localDataStore.isNplWalkAroundShown()) {
            navigate(NPLDashboardFragmentDirections.actionNplDashboardFragmentToNplWalkAroundBottomDialogFragment())
        }*/

        binding.lytNplStates.root.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // Disallow ScrollView to intercept touch events.
                    binding.contentView.requestDisallowInterceptTouchEvent(true)
                    // Disable touch on transparent view
                    return@setOnTouchListener false
                }

                MotionEvent.ACTION_UP -> {
                    // Allow ScrollView to intercept touch events.
                    binding.contentView.requestDisallowInterceptTouchEvent(false)
                    return@setOnTouchListener true
                }

                else -> return@setOnTouchListener true
            }
        }
        binding.lytHallOfFame.root.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // Disallow ScrollView to intercept touch events.
                    binding.contentView.requestDisallowInterceptTouchEvent(true)
                    // Disable touch on transparent view
                    return@setOnTouchListener false
                }

                MotionEvent.ACTION_UP -> {
                    // Allow ScrollView to intercept touch events.
                    binding.contentView.requestDisallowInterceptTouchEvent(false)
                    return@setOnTouchListener true
                }

                else -> return@setOnTouchListener true
            }
        }
    }


    private fun setWinsViewPager(data: List<LiveMatch>) {

        if (data.isEmpty()) {
            binding.divider333.root.gone()
            binding.lytWinClaims.root.gone()
            return
        } else {
            binding.divider333.root.visible()
            binding.lytWinClaims.root.visible()
        }

        val fragments = ArrayList<NplCollectFragment>()

        data.forEach {
            fragments.add(NplCollectFragment.newInstance(it))
        }

        val winsAdapter =
            NplWinsAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytWinClaims.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = winsAdapter
        }

        TabLayoutMediator(
            binding.lytWinClaims.tabLayout, binding.lytWinClaims.vpImageSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 0) {
            binding.lytWinClaims.tabLayout.visible()
        } else {
            binding.lytWinClaims.tabLayout.invisible()
        }


    }

    private fun showCollectAnimation() {

        binding.vAnimCollect.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {

                binding.vTouchBlock.gone()
                binding.vAnimCollect.gone()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
        binding.vTouchBlock.visible()
        binding.vAnimCollect.visible()
        binding.vAnimCollect.setAnimation(R.raw.anim_coins)
        binding.vAnimCollect.playAnimation()
        binding.vAnimCollect.repeatCount = 0
    }

    private fun setRecycler() {
//        with(binding.lytPredictWinner.rvPredictWin) {
//            adapter = mPWAdapter
//        }
        with(binding.lytNplWatchface.rvWatchfaces) {
            adapter = nplWatchFaceAdapter
        }
    }

    private fun setImageSlider() {
        binding.lytNplStates.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(imageTransformer)
            adapter = statesManiaSliderAdapter
        }
        TabLayoutMediator(
            binding.lytNplStates.tabLayout, binding.lytNplStates.vpImageSlider
        ) { _, _ -> }.attach()

        binding.lytNplStates.vpImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

            }

        })
    }

    private fun setImageSliderHoF() {
        binding.lytHallOfFame.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(imageTransformerHoF)
            adapter = hallOfFameSliderAdapter
        }
        TabLayoutMediator(
            binding.lytHallOfFame.tabLayout, binding.lytHallOfFame.vpImageSlider
        ) { _, _ -> }.attach()

        binding.lytHallOfFame.vpImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

            }

        })
    }


    override fun initListener() {
        binding.lytToolbar.view1.invisible()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_info_npl)
        binding.lytQuiz.lytNplQSData.btnStartQuiz.setOnClickListener {
            mainViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_START_QUIZ_CLICK)
            navigate(R.id.nplQuizFragment)
        }
        binding.lytToolbar.backBtn.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_HOME_BACK_NAVIGATION_CLICK)
            navigateUpSafe()
        }

        binding.tvPredictionHistory.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_PREDICTION_HISTORY_CLICK)
            navigate(R.id.predictionHistoryFragment)
        }
        binding.lytWinClaims.tvClaimYourWin.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_CLAIM_WINS_ENTRY_CLICK)
            navigate(R.id.nplWinsFragment)
        }
        binding.tvRaiseComplain.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_RAISE_COMPLAINT_CLICK)
            navigate(R.id.feedbackFragment)
        }
//        binding.tvHTPlay.setOnClickListener {
//            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_HOWTOPLAY_CLICK)
//            navigate(R.id.howToPlayFragment)
//        }
        binding.tvTC.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_HOME_TERMS_CLICK)
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_TERMS_PAGE_VISIT)
            startActivity(
                WebViewActivity.getStartIntent(
                    requireContext(),
                    getString(R.string.text_terms_and_conditions),
                    AppConstants.URL_NPL_TERMS_OF_USE
                )
            )
        }
        binding.lytToolbar.view1.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_HOME_INFO_ICON_CLICK)
            navigate(R.id.howToPlayFragment)
        }

        binding.lytNplWatchface.tvMore.setOnClickListener {
            navigate(R.id.watchFaceCategoryFragment, Bundle().apply {
                viewModel.categoryId?.let { it1 -> putInt("categoryId", it1) }
                putString("categoryName", viewModel.categoryName)
            })
        }

        binding.lytQuiz.lytNplTargetAchieve.viewCollect.setOnClickListener {
            viewModel.collectQuizReward()
        }
    }

//    private fun setMatchStatus(
//        tvMatchStatus: TextView,
//        textColor: Int,
//        bgColor: Int,
//        status: String
//    ) {
//        tvMatchStatus.visible()
//        tvMatchStatus.setTextColor(requireContext().getColor(textColor))
//        tvMatchStatus.text = status
//        binding.lytOngMatch.lytOngoing.rootView.setBackgroundResource(bgColor)
//    }

//    private fun handleOnGoingData(liveMatch: LiveMatch?) {
//        if (liveMatch == null) {
////            binding.divider2.root.gone()
//            binding.lytOngMatch.root.gone()
//            return
//        }
//
////        binding.divider2.root.visible()
//        binding.lytOngMatch.root.visible()
//
//        binding.lytOngMatch.lytOngoing.apply {
//            tvDate.text = DateFormats.formatTimeNpl(liveMatch.startAt)
//
//            when (liveMatch.match_status?.lowercase()) {
//                "live" -> {
//                    tvMatchStatus.gone()
//                    binding.lytOngMatch.lytOngoing.rootView.setBackgroundResource(R.drawable.back_modal_npl)
//
//                }
//
//                "paused" -> {
//                    setMatchStatus(
//                        tvMatchStatus,
//                        R.color.purple_,
//                        R.drawable.back_modal_npl,
//                        "(Match Paused)"
//                    )
//
//                }
//
//                "abondoned" -> {
//                    setMatchStatus(
//                        tvMatchStatus,
//                        R.color.color_error,
//                        R.drawable.back_modal_new_red,
//                        "(Abondoned)"
//                    )
//                }
//            }
//
//
//
//            if (liveMatch.isSuperOver == true) {
//                setMatchStatus(
//                    tvMatchStatus,
//                    R.color.purple_,
//                    R.drawable.back_modal_npl,
//                    "(Super-over)"
//                )
//            }
//        }
//
//
//        binding.lytOngMatch.lytOngoing.layoutPredictWin.apply {
//            tvTeam1.text = liveMatch.teamA?.getTeamNames()
//            lytTeam1.ivTeam1.loadCircleImage(requireContext(), liveMatch.teamA?.imageUrl)
//            lytTeam2.ivTeam1.loadCircleImage(requireContext(), liveMatch.teamB?.imageUrl)
//            tvTeam2.text = liveMatch.teamB?.getTeamNames()
//            ivPowered.loadImage(requireContext(), liveMatch.sponsorImage)
//            tvPredictValue.text = (liveMatch.predictionCount ?: 0L).numberFormatter()
//
//            when (liveMatch.userTeam) {
//                liveMatch.teamA?.teamId -> {
//                    lytTeam1.ivTop.visible()
//                    lytTeam2.ivTop.gone()
//                    lytTeam1.viewStroke.visible()
//                    lytTeam2.viewStroke.gone()
//                }
//
//                liveMatch.teamB?.teamId -> {
//                    lytTeam2.ivTop.visible()
//                    lytTeam1.ivTop.gone()
//                    lytTeam1.viewStroke.gone()
//                    lytTeam2.viewStroke.visible()
//                }
//
//                else -> {
//                    lytTeam1.ivTop.gone()
//                    lytTeam1.viewStroke.gone()
//                    lytTeam2.viewStroke.gone()
//                    lytTeam2.ivTop.gone()
//                }
//            }
//
//            if (liveMatch.teamA?.score != null) {
//                val run = "${liveMatch.teamA.score.run}/${liveMatch.teamA.score.wickets}"
//                val over = "(${liveMatch.teamA.score.over})"
//                tvScore.text = run
//                tvOver.text = over
//                tvOver.visible()
//                tvScore.visible()
//                team1YetToBat.gone()
//            } else {
//                tvOver.invisible()
//                tvScore.invisible()
//                team1YetToBat.visible()
//            }
//
//            if (liveMatch.teamB?.score != null) {
//                val run = "${liveMatch.teamB.score.run}/${liveMatch.teamB.score.wickets}"
//                val over = "(${liveMatch.teamB.score.over})"
//                tvScore2.text = run
//                tvOver2.text = over
//                tvOver2.visible()
//                tvScore2.visible()
//                team2YetToBat.gone()
//            } else {
//                tvOver2.invisible()
//                tvScore2.invisible()
//                team2YetToBat.visible()
//            }
//
//        }
//
//
//        if (liveMatch.userTeam == null) {
//            binding.lytOngMatch.lytPRDMeter.root.gone()
//            return
//        }
//
//        binding.lytOngMatch.lytPRDMeter.root.visible()
//        binding.lytOngMatch.lytPRDMeter.layoutPRDMeter.apply {
//            tvTeam1.text = liveMatch.teamA?.getTeamNames()
//
//            lytTeam1.ivTeam1.loadCircleImage(requireContext(), liveMatch.teamA?.imageUrl)
//            lytTeam2.ivTeam1.loadCircleImage(requireContext(), liveMatch.teamB?.imageUrl)
//            tvTeam2.text = liveMatch.teamB?.getTeamNames()
//            when (liveMatch.userTeam) {
//                liveMatch.teamA?.teamId -> {
//                    lytTeam1.ivTop.visible()
//                    lytTeam2.ivTop.gone()
//                    lytTeam1.viewStroke.visible()
//                    lytTeam2.viewStroke.gone()
//                    pbSteps.progress = liveMatch.teamA?.meter?.toInt() ?: 0
//                    pbSteps.indicatorDirection =
//                        LinearProgressIndicator.INDICATOR_DIRECTION_LEFT_TO_RIGHT
//                }
//
//                liveMatch.teamB?.teamId -> {
//                    lytTeam2.ivTop.visible()
//                    lytTeam1.ivTop.gone()
//                    lytTeam1.viewStroke.gone()
//                    lytTeam2.viewStroke.visible()
//                    pbSteps.progress = liveMatch.teamB?.meter?.toInt() ?: 0
//                    pbSteps.indicatorDirection =
//                        LinearProgressIndicator.INDICATOR_DIRECTION_RIGHT_TO_LEFT
//                }
//
//                else -> {
//                    lytTeam1.ivTop.gone()
//                    lytTeam1.viewStroke.gone()
//                    lytTeam2.viewStroke.gone()
//                    lytTeam2.ivTop.gone()
//                }
//            }
//
//            val team1Pre = "${liveMatch.teamA?.meter?.prettyCountDecimal()}%"
//            val team2Pre = "${liveMatch.teamB?.meter?.prettyCountDecimal()}%"
//            tvScore.text = team1Pre
//            tvScore2.text = team2Pre
//
//
//        }
//
//
//    }

    override fun subscribeObservers() {

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        collectViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        collectViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        collectViewModel.rewardCollected.observe(this) {
            it.getContent()?.let { id ->
                viewModel.removeCollectedReward(id)
                showCollectAnimation()
            }
        }

//        viewModel.predictWinnerData.observe(this) {
//            if (it.isEmpty()) {
//                binding.lytPredictWinner.root.gone()
//                binding.divider222.root.gone()
//            } else {
//                binding.lytPredictWinner.root.visible()
//                binding.divider222.root.visible()
//                mPWAdapter.setDataSet(it)
//            }
//
//        }
        viewModel.userWinsData.observe(this) {
            setWinsViewPager(it)
        }

//        viewModel.ongoingMatchData.observe(this) {
//            handleOnGoingData(it)
//        }

        viewModel.showNextImage.observe(this) {
            it.getContent()?.let {
                val current = binding.lytNplStates.vpImageSlider.currentItem
                val imageSize = viewModel.scoreCardData.value?.banners?.size ?: 0
                if (imageSize == 0) return@let

                if ((current + 1) == imageSize) {
                    try {
                        binding.lytNplStates.vpImageSlider.setCurrentItem(0, true)
                    } catch (_: IllegalStateException) {
                    }
                } else {
                    try {
                        binding.lytNplStates.vpImageSlider.setCurrentItem(current + 1, true)
                    } catch (_: IllegalStateException) {
                    }
                }
            }
        }
        viewModel.showNextImageHoF.observe(this) {
            it.getContent()?.let {
                val current = binding.lytHallOfFame.vpImageSlider.currentItem
                val imageSize = viewModel.scoreCardData.value?.bannersFame?.size ?: 0
                if (imageSize == 0) return@let

                if ((current + 1) == imageSize) {
                    try {
                        binding.lytHallOfFame.vpImageSlider.setCurrentItem(0, true)
                    } catch (_: IllegalStateException) {
                    }
                } else {
                    try {
                        binding.lytHallOfFame.vpImageSlider.setCurrentItem(current + 1, true)
                    } catch (_: IllegalStateException) {
                    }
                }
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        collectViewModel.getLoading().observe(this) {
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

        viewModel.scoreCardData.observe(this) {
            updateScoreUi(it)
        }

        viewModel.showKonfettiAnim.observe(this) {
            it.getContent()?.let {
                binding.konfettiViewLeft.start(Presets.festiveLeft())
                binding.konfettiViewRight.start(Presets.festiveRight())
            }
        }
        viewModel.nextQuizTimer.observe(this) {
            updateNextQuizTicker(it)
        }

        viewModel.nextQuizTimerFinish.observe(this) {
            if (it) {
                if (viewModel.timerLeft != null) {
                    viewModel.timerLeft?.cancel()
                }
                binding.lytQuiz.lytNplQSData.btnStartQuiz.visible()
                binding.lytQuiz.lytNplQSData.btnElapseTime.gone()
            }
        }

        viewModel.collectQuizReward.observe(this) {
            it.getContent()?.let { isCollect ->
                if (isCollect) {
                    showNoiseMakerBottomsheet()
                } else {
                    navigateUpSafe()
                }
            }
        }

    }

    private fun showNoiseMakerBottomsheet() {
        setFragmentResultListener(NPL_NOISEMAKER_DONE) { key, bundle ->
            val isSelected = bundle.getBoolean("isDone")
            if (isSelected) {
                navigateUpSafe()
            }
        }
        navigate(R.id.bottomSheetNoiseMaker)
    }

    private fun updateNextQuizTicker(it: Long?) {
        binding.lytQuiz.lytNplQSData.btnElapseTime.text = viewModel.parseNextQuizTickerTime(it)
    }


    private fun handlePrizeInfo(prizeInfo: PrizeInfo?) {
        if (prizeInfo == null) {
            return
        }

        val winsRequired = (prizeInfo.rewardWins ?: 0) - (prizeInfo.userWins ?: 0)
        if (winsRequired > (prizeInfo.matchesLeft ?: 0)) {
            return
        }

//        binding.divider3.root.visible()
//        binding.lytXWins.root.visible()
        binding.lytLuckyDraw.root.gone()
        binding.dividerLD.root.gone()


//        binding.lytXWins.imageView39.gone()
        val percentage =
            prizeInfo.userWins?.toFloat()?.calculatePercentage(prizeInfo.rewardWins?.toFloat())

        if (prizeInfo.userWins!! >= prizeInfo.rewardWins!!) {
            val predictAway = buildSpannedString {
                inSpans(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white))
                ) {
                    append("Woohoo! You are now eligible for a lucky draw to win an iPhone 14 Pro")
                }
            }
//            binding.lytXWins.layoutPredictWin.apply {
//                tvTitle.text = predictAway
//                tvMatchesLeft.gone()
//                tvPowered.text = prizeInfo.message
//                pbSteps.progress = percentage?.toInt() ?: 0
//                ivWinPrize.loadImage(requireContext(), prizeInfo.imageUrl)
//            }
            showLuckyDrawWinner()
        } else {
            binding.lytLuckyDraw.root.gone()
            binding.dividerLD.root.gone()
            val predictAway = buildSpannedString {
                inSpans(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), com.noisefit_commans.R.color.purple_))
                ) {
                    append(winsRequired.toString())
                    append(" wins")
                }
                append(" away from ")
                inSpans(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white))
                ) {
                    append(prizeInfo.prizeName.toString())
                }
            }
            val matchLeft = buildSpannedString {
                append("Matches left: ")
                inSpans(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white))
                ) {
                    append(prizeInfo.matchesLeft.toString())
                }
            }
//            binding.lytXWins.layoutPredictWin.apply {
//                tvTitle.text = predictAway
//                tvMatchesLeft.text = matchLeft
//                tvPowered.text = prizeInfo.message
//                pbSteps.progress = percentage?.toInt() ?: 0
//                ivWinPrize.loadImage(requireContext(), prizeInfo.imageUrl)
//            }
//
//            binding.lytXWins.tvTitle.text = prizeInfo.title
        }

    }

    private fun showLuckyDrawWinner() {
        binding.lytLuckyDraw.root.visible()
        binding.dividerLD.root.visible()
    }

    private fun updateScoreUi(scoreData: ScoreCardData?) {
        binding.contentView.visible()
        if (scoreData != null) {
            val score = scoreData.scoreCard
            if (score?.userWins == 0 && score.userLosses == 0) {
                binding.tvScoreTitle.gone()
                binding.dividerHoF.root.gone()
                binding.lytScoreCard.root.gone()
            } else {
                binding.tvScoreTitle.visible()
                binding.dividerHoF.root.visible()
                binding.lytScoreCard.root.visible()

                if ((score?.userWins ?: 0) > 0) {
                    binding.lytScoreCard.tvWinsValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.steps_arc
                        )
                    )
                } else {
                    binding.lytScoreCard.tvWinsValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
                binding.lytScoreCard.tvWinsValue.text = score?.userWins.toString()
                binding.lytScoreCard.tvLoseValue.text = score?.userLosses.toString()
            }


//            binding.lytScoreCard.tvMLeftValue.text = score?.matchesLeft.toString()

            handlePrizeInfo(scoreData.prizeInfo)
            handleQuizView(scoreData)
            handleLeagueData(scoreData.nplLeague)

            handleStatsMania(scoreData)
            handleHallOfFame(scoreData)
            handleWatchFaces(scoreData.watchFace)

        }
    }

    private fun handleWatchFaces(watchFace: WatchFaces?) {
//        val tempData = viewModel.getDummyWatchData()
        if (watchFace != null) {
            binding.lytNplWatchface.root.visible()
            binding.dividerNplWf.root.visible()
            viewModel.categoryId = watchFace.id
            viewModel.categoryName = watchFace.name
            if (watchFace.faces.isNullOrEmpty()) {
                binding.lytNplWatchface.rvWatchfaces.gone()
            } else {
                binding.lytNplWatchface.rvWatchfaces.visible()
                nplWatchFaceAdapter.setDataSet(watchFace.faces)
            }

        } else {
            binding.lytNplWatchface.root.gone()
            binding.dividerNplWf.root.gone()


        }
    }

    private fun handleHallOfFame(scoreData: ScoreCardData) {
        if (scoreData.bannersFame != null) {
            if (scoreData.bannersFame.isNotEmpty()) {
                binding.lytHallOfFame.tvTitle.text = getString(R.string.text_hall_of_fame)
                val banners = scoreData.bannersFame
                hallOfFameSliderAdapter.setDataSet(banners)
                with(nullableBinding) {
                    if (banners.isEmpty()) {
                        //this?.lytImageSlider?.tvMessage?.visible()
                    } else {
                        //this?.lytImageSlider?.tvMessage?.gone()
                        this?.lytHallOfFame?.vpImageSlider?.setCurrentItem(
                            (banners.size - 1), false
                        )

                        this?.lytHallOfFame?.vpImageSlider?.post {
                            nullableBinding?.lytHallOfFame?.vpImageSlider?.requestLayout()
                            nullableBinding?.lytHallOfFame?.vpImageSlider?.requestTransform()
                        }
                        viewModel.startBannerTimerHoF()
                    }
                }
            }
        }
    }

    private fun handleStatsMania(scoreData: ScoreCardData) {
        if (scoreData.banners != null) {
            if (scoreData.banners.isNotEmpty()) {
                binding.lytNplStates.tvTitle.text = getString(R.string.text_stats_mania)
                val banners = scoreData.banners
                statesManiaSliderAdapter.setDataSet(banners)
                with(nullableBinding) {
                    if (banners.isEmpty()) {
                        //this?.lytImageSlider?.tvMessage?.visible()
                    } else {
                        //this?.lytImageSlider?.tvMessage?.gone()
                        this?.lytNplStates?.vpImageSlider?.setCurrentItem(
                            (banners.size - 1), false
                        )

                        this?.lytNplStates?.vpImageSlider?.post {
                            nullableBinding?.lytNplStates?.vpImageSlider?.requestLayout()
                            nullableBinding?.lytNplStates?.vpImageSlider?.requestTransform()
                        }
                        viewModel.startBannerTimer()
                    }
                }
            }
        }
    }

    private fun handleLeagueData(nplData: NplLeague?) {
        if (nplData != null) {
            binding.lytQuiz.lytCorrectAnswer.root.visible()
            binding.lytQuiz.lytCorrectAnswer.tvCorrectAnswer.text = nplData.correctQues.toString()
            val correctAns = "/${nplData.totalQues} correct answers"
            binding.lytQuiz.lytCorrectAnswer.tvCorrectAnswerCount.text = correctAns
            binding.lytQuiz.lytCorrectAnswer.tvQLeftValue.text = nplData.quesLeft.toString()
            binding.lytQuiz.lytCorrectAnswer.tvBRValue.text = nplData.reward.toString()
            binding.lytQuiz.lytCorrectAnswer.pbSteps.progress =
                ApplicationUtils.calculateProgressPercentage(nplData)
            binding.lytQuiz.lytCorrectAnswer.pbSteps.setIndicatorColor1(R.color.accent_color_purple)
            if (nplData.correctQues >= nplData.totalQues) {
                binding.lytQuiz.lytNplTargetAchieve.root.visible()
                binding.lytQuiz.lytCorrectAnswer.root.gone()
                binding.lytQuiz.lytNplQSData.root.gone()
                val correctAnsCount = "${nplData.correctQues}/${nplData.totalQues} correct answers"
                binding.lytQuiz.lytNplTargetAchieve.tvCorrectAnswerCount.text = correctAnsCount
                binding.lytQuiz.lytNplTargetAchieve.pbSteps.progress =
                    ApplicationUtils.calculateProgressPercentage(nplData)
                binding.lytQuiz.lytNplTargetAchieve.pbSteps.setIndicatorColor1(R.color.accent_color_purple)
                binding.lytQuiz.lytNplTargetAchieve.btnCollect.text = "Collect ${nplData.reward}"
            } else {
                binding.lytQuiz.lytNplTargetAchieve.root.gone()
                binding.lytQuiz.lytCorrectAnswer.root.visible()
                binding.lytQuiz.lytNplQSData.root.visible()
            }
        } else {
            binding.lytQuiz.lytCorrectAnswer.root.gone()
        }
    }

    private fun handleQuizView(scoreData: ScoreCardData) {
        if (scoreData.quiz == null) {
            binding.lytQuiz.root.gone()
            binding.divider333.root.gone()
        } else {
            binding.lytQuiz.root.visible()
            binding.divider333.root.visible()
            binding.lytQuiz.lytNplQSData.tvParticipants.text =
                scoreData.quiz.totalParticipants.numberFormatter()
            binding.lytQuiz.lytNplQSData.tvEarnedCoin.text =
                scoreData.quiz.total_coins.numberFormatter()
            if (scoreData.quiz.elapsedTime != null && scoreData.quiz.elapsedTime > 0) {
                viewModel.elapsedTimeAfterSubmitAnswer = scoreData.quiz.elapsedTime
                viewModel.getNextQuizTimerData()
                binding.lytQuiz.lytNplQSData.btnStartQuiz.gone()
                binding.lytQuiz.lytNplQSData.btnElapseTime.visible()
            } else {
                binding.lytQuiz.lytNplQSData.btnElapseTime.gone()
                binding.lytQuiz.lytNplQSData.btnStartQuiz.visible()
            }

            scoreData.nplLeague?.let {
                if (it.sponsorImage.isNullOrEmpty()) {
                    binding.lytQuiz.lytNplQSData.tvPowered.gone()
                    binding.lytQuiz.lytNplQSData.ivPowered.gone()
                    binding.lytQuiz.lytNplQSData.divider2.gone()
                } else {
                    binding.lytQuiz.lytNplQSData.tvPowered.visible()
                    binding.lytQuiz.lytNplQSData.ivPowered.visible()
                    binding.lytQuiz.lytNplQSData.divider2.visible()
                    binding.lytQuiz.lytNplQSData.ivPowered.loadImageWithCache(
                        binding.lytQuiz.lytNplQSData.ivPowered.context,
                        it.sponsorImage
                    )
                }
            }

        }
    }


}