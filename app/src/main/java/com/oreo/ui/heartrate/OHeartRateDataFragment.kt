package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHeartRateDataBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.data.model.HrAlert
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.IrregularEventsChipsListModel
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.ui.custom.Item
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class OHeartRateDataFragment :
    BaseFragment<FragmentOHeartRateDataBinding>(FragmentOHeartRateDataBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: OHeartRateDataViewModel by viewModels()
    private val ARGS_DATE = "ARGS_DATE"
    private val TAG = "HeartRateDataFragment"

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private val learnMoreAdapter: OHRLearnMoreAdapter by lazy {
        OHRLearnMoreAdapter(object : OnItemClickListener {
            override fun onItemClick(item: LearnMoreDataModel) {
                when (item.type) {
                    1 -> {
                        mainViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.article_clicked,
                            HashMap<String, Any>().apply {
                                this["source"] = getString(R.string.text_heart_rate)
                                this["article_name"] =
                                    getString(R.string.text_general_heart_rate_terms)
                            }
                        )
                        navigate(R.id.hrArticle1Fragment)
                    }

                    2 -> {
                        mainViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.article_clicked,
                            HashMap<String, Any>().apply {
                                this["source"] = getString(R.string.text_heart_rate)
                                this["article_name"] =
                                    getString(R.string.text_normal_heart_rate_for_my_age)
                            }
                        )
                        navigate(R.id.hrArticle2Fragment)
                    }

                    3 -> {
                        mainViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.article_clicked,
                            HashMap<String, Any>().apply {
                                this["source"] = getString(R.string.text_heart_rate)
                                this["article_name"] =
                                    getString(R.string.text_what_are_heart_rate_zones)
                            }
                        )
                        navigate(R.id.hrArticle3Fragment)
                    }

                    4 -> {
                        mainViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.article_clicked,
                            HashMap<String, Any>().apply {
                                this["source"] = getString(R.string.text_heart_rate)
                                this["article_name"] =
                                    getString(R.string.text_heart_rate_during_sleep)
                            }
                        )
                        navigate(R.id.hrArticle4Fragment)
                    }
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        viewModel.date?.let {
            mainViewModel.getDashBoardData(it)?.let { dash ->
                viewModel.summaryHealthData = dash.first
                viewModel.prepareActivityData(dash.first)
                setUi(it)
                viewModel.loadAlertsData()
            }
        }
    }

    private fun setUi(date: String) {

        if (viewModel.summaryHealthData?.date == DateFormats.getCurrentDate(DateFormats.dateFormat3()))
            viewModel.getTodayHeartRate()
        else
            viewModel.summaryHealthData?.let { viewModel.parseHealthData(it) }
    }

    companion object {
        @JvmStatic
        fun newInstance(date: String) = OHeartRateDataFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_DATE, date)
            }
        }
    }

    override fun initListener() {

        binding.lytHeartRate.candleChart.setClickListener(object : OnHRClickAction {

            override fun onValueSelected(
                item: Item?,
                position: Int,
                time: String?
            ) {
                if (item != null) {
                    if (item.value != 0) {
                        binding.lytHeartRate.lytSubtitleValue1.tvValue.text = item.value.toString()
                        binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                        binding.lytHeartRate.lytSubtitleValue1.tvUnit.text =
                            getString(R.string.text_bpm_small)
                        binding.lytHeartRate.tvSubtitle1.text = time
                    } else {
                        binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                        binding.lytHeartRate.lytSubtitleValue1.tvUnit.text =
                            getString(R.string.text_bpm_small)
                        binding.lytHeartRate.tvSubtitle1.text = ""
                    }
                    if (item.maxValue != 0 && item.minValue != 0) {
                        binding.lytHeartRate.tvSubtitle2.visible()
                        binding.lytHeartRate.tvSubtitle2.text =
                            getString(R.string.text_range_value_bpm, item.minValue, item.maxValue)
                    } else {
                        binding.lytHeartRate.tvSubtitle2.gone()
                    }
                }
            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                if (!onGoing) {
                    binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_average_hr)
                    binding.lytHeartRate.tvSubtitle2.visible()
                    viewModel.heartRateData.value?.let { updateUI(it) }
                }

            }

            override fun onTopClicked() {
                if (viewModel.activityData?.isNotEmpty() == true)
                    viewModel.activityData?.toTypedArray()?.let { it1 ->
                        navigate(
                            OHeartRateDetailsFragmentDirections.actionNavigationHrDetailsFragToDayTimeActivitiesBottomSheet(
                                it1
                            )
                        )
                    }

            }
        })



        binding.lytLearnMore.vRecycler.addOnItemTouchListener(object :
            RecyclerView.OnItemTouchListener {

            override fun onTouchEvent(view: RecyclerView, event: MotionEvent) {}

            override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        binding.lytLearnMore.vRecycler.parent?.requestDisallowInterceptTouchEvent(
                            true
                        )
                    }
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    override fun subscribeObservers() {
        viewModel.heartRateData.observe(viewLifecycleOwner) {
            if (it != null) {
                updateUI(it)
                initHeartRateGraph(viewModel.summaryHealthData, it)
            }
        }

        viewModel.hrAlertsData.observe(this){
            it.getContent()?.let {
                val date = viewModel.date
                if(date==null) return@observe

                if (date.equals(viewModel.getTodayDate(), true)) {
                    val alerts = viewModel.getIrregularityEventsAlerts()
                    if(alerts!=null && alerts.data.isEmpty().not()){
                        binding.divider1.root.visible()
                        binding.lytIrregularityEvents.root.visible()
                        setIrregularityEventBannerViewPager(
                            alerts.data
                        )
                    }else{
                        if(viewModel.isEventSubmitted){
                            binding.divider1.root.visible()
                            binding.lytIrregularityEvents.root.visible()
                            binding.lytIrregularityEvents.lytIrregularityEventsSubmittedCard.root.visible()
                            binding.lytIrregularityEvents.tabLayout.gone()
                            binding.lytIrregularityEvents.vpBannerSlider.gone()
                        }else{
                            binding.divider1.root.gone()
                            binding.lytIrregularityEvents.root.gone()
                        }
                        /*binding.divider1.root.gone()
                        binding.lytIrregularityEvents.root.gone()*/
                    }
                } else {
                    binding.divider1.root.gone()
                    binding.lytIrregularityEvents.root.gone()
                }
            }
        }
    }

    private fun updateUI(it: OHealthOverview.HeartRateDataModel) {
        if (it.average.toInt() != 0) {
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = it.average.toInt().toString()
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = getString(R.string.text_bpm_small)
            binding.lytHeartRate.tvSubtitle2.text =
                getString(R.string.text_range_value_bpm, it.minValues, it.maxValues)
        } else {
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = getString(R.string.text_bpm_small)
        }
    }

    private fun initHeartRateGraph(
        dayData: ServerUserHealthData?,
        heartRate: OHealthOverview.HeartRateDataModel
    ) {
        binding.lytHeartRate.candleChart.enableInteractiveMode(true)
        binding.lytHeartRate.candleChart.setVibrationUtil(vibrationUtils)
        binding.lytHeartRate.candleChart.updateData(
            viewModel.hrDataConvertor.getHrCombinedData(
                dayData, heartRate
            ), 5, heartRate.minValues, heartRate.maxValues
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = arguments?.getString(ARGS_DATE)
        viewModel.date = date
//        setHRBannerViewPager()

        setRecycler()
    }

    private fun setRecycler() {
        with(binding.lytLearnMore.vRecycler) {
            isNestedScrollingEnabled = false
            adapter = learnMoreAdapter
        }

        learnMoreAdapter.setData(viewModel.getLearnMoreData())
    }

    private fun setIrregularityEventBannerViewPager(data: List<HrAlert>) {

        val parsedData = viewModel.convertAlertsModel(data)

        binding.lytIrregularityEvents.tvTitle.text = getString(R.string.text_irregularity_events)
        val fragments = ArrayList<IrregularityEventsOHeartRateDataFragment>()
/*        if(fragments.isEmpty()){
            binding.lytIrregularityEvents.lytIrregularityEventsSubmittedCard.root.visible()
            binding.lytIrregularityEvents.tabLayout.gone()
            binding.lytIrregularityEvents.vpBannerSlider.gone()
            return
        }*/
        parsedData?.forEach {
            fragments.add(IrregularityEventsOHeartRateDataFragment.newInstance(it).apply {
                setClickListener(object : IrregularityEventsBannerListener {

                    override fun onSubmitBtnClicked(
                        data: IrregularEventsChipsListModel,
                        selectedChips: List<String>,
                        other: String?
                    ) {
                        viewModel.onSubmitButtonClickedIrregularityEvents(
                            data, selectedChips, other
                        ) {
                            viewModel.isEventSubmitted = true
                            viewModel.removeAlert(data)
                            viewModel.loadAlertsData()
                        }
                    }

                    override fun onCrossClicked(data: IrregularEventsChipsListModel) {
                        viewModel.removeAlert(data)
                        viewModel.loadAlertsData()
                    }
                })
            })
        }

        val winsAdapter = IrregularityEventsBannerAdapter(
            childFragmentManager, lifecycle, fragments
        )

        binding.lytIrregularityEvents.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3

            // Get the RecyclerView and configure nested scrolling
            (getChildAt(0) as? RecyclerView)?.let { recyclerView ->
                recyclerView.apply {
                    isNestedScrollingEnabled = true

                    overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                    addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                        private var initialY = 0f

                        override fun onInterceptTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent
                        ): Boolean {
                            when (e.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    initialY = e.y

                                    parent.requestDisallowInterceptTouchEvent(false)
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    val dy = e.y - initialY
                                    if (abs(dy) > 10) {

                                        parent.requestDisallowInterceptTouchEvent(false)
                                    } else {

                                        parent.requestDisallowInterceptTouchEvent(true)
                                    }
                                }
                            }
                            return false
                        }

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                    })
                }
            }

            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })

            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = winsAdapter
        }

        TabLayoutMediator(
            binding.lytIrregularityEvents.tabLayout,
            binding.lytIrregularityEvents.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 1) {
            binding.lytIrregularityEvents.tabLayout.visible()
        } else {
            binding.lytIrregularityEvents.tabLayout.invisible()
        }
    }

}