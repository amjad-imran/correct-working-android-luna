package com.oreo.ui.sleep

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOsleepParentDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.OSleepScoreDetailsFragment
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import com.oreo.util.setSafeOnClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OSleepDetailsParentFragment :
    BaseFragment<FragmentOsleepParentDetailsBinding>(FragmentOsleepParentDetailsBinding::inflate) {
    private val mViewModel: SharedOSCDViewModel by activityViewModels()
    private val args: OSleepDetailsParentFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewPager()
    }

    private fun setViewPager() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.text_day)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.text_week)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.text_month)))
        binding.lytToolbar.tvTitle.text = getPageTitle()
        loadFragment(
            OSleepScoreDetailsFragment.newInstance(
                "Day",
                mViewModel.itemType,
                args.viewType,
                args.date
            )
        )
        mViewModel.selectedTab = 0
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {

                        sendEvent("day", mViewModel.itemClickType)
                        mViewModel.selectedTab = 0
                        var clickedType = ""
                        when (mViewModel.itemType) {
                            ClickViewType.SLEEP.name -> {
                                clickedType = ClickViewType.SLEEP.name
                            }

                            ClickViewType.ACTIVITY.name -> {
                                clickedType = ClickViewType.SLEEP.name
                            }

                            ClickViewType.READINESS.name -> {
                                clickedType = ClickViewType.READINESS.name
                            }

                            ClickViewType.ACTIVITY_SCORE.name -> {
                                clickedType = ClickViewType.ACTIVITY_SCORE.name
                            }

                            ClickViewType.GOAL_PROGRESS_WEEK.name,
                            ClickViewType.GOAL_PROGRESS_MONTH.name -> {
                                clickedType = ClickViewType.GOAL_PROGRESS_DAY.name
                            }

                            ClickViewType.TOTAL_BURN_WEEK.name, ClickViewType.TOTAL_BURN_MONTH.name -> {
                                clickedType = ClickViewType.TOTAL_BURN_DAY.name
                            }
                        }
                        mViewModel.itemType = clickedType
                        loadFragment(
                            OSleepScoreDetailsFragment.newInstance(
                                "Day",
                                clickedType,
                                args.viewType,
                                args.date
                            )
                        )


                    }

                    1 -> {
                        sendEvent("week", mViewModel.itemClickType)

                        mViewModel.selectedTab = 1
                        var clickedViewType = ""
                        when (mViewModel.itemType) {
                            ClickViewType.SLEEP.name -> {
                                clickedViewType = ClickViewType.SLEEP.name
                            }

                            ClickViewType.ACTIVITY.name -> {
                                clickedViewType = ClickViewType.SLEEP.name
                            }

                            ClickViewType.READINESS.name -> {
                                clickedViewType = ClickViewType.READINESS.name
                            }

                            ClickViewType.ACTIVITY_SCORE.name -> {
                                clickedViewType = ClickViewType.ACTIVITY_SCORE.name
                            }

                            ClickViewType.GOAL_PROGRESS_DAY.name,
                            ClickViewType.GOAL_PROGRESS_MONTH.name -> {
                                clickedViewType = ClickViewType.GOAL_PROGRESS_WEEK.name
                            }

                            ClickViewType.TOTAL_BURN_DAY.name, ClickViewType.TOTAL_BURN_MONTH.name -> {
                                clickedViewType = ClickViewType.TOTAL_BURN_WEEK.name
                            }
                        }
                        mViewModel.itemType = clickedViewType
                        loadFragment(
                            OSleepScoreDetailsFragment.newInstance(
                                "Week",
                                clickedViewType,
                                args.viewType,
                                args.date
                            )
                        )

                    }

                    else -> {
                        sendEvent("month", mViewModel.itemClickType)

                        mViewModel.selectedTab = 2
                        var clickedType = ""
                        when (mViewModel.itemType) {
                            ClickViewType.SLEEP.name -> {
                                clickedType = ClickViewType.SLEEP.name
                            }

                            ClickViewType.ACTIVITY.name -> {
                                clickedType = ClickViewType.READINESS.name
                            }

                            ClickViewType.READINESS.name -> {
                                clickedType = ClickViewType.READINESS.name
                            }

                            ClickViewType.ACTIVITY_SCORE.name -> {
                                clickedType = ClickViewType.ACTIVITY_SCORE.name
                            }

                            ClickViewType.GOAL_PROGRESS_DAY.name,
                            ClickViewType.GOAL_PROGRESS_WEEK.name -> {
                                clickedType = ClickViewType.GOAL_PROGRESS_MONTH.name
                            }

                            ClickViewType.TOTAL_BURN_DAY.name,
                            ClickViewType.TOTAL_BURN_WEEK.name -> {
                                clickedType = ClickViewType.TOTAL_BURN_MONTH.name
                            }
                        }
                        mViewModel.itemType = clickedType
                        loadFragment(
                            OSleepScoreDetailsFragment.newInstance(
                                "Month",
                                clickedType, args.viewType, args.date
                            )
                        )


                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private fun sendEvent(interval: String, itemClickType: ViewItemClickType?) {

        var source: String? = null
        source = when (itemClickType) {
            ViewItemClickType.READINESS_SCORE -> "readiness"
            ViewItemClickType.RESTING_HR -> "resting_heart_rate"
            ViewItemClickType.HR_VARIABILITY -> "hrv"
            ViewItemClickType.BODY_TEMPERATURE -> "skin_temperature"
            ViewItemClickType.RESPIRATORY_RATE -> "respiratory_rate"
            else -> null
        }

        if (source != null) {
            uiController.logAppEvent(
                MoEngageLunaAppEvents.score_interval_change,
                hashMapOf(
                    "source" to source,
                    "interval" to interval
                )
            )
        }
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_sleep_score_lower)
        binding.lytToolbar.backBtn.setOnClickListener {
            mViewModel.selectedTab = 0
            mViewModel.itemType = ""
            navigateUpSafe()
        }
        binding.lytToolbar.view1.setSafeOnClickListener {

            if(mViewModel.itemClickType == ViewItemClickType.READINESS_SCORE){
                uiController.logAppEvent(
                    MoEngageLunaAppEvents.info_clicked,
                    hashMapOf("source" to "readiness","section" to "readiness_score")
                )
            }

            //mViewModel.sessionManager.logMoEngageAppEvent("${mViewModel.itemClickType}_" + MoEngageLunaAppEvents.info_click)

            args.infoData?.let { data ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", data)
                })
            }

        }
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)

    }

    override fun subscribeObservers() {

    }

    fun loadFragment(fragment: Fragment) {
        val fm: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.flFragment, fragment)
        fragmentTransaction.commit()

    }

    private fun getPageTitle(): String {
        var trendTitle = ""
        when (mViewModel.itemClickType) {
            ViewItemClickType.SLEEP_SCORE -> {
                trendTitle = "Sleep score"
            }

            ViewItemClickType.TOTAL_SLEEP -> {
                trendTitle = "Total sleep"
            }

            ViewItemClickType.SLEEP_EFFICIENCY -> {
                trendTitle = "Sleep efficiency"
            }

            ViewItemClickType.TIME_IN_BED -> {
                trendTitle = "Time in bed"
            }

            ViewItemClickType.RESTING_HR -> {
                trendTitle = "Average HR"
            }

            ViewItemClickType.READINESS_SCORE -> {
                trendTitle = getString(R.string.text_readiness_score)
            }

            ViewItemClickType.HR_VARIABILITY -> {
                trendTitle = "Hr variability"
            }

            ViewItemClickType.BODY_TEMPERATURE -> {
                trendTitle = "Body temperature"
            }

            ViewItemClickType.RESPIRATORY_RATE -> {
                trendTitle = "Respiratory rate"
            }

            ViewItemClickType.ACTIVITY_SCORE -> {
                trendTitle = getString(R.string.text_activity_score)
            }

            ViewItemClickType.ACTIVE_CALORIES -> {
                trendTitle = getString(R.string.text_goal_progress)
            }

            ViewItemClickType.TOTAL_CALORIES_BURNED -> {
                trendTitle = getString(R.string.text_total_calories)
            }

            ViewItemClickType.STEPS -> {
                trendTitle = getString(R.string.text_steps)
            }

            ViewItemClickType.DISTANCE -> {
                trendTitle = getString(R.string.text_distance)
            }

            ViewItemClickType.AVG_TEMP -> {
                trendTitle = "Body temperature"
            }

            null -> {}
        }
        return trendTitle
    }


}