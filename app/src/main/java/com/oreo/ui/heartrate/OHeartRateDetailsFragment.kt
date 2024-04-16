package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHeartRateDetailsBinding
import com.noisefit.oreo.OreoMainViewModel
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
class OHeartRateDetailsFragment :
    BaseFragment<FragmentOHeartRateDetailsBinding>(FragmentOHeartRateDetailsBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private var pagerAdapter: HeartRatePagerAdapter? = null
    override fun initListener() {
        binding.lytHeader.ivAddFriend.invisible()
        binding.lytHeader.view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)
        binding.lytHeader.tvTitle.text = getString(R.string.text_heart_rate)

        binding.lytHeader.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tabLayout.tvDateLeft.setOnClickListener {
            val currentItem = binding.viewPagerHeartRate.currentItem
            if (currentItem == 0) return@setOnClickListener
            binding.viewPagerHeartRate.setCurrentItem((currentItem - 1), true)

        }

        binding.tabLayout.tvDateRight.setOnClickListener {
            if (pagerAdapter == null) return@setOnClickListener
            val currentItem = binding.viewPagerHeartRate.currentItem
            if (currentItem == (pagerAdapter!!.itemCount - 1)) {
                return@setOnClickListener
            }
            binding.viewPagerHeartRate.setCurrentItem((currentItem + 1), true)
        }

    }

    override fun subscribeObservers() {
        mainViewModel.dashboard.observe(viewLifecycleOwner) {
            LOGS.w("Setting_data size ${it.size}")
            pagerAdapter?.setDataSet(it)

            val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate) ?: (it.size - 1)

            binding.viewPagerHeartRate.setCurrentItem(pos, false)
            binding.tabLayout.root.visible()
            setTabDates(pos)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytHeader.view1.visible()
        setViewPager()
    }

    private fun setViewPager() {
        pagerAdapter = HeartRatePagerAdapter(this)
        binding.viewPagerHeartRate.adapter = pagerAdapter
        binding.viewPagerHeartRate.offscreenPageLimit = 1
        binding.viewPagerHeartRate.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mainViewModel.selectedDate = pagerAdapter?.getDate(position)
                setTabDates(position)

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

}