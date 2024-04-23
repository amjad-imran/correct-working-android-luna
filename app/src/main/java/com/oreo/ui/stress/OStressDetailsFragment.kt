package com.oreo.ui.stress

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressDetailsBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.data.enums.StressType
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class OStressDetailsFragment :
    BaseFragment<FragmentOStressDetailsBinding>(FragmentOStressDetailsBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val sharedViewModel: StressDetailSharedViewModel by activityViewModels()
    private var pagerAdapter: StressPagerAdapter? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytHeader.view1.visible()
        sharedViewModel.lastSelectedStressType = StressType.NO_DATA
        setViewPager()
    }


    private fun animateBackFadeInView(type: StressType) {
        val backDrawable: Int = when (type) {
            StressType.CALM -> R.drawable.ic_calm_stress_bg
            StressType.FOCUSED -> R.drawable.ic_focused_stress_bg
            StressType.STRESSED -> R.drawable.ic_high_stress_bg
            StressType.NO_DATA -> 0
        }
        binding.imageBg.setBackgroundResource(backDrawable)
        val animFadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.imageBg.startAnimation(animFadeIn)

    }

    private fun setViewPager() {
        pagerAdapter = StressPagerAdapter(this)
        binding.viewPagerStress.adapter = pagerAdapter
        binding.viewPagerStress.offscreenPageLimit = 1

        binding.viewPagerStress.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mainViewModel.selectedDate = pagerAdapter?.getDate(position)
                setTabDates(position)

                val shouldShow = mainViewModel.shouldShowStressCard(mainViewModel.selectedDate!!)
                if (shouldShow.not()) return

                if (mainViewModel.shouldLoadMoreData()) {
                    LOGS.w("Loading more data")
                }
            }
        })
    }

    private fun setTabDates(position: Int) {
        var currentDayText = ""
        val centerDate = pagerAdapter?.getDate(position)
        if (centerDate.equals(DateFormats.getCurrentDate(DateFormats.dateFormat3))) {
            currentDayText = "Today, "
        }
        LocalDate.MAX
        binding.tabLayout.tvSelectedDate.text = "$currentDayText${
            if (currentDayText.isEmpty()) {
                DateFormats.getOrdinalDate(
                    centerDate,
                    DateFormats.dateFormat3
                )
            } else {
                DateFormats.getOrdinalDateToday(
                    centerDate,
                    DateFormats.dateFormat3,
                )
            }
        }"
        val leftDate = pagerAdapter?.getDate(position - 1)
        if (leftDate == null) {
            binding.tabLayout.tvDateLeft.gone()
        } else {
            binding.tabLayout.tvDateLeft.visible()
            binding.tabLayout.tvDateLeft.text = DateFormats.getOrdinalDate(
                leftDate,
                DateFormats.dateFormat3,
            )
        }
        val rightDate = pagerAdapter?.getDate(position + 1)
        if (rightDate == null) {
            binding.tabLayout.tvDateRight.gone()
        } else {
            var rightTodayText = ""
            if (rightDate.equals(DateFormats.getCurrentDate(DateFormats.dateFormat3))) {
                rightTodayText = "Today, "
            }
            binding.tabLayout.tvDateRight.visible()
            binding.tabLayout.tvDateRight.text = "$rightTodayText${
                if (rightTodayText.isEmpty()) {
                    DateFormats.getOrdinalDate(
                        rightDate,
                        DateFormats.dateFormat3,
                    )
                } else {

                    DateFormats.getOrdinalDateToday(
                        rightDate,
                        DateFormats.dateFormat3,
                    )
                }
            }"
        }
    }

    override fun initListener() {
        binding.lytHeader.ivAddFriend.invisible()
        binding.lytHeader.view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)
        binding.lytHeader.tvTitle.text = getString(R.string.text_stress)

        binding.lytHeader.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytHeader.view1.setOnClickListener {
            navigate(R.id.stressUnderstandingFragment)
        }

        binding.tabLayout.tvDateLeft.setOnClickListener {
            val currentItem = binding.viewPagerStress.currentItem
            if (currentItem == 0) return@setOnClickListener
            binding.viewPagerStress.setCurrentItem((currentItem - 1), true)

        }
        binding.tabLayout.tvDateRight.setOnClickListener {
            if (pagerAdapter == null) return@setOnClickListener
            val currentItem = binding.viewPagerStress.currentItem
            if (currentItem == (pagerAdapter!!.itemCount - 1)) {
                return@setOnClickListener
            }
            binding.viewPagerStress.setCurrentItem((currentItem + 1), true)
        }

    }

    override fun subscribeObservers() {
        mainViewModel.dashboard.observe(viewLifecycleOwner) {
            LOGS.w("Setting_data size ${it.size}")

            val filteredDates = it.filter {
                mainViewModel.shouldShowStressCard(it)
            }

            pagerAdapter?.setDataSet(filteredDates)

            val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate) ?: (filteredDates.size - 1)

            binding.viewPagerStress.setCurrentItem(pos, false)
            binding.tabLayout.root.visible()
            setTabDates(pos)

        }

        sharedViewModel.selectedStressLevel.observe(this) {
            animateBackFadeInView(it)
            sharedViewModel.lastSelectedStressType = it
        }
    }

}