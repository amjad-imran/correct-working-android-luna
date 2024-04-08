package com.oreo.ui.stress

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressInternalDetailsBinding
import com.noisefit.luna.databinding.OreoLayoutTopHourMn20Binding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModelStress
import com.oreo.data.model.StressResultData
import com.oreo.ui.custom.ScrollListenerStress
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DAY_TYPE = "DAY_TYPE"
private const val DATE = "DATE"

@AndroidEntryPoint
class OStressInternalDetailsFragment :
    BaseFragment<FragmentOStressInternalDetailsBinding>(FragmentOStressInternalDetailsBinding::inflate),
    ScrollListenerStress {
    private val mViewModel: OSIDViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            mViewModel.dayType = it.getString(DAY_TYPE).toString()
            mViewModel.selectedDate = it.getString(DATE)
        }

        mViewModel.getInternalDetailsData()
    }

    companion object {
        fun newInstance(dayType: String, date: String) =
            OStressInternalDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(DAY_TYPE, dayType)
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
        binding.stressChart.updateData(
            topGraphData.first,
            topGraphData.third,
            topGraphData.second
        )
    }

    private fun handleProgressStatus(strData: StressResultData?) {

        binding.lytTopView.lytCalm.apply {
            val diffCalm = mViewModel.getDifference(
                strData?.data?.calm?.duration ?: 0,
                strData?.data?.calm?.typicalDay ?: 0
            )
            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                strData?.data?.calm?.duration ?: 0
            )
            lytUnit.tvHour.text = "$hourCalm"
            lytUnit.tvHourUnit.text = getString(R.string.text_hr)
            lytUnit.tvMinute.text = "$minuteCalm"
            lytUnit.tvMinuteUnit.text = getString(R.string.text_mins)

            tvDifference.text = "${abs(diffCalm)}%"
            if (diffCalm > 0) {
                icTrend.visible()
                icTrend.rotation = 0f
            } else if (diffCalm < 0) {
                icTrend.visible()
                icTrend.rotation = 180f
            } else {
                icTrend.invisible()
            }
        }

        binding.lytTopView.lytFocussed.apply {
            val diffFocussed = mViewModel.getDifference(
                strData?.data?.focused?.duration ?: 0,
                strData?.data?.focused?.typicalDay ?: 0
            )
            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                strData?.data?.focused?.duration ?: 0
            )
            lytUnit.tvHour.text = "$hourCalm"
            lytUnit.tvHourUnit.text = getString(R.string.text_hr)
            lytUnit.tvMinute.text = "$minuteCalm"
            lytUnit.tvMinuteUnit.text = getString(R.string.text_mins)

            tvDifference.text = "${abs(diffFocussed)}%"
            if (diffFocussed > 0) {
                icTrend.visible()
                icTrend.rotation = 0f
            } else if (diffFocussed < 0) {
                icTrend.visible()
                icTrend.rotation = 180f
            } else {
                icTrend.invisible()
            }
        }

        binding.lytTopView.lytStressed.apply {
            val diffStressed = mViewModel.getDifference(
                strData?.data?.stressed?.duration ?: 0,
                strData?.data?.stressed?.typicalDay ?: 0
            )
            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                strData?.data?.stressed?.duration ?: 0
            )
            lytUnit.tvHour.text = "$hourCalm"
            lytUnit.tvHourUnit.text = getString(R.string.text_hr)
            lytUnit.tvMinute.text = "$minuteCalm"
            lytUnit.tvMinuteUnit.text = getString(R.string.text_mins)

            tvDifference.text = "${abs(diffStressed)}%"
            if (diffStressed > 0) {
                icTrend.visible()
                icTrend.rotation = 0f
            } else if (diffStressed < 0) {
                icTrend.visible()
                icTrend.rotation = 180f
            } else {
                icTrend.invisible()
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

            context.showShortToast("Data changed")

            val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                data?.data?.stressed?.duration?.toFloat()?.roundToInt() ?: 0
            )

            handleUnitView(hour, minute, binding.lytTopView.lytStressed.lytUnit)
            // for focussed
            binding.lytTopView.lytFocussed.tvTitle.text = getString(R.string.text_focussed)
            binding.lytTopView.lytFocussed.tvTitle.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.stress_nap_focussed
                )
            )
            val (hour1, minute1) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                data?.data?.focused?.duration?.toFloat()?.roundToInt() ?: 0
            )
            handleUnitView(hour1, minute1, binding.lytTopView.lytFocussed.lytUnit)
            // for calm
            binding.lytTopView.lytCalm.tvTitle.text = getString(R.string.text_calm)
            binding.lytTopView.lytCalm.tvTitle.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.stress_nap_calm
                )
            )
            val (hour2, minute2) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                data?.data?.focused?.duration?.toFloat()?.roundToInt() ?: 0
            )
            handleUnitView(hour2, minute2, binding.lytTopView.lytCalm.lytUnit)
            binding.tvMsg.text = Html.fromHtml(data?.message?:"")

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
        data?.let{
            mViewModel.selectedData.postValue(it)
        }

        LOGS.d("dsfsdfsdfsf $data")
    }

    override fun onScrolling(position: Int, chartModel: ChartModelStress?) {

    }
}