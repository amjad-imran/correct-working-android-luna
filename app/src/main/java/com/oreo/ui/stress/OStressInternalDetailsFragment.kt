package com.oreo.ui.stress

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressInternalDetailsBinding
import com.noisefit.luna.databinding.OreoLayoutTopHourMn20Binding
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.ResultData
import com.oreo.data.model.ResultDataStress
import dagger.hilt.android.AndroidEntryPoint

private const val DAY_TYPE = "DAY_TYPE"
private const val DATE = "DATE"

@AndroidEntryPoint
class OStressInternalDetailsFragment :
    BaseFragment<FragmentOStressInternalDetailsBinding>(FragmentOStressInternalDetailsBinding::inflate) {
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

        //mViewModel.getInternalDetailsData()
        updateUI()
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

    }

    private fun updateUI() {

        //for stressed
        binding.lytTopView.lytStressed.tvTitle.text = getString(R.string.text_stressed)
        binding.lytTopView.lytStressed.tvTitle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.stress_nap_stressed
            )
        )
        val hour = 8
        val min = 10
        handleUnitView(hour, min, binding.lytTopView.lytStressed.lytUnit)
        binding.lytTopView.lytStressed.tvProgStatus.text =
            "5%"//todo will update once response model define
        // for focussed
        binding.lytTopView.lytFocussed.tvTitle.text = getString(R.string.text_focussed)
        binding.lytTopView.lytFocussed.tvTitle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.stress_nap_focussed
            )
        )
        handleUnitView(hour, min, binding.lytTopView.lytFocussed.lytUnit)
        binding.lytTopView.lytFocussed.tvProgStatus.text =
            "5%"//todo will update once response model define
        // for calm
        binding.lytTopView.lytCalm.tvTitle.text = getString(R.string.text_calm)
        binding.lytTopView.lytFocussed.tvTitle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.stress_nap_calm
            )
        )
        handleUnitView(hour, min, binding.lytTopView.lytCalm.lytUnit)
        binding.lytTopView.lytCalm.tvProgStatus.text =
            "5%"//todo will update once response model define


        binding.tvMsg.text = "Dummy text"//todo will update once response model define

        setStressGraph()

    }

    private fun setStressGraph() {
        val dummyData = "[ { \"date\": \"2024-03-20\", \"data\": { \"calm\":12, \"focussed\":8, \"stressed\":4 } }, { \"date\": \"2024-03-19\",  \"data\": { \"calm\":10, \"focussed\":6, \"stressed\":8 } }, { \"date\": \"2024-03-18\",  \"data\": { \"calm\":10, \"focussed\":8, \"stressed\":6 } }, { \"date\": \"2024-03-17\",  \"data\": { \"calm\":4, \"focussed\":4, \"stressed\":16 } }, { \"date\": \"2024-03-16\",  \"data\": { \"calm\":16, \"focussed\":4, \"stressed\":4 } }, { \"date\": \"2024-03-15\",  \"data\": { \"calm\":12, \"focussed\":12, \"stressed\":0 } }, { \"date\": \"2024-03-14\",  \"data\": { \"calm\":6, \"focussed\":6, \"stressed\":12 } }]"
        val data = Gson().fromJson<List<ResultDataStress>>(dummyData)

        val topGraphData = mViewModel.getPrefixAndSuffixList(
            data as ArrayList<ResultDataStress>,
            mViewModel.dayType
        )
        binding.stressChart.updateData(
            topGraphData.first.first,
            topGraphData.third,
            topGraphData.second

        )
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
                view.tvHour.visible()
                view.tvHourUnit.visible()
                view.tvMinuteUnit.gone()
                view.tvMinute.gone()
                view.tvHour.text = hour.toString()
                view.tvHourUnit.text = getString(R.string.text_hr)
            }
        } else if (min > 0) {
            view.apply {
                view.tvHour.gone()
                view.tvHourUnit.gone()
                view.tvMinuteUnit.visible()
                view.tvMinute.visible()
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
                //todo handle ui on data response
//                updateUI(it)
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

}