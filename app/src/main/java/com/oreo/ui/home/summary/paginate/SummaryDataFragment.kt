package com.oreo.ui.home.summary.paginate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryDataBinding
import com.noisefit.oreo.BottomNavOption
import com.noisefit.ui.common.bottomSheet.DELETE_REQ_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.oreo.data.model.VideoInfoType
import com.oreo.ui.home.summary.OSummaryHealthOverviewAdapter
import com.oreo.ui.home.summary.OSummaryHealthOverviewClickEnum
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SummaryDataFragment :
    BaseFragment<FragmentSummaryDataBinding>(FragmentSummaryDataBinding::inflate) {

    private val healthOverviewAdapter by lazy {
        OSummaryHealthOverviewAdapter()
    }
    private val viewedCardsAdapter by lazy {
        OSummaryHealthOverviewAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
    }


    private fun setAdapter() {
        binding.contentMain.rvHealthData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthOverviewAdapter
        }

        binding.contentMain.rvViewedCards.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = viewedCardsAdapter
        }

        viewedCardsAdapter.itemClickListener = { type ->
            when (type) {
                is OSummaryHealthOverviewClickEnum.TextRingCareClicked -> {
                    navigate(R.id.ringCareFragment, Bundle().apply {
                        this.putString("title", type.title)
                    })
                }

                is OSummaryHealthOverviewClickEnum.VideoInfoClicked -> {
                    navigate(R.id.ringInfoPlayerFragment, Bundle().apply {
                        this.putString("videoUrl", type.videoUrl)
                    })
                }

                else -> {}
            }

        }

        healthOverviewAdapter.itemClickListener = { type ->
            when (type) {

                is OSummaryHealthOverviewClickEnum.WorkoutAlertWhatisThis -> {

                    navigate(R.id.aboutAutoWorkoutBottomSheet)
                }

                is OSummaryHealthOverviewClickEnum.WorkoutAlertIdentify -> {
                    navigate(R.id.detectWorkoutListFragment)
                }

                is OSummaryHealthOverviewClickEnum.AutoSportsDelete -> {
                    setFragmentResultListener(DELETE_REQ_REQUEST_KEY) { _, bundle ->
                        val allow = bundle.getBoolean("allow")
                        if (allow) {
                            viewModel.markWorkoutSyncedAll()
                            viewModel.removeAutoWorkoutCard()
                        }
                    }
                    navigate(R.id.deleteAllWorkoutBottomSheet, Bundle().apply {
                        this.putString("title", getString(R.string.text_dismiss_activity_title))
                        this.putString(
                            "description",
                            getString(R.string.text_dismiss_activity_desc)
                        )
                        this.putString("acceptText", "")
                        this.putString("declineText", "")
                    })
                }


                OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.ACTIVITY)
                    mainViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_ACTIVITY_CLICK)
                }

                OSummaryHealthOverviewClickEnum.ReadinessDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.READINESS)
                    mainViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_READINESS_CLICK)
                }

                OSummaryHealthOverviewClickEnum.SleepDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.SLEEP)
                    mainViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_SLEEP_CLICK)
                }

//                OSummaryHealthOverviewClickEnum.ActivityInternalDetailsWorkoutClick->{
//                    mSharedViewModel.selectedTab = 0
//                    mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
//                    mSharedViewModel.itemClickType = ViewItemClickType.ACTIVITY_SCORE
//                    navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
//                        putString("viewType", "activity")
//                    })
//                }
                is OSummaryHealthOverviewClickEnum.VideoInfoClicked -> {
                    navigate(R.id.ringInfoPlayerFragment, Bundle().apply {
                        this.putString("videoUrl", type.videoUrl)
                    })
                    viewModel.localDataStore.setDashCardClickState(
                        when (type.type) {
                            VideoInfoType.SLEEP -> DashInfoCard.SLEEP
                            VideoInfoType.READINESS -> DashInfoCard.READINESS
                            VideoInfoType.ACTIVITY -> DashInfoCard.ACTIVITY
                        }, true
                    )
                }

                is OSummaryHealthOverviewClickEnum.TextRingCareClicked -> {
                    viewModel.localDataStore.setDashCardClickState(DashInfoCard.CARE, true)
                    navigate(R.id.ringCareFragment, Bundle().apply {
                        this.putString("title", type.title)
                    })

                }

                OSummaryHealthOverviewClickEnum.TextWelcomeRingClicked -> {
                    viewModel.localDataStore.setDashCardClickState(DashInfoCard.WELCOME, true)
                    navigate(R.id.ringWelcomeFragment)
                }
            }
        }

    }


    override fun initListener() {
        binding.contentMain.lytConnectHelp.btnCancel.setOnClickListener {
            mainViewModel.onRingConnected()
        }

        binding.contentMain.lytConnectHelp.tvDesc.setOnClickListener {
            navigate(R.id.oreoHSQuestionFragment, Bundle().apply {
                putString("title", "Battery & Charging")
                putString("id", "6")
            })
        }

        binding.contentMain.lytChargeRing.root.setOnClickListener {
            navigate(R.id.ringBatteryChargeFragment)
            viewModel.setRingBatteryInfoState()
        }

        binding.contentMain.lytPairDevice.btnPairDevice.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
        }
        binding.contentMain.lytHeartRate.bInfo.setOnClickListener {
            viewModel.getContributorInfo("hr")
        }

        binding.contentMain.lytReadinessAvg.root.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_READINESS_SCORE_CLICK)
            viewModel.getContributorInfo("readiness")
        }

        binding.contentMain.lytSleepAvg.constraintLayout2.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_SLEEP_SCORE_CLICK)
            viewModel.getContributorInfo("sleep")

        }

        binding.contentMain.lytSleepAvg.constraintLayout.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_ACTIVITY_SCORE_CLICK)
            viewModel.getContributorInfo("activity")
        }

    }

    override fun subscribeObservers() {
        viewModel.stateHeaderCard.observe(this) {
            binding.contentMain.lytHeader.apply {
                this.tvDate.text =
                    it.second
                this.tvGreeting.text = it.first
            }
        }


    }
}