package com.oreo.ui.stress

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressInternalDetailsBinding
import com.noisefit.luna.databinding.OreoLayoutTopHourMn20Binding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModelStress
import com.oreo.data.model.StressResultData
import com.oreo.ui.custom.ScrollListenerStress
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DAY_TYPE = "DAY_TYPE"
private const val FILTER_TYPE = "FILTER_TYPE"
private const val DATE = "DATE"

@AndroidEntryPoint
class OStressInternalDetailsFragment :
    BaseFragment<FragmentOStressInternalDetailsBinding>(FragmentOStressInternalDetailsBinding::inflate),
    ScrollListenerStress {
    private val mViewModel: OSIDViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            mViewModel.dayType = it.getString(DAY_TYPE).toString()
            mViewModel.filterType = it.getString(FILTER_TYPE).toString()
            mViewModel.selectedDate = it.getString(DATE)
        }
        mViewModel.getInternalDetailsData()
    }

    companion object {
        fun newInstance(dayType: String, date: String, filterType: String) =
            OStressInternalDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(DAY_TYPE, dayType)
                    putString(FILTER_TYPE, filterType)
                    putString(DATE, date)
                }
            }
    }


    override fun initListener() {
        binding.stressChart.setOnChartScrollChangedListener(this)

    }

    private fun updateUI(stressData: List<StressResultData>) {

        //for stressed
        binding.lytTopView.lytStressed.tvTitle.text = getString(R.string.text_stressed)
        binding.lytTopView.lytStressed.tvTitle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.stress_nap_stressed
            )
        )

        binding.lytTopView.lytFocussed.tvTitle.text = getString(R.string.text_focussed)
        binding.lytTopView.lytFocussed.tvTitle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.stress_nap_focussed
            )
        )
        binding.lytTopView.lytCalm.tvTitle.text = getString(R.string.text_relaxed)
        binding.lytTopView.lytCalm.tvTitle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.stress_nap_calm
            )
        )



        setStressGraph(stressData)

        val todayData = stressData.firstOrNull()
        mViewModel.selectedData.postValue(todayData)

    }

    private fun setStressGraph(stressData: List<StressResultData>) {


        val topGraphData = mViewModel.getPrefixAndSuffixList(
            stressData,
            mViewModel.dayType
        )
        binding.stressChart.visible()
        val hCount = if (mViewModel.dayType?.lowercase() == "day")
            12
        else if (mViewModel.dayType?.lowercase() == "month")
            7
        else {
            10
        }
        binding.stressChart.setHCount(hCount)

        binding.stressChart.updateData(
            topGraphData.first,
            topGraphData.third,
            topGraphData.second
        )
    }

    private fun handleProgressStatus(strData: StressResultData?) {

        val stressDays = mainViewModel.stressDaysFromCurrent(strData?.date)

        binding.lytTopView.lytCalm.apply {

            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                strData?.data?.calm?.duration ?: 0
            )
            lytUnit.tvHour.text = "$hourCalm"
            lytUnit.tvHourUnit.text = getString(R.string.text_hr)
            lytUnit.tvMinute.text = "$minuteCalm"
            lytUnit.tvMinuteUnit.text = getString(R.string.text_mins)

            if (strData?.data?.calm?.typicalDay != null && stressDays > 7) {
                val diffCalm = mViewModel.getDifference(
                    strData.data.calm.duration ?: 0,
                    strData.data.calm.typicalDay ?: 0
                )
                tvDifference.text = "${abs(diffCalm)}%"
                if (diffCalm > 0) {
                    icTrend.visible()
                    icTrend.rotation = 0f
                } else if (diffCalm < 0) {
                    icTrend.visible()
                    icTrend.rotation = 180f
                } else {
                    icTrend.gone()
                    tvDifference.text = "No change"
                }
                lytDifference.visible()
            } else {
                lytDifference.gone()
            }


        }

        binding.lytTopView.lytFocussed.apply {

            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                strData?.data?.focused?.duration ?: 0
            )
            lytUnit.tvHour.text = "$hourCalm"
            lytUnit.tvHourUnit.text = getString(R.string.text_hr)
            lytUnit.tvMinute.text = "$minuteCalm"
            lytUnit.tvMinuteUnit.text = getString(R.string.text_mins)

            if (strData?.data?.focused?.typicalDay != null && stressDays > 7) {
                val diffFocussed = mViewModel.getDifference(
                    strData?.data?.focused?.duration ?: 0,
                    strData?.data?.focused?.typicalDay ?: 0
                )
                tvDifference.text = "${abs(diffFocussed)}%"
                if (diffFocussed > 0) {
                    icTrend.visible()
                    icTrend.rotation = 0f
                } else if (diffFocussed < 0) {
                    icTrend.visible()
                    icTrend.rotation = 180f
                } else {
                    icTrend.gone()
                    tvDifference.text = "No change"
                }
                lytDifference.visible()
            } else {
                lytDifference.gone()
            }


        }

        binding.lytTopView.lytStressed.apply {

            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                strData?.data?.stressed?.duration ?: 0
            )
            lytUnit.tvHour.text = "$hourCalm"
            lytUnit.tvHourUnit.text = getString(R.string.text_hr)
            lytUnit.tvMinute.text = "$minuteCalm"
            lytUnit.tvMinuteUnit.text = getString(R.string.text_mins)


            if (strData?.data?.stressed?.typicalDay != null && stressDays > 7) {
                val diffStressed = mViewModel.getDifference(
                    strData?.data?.stressed?.duration ?: 0,
                    strData?.data?.stressed?.typicalDay ?: 0
                )
                tvDifference.text = "${abs(diffStressed)}%"
                if (diffStressed > 0) {
                    icTrend.visible()
                    icTrend.rotation = 0f
                } else if (diffStressed < 0) {
                    icTrend.visible()
                    icTrend.rotation = 180f
                } else {
                    icTrend.gone()
                    tvDifference.text = "No change"
                }
                lytDifference.visible()
            } else {
                lytDifference.gone()

            }


        }
    }

    private fun handleUnitView(hour: Int, min: Int, view: OreoLayoutTopHourMn20Binding) {
        if (hour > 0 && min > 0) {
            view.apply {
                tvHour.visible()
                tvHourUnit.visible()
                tvMinuteUnit.visible()
                tvMinute.visible()
                tvHour.text = hour.toString()
                tvHourUnit.text = getString(R.string.text_hr)
                tvMinute.text = min.toString()
                tvMinuteUnit.text = getString(R.string.text_min)
            }

        } else if (hour > 0) {
            view.apply {
                tvHour.visible()
                tvHourUnit.visible()
                tvMinuteUnit.visible()
                tvMinute.visible()
                view.tvHour.text = hour.toString()
                view.tvHourUnit.text = getString(R.string.text_hr)
                view.tvMinute.text = 0.toString()
                view.tvMinuteUnit.text = getString(R.string.text_min)
            }
        } else if (min > 0) {
            view.apply {
                tvHour.visible()
                tvHourUnit.visible()
                tvMinuteUnit.visible()
                tvMinute.visible()
                view.tvHour.text = 0.toString()
                view.tvHourUnit.text = getString(R.string.text_hr)
                view.tvMinute.text = min.toString()
                view.tvMinuteUnit.text = getString(R.string.text_min)
            }
        } else {
            view.apply {
                tvHour.visible()
                tvHourUnit.gone()
                tvMinuteUnit.gone()
                tvMinute.gone()
                tvHour.text = "--"
            }
        }
    }


    override fun subscribeObservers() {
        mViewModel.selectedData.observe(this) { data ->

            binding.tvMsg.text = Html.fromHtml(data?.message ?: "")

            if (mViewModel.dayType?.equals("day", true) == true) {
                binding.tvDate.text = DateFormats.formatDateTime(
                    data.date,
                    DateFormats.dateFormat3,
                    DateFormats.dateFormat6
                )
                binding.tvDate.visible()
            } else {
                binding.tvDate.gone()
            }

            handleProgressStatus(data)


        }

        mViewModel.internalDetailsData.observe(this) {
            if (it != null) {
                updateUI(it)
            }
        }
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }
    }

    override fun onPositionSelected(position: Int, chartModel: ChartModelStress?) {
        val data = mViewModel.getDataByDate(chartModel?.date)
        data?.let {
            mViewModel.selectedData.postValue(it)
        }
    }

    override fun onScrolling(position: Int, chartModel: ChartModelStress?) {

    }
}