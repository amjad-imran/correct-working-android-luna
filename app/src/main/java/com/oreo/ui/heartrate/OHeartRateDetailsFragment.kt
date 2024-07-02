package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import com.oreo.data.model.ChartModel
import com.oreo.ui.custom.ScrollListener
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class OHeartRateDetailsFragment :
    BaseFragment<FragmentOHeartRateDetailsBinding>(FragmentOHeartRateDetailsBinding::inflate),
    ScrollListener {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: HeartRateViewModel by viewModels()
    private var pagerAdapter: HeartRatePagerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytHeader.view1.visible()
        setViewPager()
    }


    override fun initListener() {
        binding.lytHeader.ivAddFriend.invisible()
        binding.lytHeader.view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)
        binding.lytHeader.tvTitle.text = getString(R.string.text_heart_rate)

        binding.tabLayout.setOnChartScrollChangedListener(this)

        binding.lytHeader.view1.setOnClickListener {
            navigate(R.id.fragmentHrInfo)
        }

        binding.lytHeader.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        /* binding.tabLayout.tvDateLeft.setOnClickListener {
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
         }*/

    }

    fun setTopBar() {

        val it = mainViewModel.dashboard.value
        if (it.isNullOrEmpty()) return

        val topGraphData = viewModel.getPrefixAndSuffixList(it)

        var moveToPos = -1

        if (mainViewModel.selectedDate != null) {
            val index = it?.indexOfFirst { data ->
                data.equals(mainViewModel.selectedDate, true)
            }
            if (index != null) {
                moveToPos = 15 + (it.size - index - 1)
            }
        }

        binding.tabLayout.updateDataWithMax(
            topGraphData.first,
            topGraphData.third,
            topGraphData.second,
            moveToPos
        )
    }


    override fun subscribeObservers() {
        mainViewModel.dashboard.observe(viewLifecycleOwner) {
            LOGS.w("Setting_data size ${it.size}")
            pagerAdapter?.setDataSet(it)

            val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate) ?: (it.size - 1)

            binding.viewPagerHeartRate.setCurrentItem(pos, false)
            binding.tabLayout.visible()
            //setTabDates(pos)

        }
    }

    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {
        if (mainViewModel.selectedDate == chartModel?.date!!) {
            return
        }
        mainViewModel.selectedDate = chartModel.date!!
        mainViewModel.handleAddWorkoutVisibility()

        val returnDate = mainViewModel.updateSelectedDate(mainViewModel.selectedDate)
        if (returnDate != null) {
            mainViewModel.selectedDate = returnDate
        }

        val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate)
        if (pos != null && pos != -1) {
            binding.viewPagerHeartRate.setCurrentItem(pos, false)
        }

        val shouldShow = mainViewModel.shouldShowStressCard(mainViewModel.selectedDate!!)
        if (shouldShow.not()) return

        if (mainViewModel.shouldLoadMoreData()) {
            LOGS.w("Loading more data")
        }
    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

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
                //setTabDates(position)

                if (!binding.tabLayout.isInteracting) {
                    setTopBar()
                }

                if (mainViewModel.shouldLoadMoreData()) {
                    LOGS.w("Loading more data")
                }
            }
        })

    }

    /*private fun setTabDates(position: Int) {
        var currentDayText = ""
        val centerDate = pagerAdapter?.getDate(position)
        if (centerDate.equals(DateFormats.getCurrentDate(DateFormats.dateFormat3()))) {
            currentDayText = "Today, "
        }
        LocalDate.MAX
        binding.tabLayout.tvSelectedDate.text = "$currentDayText${
            if (currentDayText.isEmpty()) {
                DateFormats.getOrdinalDate(
                    centerDate,
                    DateFormats.dateFormat3()
                )
            } else {
                DateFormats.getOrdinalDateToday(
                    centerDate,
                    DateFormats.dateFormat3(),
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
                DateFormats.dateFormat3(),
            )
        }
        val rightDate = pagerAdapter?.getDate(position + 1)
        if (rightDate == null) {
            binding.tabLayout.tvDateRight.gone()
        } else {
            var rightTodayText = ""
            if (rightDate.equals(DateFormats.getCurrentDate(DateFormats.dateFormat3()))) {
                rightTodayText = "Today, "
            }
            binding.tabLayout.tvDateRight.visible()
            binding.tabLayout.tvDateRight.text = "$rightTodayText${
                if (rightTodayText.isEmpty()) {
                    DateFormats.getOrdinalDate(
                        rightDate,
                        DateFormats.dateFormat3(),
                    )
                } else {

                    DateFormats.getOrdinalDateToday(
                        rightDate,
                        DateFormats.dateFormat3(),
                    )
                }
            }"
        }
    }*/

}