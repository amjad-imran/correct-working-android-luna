package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.noisefit.luna.databinding.FragmentSleepBarChartBinding
import com.noisefit.luna.databinding.FragmentSleepTempDeviationBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.ChartModel
import com.oreo.data.model.ResultData
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class SleepTempDeviationChartFragment :
    BaseFragment<FragmentSleepTempDeviationBinding>(FragmentSleepTempDeviationBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private var pageData: TrendsGraphData? = null

    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

    companion object {
        private const val graphData = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) =
            SleepTempDeviationChartFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(graphData, pageData)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            pageData = bundle.getParcelable(SleepTempDeviationChartFragment.graphData)
        }

        val dataList = convertData(pageData?.data)

        val topGraphData = sharedViewModel.getPrefixAndSuffixListTemp(
            dataList as ArrayList<ResultData>,
        )
        binding.rvTopBarGraph.updateDataWithMax(
            topGraphData.first.first,
            topGraphData.third,
            topGraphData.second
        )

        pageData?.data?.firstOrNull()?.date?.let {
            sharedViewModel.sendInteractDay(LocalDate.parse(it))
        }


        binding.rvTopBarGraph.setOnChartScrollChangedListener(object : ScrollListener {
            override fun onPositionSelected(position: Int, chartModel: ChartModel?) {
                try {
                    val calcPos = (pageData?.data?.size ?: 0) - (position - 14)
                    val date = pageData?.data?.get(calcPos)?.date
                    sharedViewModel.sendInteractDay(LocalDate.parse(date))
                } catch (exp: Exception) {
                    sharedViewModel.sendInteractDay(null)
                }
            }

            override fun onScrolling(position: Int, chartModel: ChartModel?) {

            }

        })

    }

    private fun convertData(data: List<TrendsValues>?): List<ResultData> {
        return data?.map {
            ResultData(
                date = it.date ?: "",
                data = it.value1 ?: 0.0f,
                deviation = it.value2
            )
        } ?: ArrayList()
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}