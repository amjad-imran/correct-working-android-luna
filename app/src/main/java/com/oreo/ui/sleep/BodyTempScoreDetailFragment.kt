package com.oreo.ui.sleep

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentBodyTempScoreDetailBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.ChartModel
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.ResultData
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.OSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BodyTempScoreDetailFragment :
    BaseFragment<FragmentBodyTempScoreDetailBinding>(FragmentBodyTempScoreDetailBinding::inflate),
    ScrollListener {

    private val mViewModel: OSCDViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.dayType = "Day"
        mViewModel.selectedDate = "2023-12-13"//TODO change
        mViewModel.itemClickType = ViewItemClickType.BODY_TEMPERATURE.name

        mViewModel.getReadinessInternalDetailsData()
    }

    private fun initUi(it: OInternalPageResponseModal) {
        val topGraphData = mViewModel.getPrefixAndSuffixList(
            it.result as ArrayList<ResultData>,
            mViewModel.dayType
        )
        binding.rvTopBarGraph.updateDataWithMax(
            topGraphData.first.first,
            topGraphData.third,
            topGraphData.second,
            topGraphData.first.second,
            barGraphScoreColor().first,
            barGraphScoreColor().second

        )
    }

    override fun initListener() {
        binding.rvTopBarGraph.setOnChartScrollChangedListener(this)

    }

    override fun subscribeObservers() {
        mViewModel.internalDetailsData.observe(viewLifecycleOwner) {
            if (it != null) {
                initUi(it)
            }
        }
    }

    private fun barGraphScoreColor(): Pair<Int, Int> {
        var normalColor: Int = 0
        var selectedColor: Int = 0

        normalColor = ContextCompat.getColor(
            requireContext(),
            R.color.readiness_un_selected_bar_color
        )
        selectedColor = ContextCompat.getColor(
            requireContext(),
            R.color.readiness_selected_bar_color
        )

        return Pair(normalColor, selectedColor)

    }

    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {

    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }


}