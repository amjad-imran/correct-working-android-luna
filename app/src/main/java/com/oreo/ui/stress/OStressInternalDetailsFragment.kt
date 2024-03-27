package com.oreo.ui.stress

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressInternalDetailsBinding
import com.noisefit.luna.databinding.OreoLayoutTopHourMn20Binding
import com.noisefit_commans.common.fromJson
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.ChartModelStress
import com.oreo.data.model.ResultDataStress
import com.oreo.data.model.OStressInternalPageResponseModal
import com.oreo.data.model.StressData
import com.oreo.ui.custom.ScrollListenerStress
import dagger.hilt.android.AndroidEntryPoint
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

    private fun updateUI(responseData: OStressInternalPageResponseModal) {

        //for stressed
        binding.lytTopView.lytStressed.tvTitle.text = getString(R.string.text_stressed)
        binding.lytTopView.lytStressed.tvTitle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.stress_nap_stressed
            )
        )
        if (responseData.stressData == null)
            return
        val strData = responseData.stressData
        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
            strData.stressed?.duration?.toFloat()?.roundToInt() ?: 0
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
            strData.focussed?.duration?.toFloat()?.roundToInt() ?: 0
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
            strData.focussed?.duration?.toFloat()?.roundToInt() ?: 0
        )
        handleUnitView(hour2, minute2, binding.lytTopView.lytCalm.lytUnit)
        binding.tvMsg.text = Html.fromHtml(strData.dspMsg)

        handleProgressStatus(strData)

        setStressGraph()

    }

    private fun setStressGraph() {
        val dummyData =
            "[ { \"date\": \"2024-03-20\", \"data\": { \"calm\":12, \"focussed\":8, \"stressed\":4 } }, { \"date\": \"2024-03-19\",  \"data\": { \"calm\":10, \"focussed\":6, \"stressed\":8 } }, { \"date\": \"2024-03-18\",  \"data\": { \"calm\":10, \"focussed\":8, \"stressed\":6 } }, { \"date\": \"2024-03-17\",  \"data\": { \"calm\":4, \"focussed\":8, \"stressed\":12 } }, { \"date\": \"2024-03-16\",  \"data\": { \"calm\":4, \"focussed\":6, \"stressed\":8 } }, { \"date\": \"2024-03-15\",  \"data\": { \"calm\":6, \"focussed\":12, \"stressed\":6 } }, { \"date\": \"2024-03-14\",  \"data\": { \"calm\":6, \"focussed\":6, \"stressed\":12 } }]"

        val data = Gson().fromJson<List<ResultDataStress>>(dummyData)

        val topGraphData = mViewModel.getPrefixAndSuffixList(
            data as ArrayList<ResultDataStress>,
           /* mViewModel.dayType*/"day"
        )
        binding.stressChart.visible()
        binding.stressChart.updateData(
            topGraphData.first,
            topGraphData.third,
            topGraphData.second
        )
    }

    private fun handleProgressStatus(strData: StressData) {
        if (strData.stressed?.score != null || (strData.stressed?.score ?: 0) > 0) {
            binding.lytTopView.lytStressed.view1.visible()
            binding.lytTopView.lytStressed.tvProgStatus.visible()
            binding.lytTopView.lytStressed.tvProgStatus.text = "${strData.stressed?.score} %"
        } else {
            binding.lytTopView.lytStressed.view1.gone()
            binding.lytTopView.lytStressed.tvProgStatus.gone()
        }
        if (strData.focussed?.score != null || (strData.focussed?.score ?: 0) > 0) {
            binding.lytTopView.lytFocussed.view1.visible()
            binding.lytTopView.lytFocussed.tvProgStatus.visible()
            binding.lytTopView.lytFocussed.tvProgStatus.text = "${strData.focussed?.score} %"
        } else {
            binding.lytTopView.lytFocussed.view1.gone()
            binding.lytTopView.lytFocussed.tvProgStatus.gone()
        }
        if (strData.calm?.score != null || (strData.focussed?.score ?: 0) > 0) {
            binding.lytTopView.lytCalm.view1.visible()
            binding.lytTopView.lytCalm.tvProgStatus.visible()
            binding.lytTopView.lytCalm.tvProgStatus.text = "${strData.calm?.score} %"
        } else {
            binding.lytTopView.lytCalm.view1.gone()
            binding.lytTopView.lytCalm.tvProgStatus.gone()
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

    }

    override fun onScrolling(position: Int, chartModel: ChartModelStress?) {

    }
}