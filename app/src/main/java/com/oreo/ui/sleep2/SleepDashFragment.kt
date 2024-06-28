package com.oreo.ui.sleep2

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.moengage.core.internal.utils.getRandomInt
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderSleepDayBinding
import com.noisefit.luna.databinding.FragmentSleepDashBinding
import com.noisefit_commans.common.setTextGradient
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OHMDataModel
import com.oreo.data.model.health.Nudges
import com.oreo.ui.internal.OHMInternalAdapter
import com.oreo.ui.readiness.OreoReadinessBannerFragment
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class SleepDashFragment :
    BaseFragment<FragmentSleepDashBinding>(FragmentSleepDashBinding::inflate) {

    private val viewModel: SleepDashViewModel by viewModels()

    private val mAdapter: OHMInternalAdapter by lazy {
        OHMInternalAdapter(object : OHMInternalAdapter.HMItemClickListener {
            override fun onItemClick(resultData: OHMDataModel, position: Int) {

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalender()
        setRecycler()
        initTempUi()
    }

    private fun setRecycler() {
        with(binding.lytSleepContributor.rvHm) {
            adapter = mAdapter
        }
        mAdapter.setData(getHealthMonitorData())
    }

    fun getHealthMonitorData(): ArrayList<OHMDataModel> {
        val listData = ArrayList<OHMDataModel>()
        listData.add(
            OHMDataModel(
                R.drawable.ic_respiratory_rate,
                "Respiratory rate",
                value = "98.4",
                unit = "rpm",
                rangeValue = "near 11.9-13.8"
            )
        )
        listData.add(OHMDataModel(R.drawable.ic_resting_hr, "Resting heart rate"))
        listData.add(OHMDataModel(R.drawable.ic_blood_oxygen, "Blood oxygen"))
        listData.add(OHMDataModel(R.drawable.ic_hrv, "HRV"))
        listData.add(OHMDataModel(R.drawable.ic_skin_tempreature, "Skin temperature"))
        return listData
    }

    private fun setNudgesView(data: List<Nudges>?) {

        if (data.isNullOrEmpty()) {
            binding.nudgesSleep.gone()
            return
        } else {
            binding.nudgesSleep.visible()
        }

        val fragments = ArrayList<OreoSleepBannerFragment>()
        data.forEach {
            fragments.add(OreoSleepBannerFragment.newInstance(it))
        }

        val sleepBannerAdapter =
            OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)

        binding.nudgesSleep.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(20))
            })
            adapter = sleepBannerAdapter
        }
    }

    private fun initTempUi() {


        setNudgesView(
            arrayListOf(
                Nudges(
                    label = "Sleep data is needed",
                    message = "Wear your luna ring when you go to bed to track your sleep. Make sure to charge your ring to avoid missing out valuable insights."
                ),
                Nudges(
                    label = "Sleep data is needed 2",
                    message = "Wear your luna ring when you go to bed to track your sleep. Make sure to charge your ring to avoid missing out valuable insights."
                )
            )
        )

        binding.lytScore.tvScore.text = "80"
        binding.lytScore.tvScoreStatus.text = "Optimal"
        binding.lytScore.tvScoreStatus.setTextColor(Color.parseColor("#29cc74"))

        binding.lytScore.lytSleepActual.apply {
            tvNoData.gone()
            tvHour.text = "7"
            tvMin.text = "30"

            tvHour.setTextGradient(
                requireActivity().getColor(R.color.white),
                Color.parseColor("#aef8be"),
                Color.parseColor("#2fce77")
            )
            tvMin.setTextGradient(
                requireActivity().getColor(R.color.white),
                Color.parseColor("#aef8be"),
                Color.parseColor("#2fce77")
            )
        }

        binding.lytScore.lytSleepNeeded.apply {
            tvNoData.gone()
            tvHour.text = "8"
            tvMin.text = "30"


        }



        binding.lytScore.circularProgressBar.setProgress(80)

        binding.lytSleepTrends.lytSleepPerformance.graphPerformance.setDataSet(
            arrayListOf(
                20,
                30,
                null,
                50,
                100,
                70,
                null
            ),
            4
        )

        binding.lytSleepTrends.lytHourVsNeed.graphHourVsNeed.setDataSet(
            arrayListOf(
                Pair(60, 100),
                Pair(null, null),
                Pair(90, 100),
                Pair(null, null),
                Pair(120, 130),
                Pair(150, 180),
                Pair(180, 200),
            ),
            4
        )
        binding.lytSleepTrends.lytRestorativeSleep.graphRestorative.setDataSet(
            arrayListOf(
                Pair(60, 40),
                Pair(80, 50),
                Pair(90, 60),
                Pair(100, 40),
                Pair(120, 20),
                Pair(150, 10),
                Pair(180, 0),
            ),
            4
        )
    }

    private fun initCalender() {

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderSleepDayBinding.bind(view)
            lateinit var day: WeekDay
            val dateToday = LocalDate.now()

            init {
                view.setOnClickListener {
                    /*if (viewModel.selectedDate.value != day.date) {
                        viewModel.updateSelectedDate(day.date)
                    }*/
                }
            }

            fun bind(day: WeekDay) {
                this.day = day

                bind.exSevenDateText.text =
                    DateFormats.getDayFromDate(DateFormats.convertLocalDateToDate(day.date))
                bind.exSevenDayText.text =
                    DateFormats.getDayString(DateFormats.convertLocalDateToDate(day.date))

                bind.circularProgressBar.setProgress(getRandomInt(20, 100))

            }
        }

        binding.vCalendar.dayBinder =
            object : WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
            }

        val currentMonth = YearMonth.now()

        binding.vCalendar.setup(
            currentMonth.atStartOfMonth(),
            currentMonth.atEndOfMonth(),
            DayOfWeek.MONDAY,
        )
        binding.vCalendar.scrollToDate(
            LocalDate.now()
        )
    }


    override fun initListener() {
        binding.toolbar.viewBackCalendar.setOnClickListener {
            navigate(R.id.healthMonitorInternal)
        }
        binding.lytScore.ivInfo.setOnClickListener {
            navigate(R.id.sleepPlannerFragment)
        }
    }

    override fun subscribeObservers() {

    }

}