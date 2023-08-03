package com.oreo.ui.workout.detect

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDetectWorkoutBinding
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.CandleChartModel
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM1 = "ARG_PARAM1"
private const val ARG_PARAM2 = "ARG_PARAM2"

@AndroidEntryPoint
class DetectWorkoutFragment :
    BaseFragment<FragmentDetectWorkoutBinding>(FragmentDetectWorkoutBinding::inflate) {

    private val viewModel: DetectWorkoutViewModel by viewModels()
    private var detectWorkoutFragmentListener: DetectWorkoutFragmentListener? = null
    private val oreoAutoSportData = ArrayList<OreoAutoSportData>()
    private var key: String = ""


    private val detectWorkoutAdapter: DetectWorkoutAdapter by lazy {
        DetectWorkoutAdapter(object : DetectWorkoutListener {
            override fun onIdentifyWorkout(data: OreoAutoSportData, position: Int) {
                detectWorkoutFragmentListener?.onIdentifyWorkout(data, key)
            }

            override fun onDismissWorkout(data: OreoAutoSportData, position: Int) {

                requireActivity().supportFragmentManager.setFragmentResultListener(
                    ALERT_REQUEST_KEY,
                    viewLifecycleOwner
                ) { _, bundle ->
                    val updated = bundle.getBoolean("allow")

                    if (updated) {
                        viewModel.deleteAutoSport(data.id)
                        detectWorkoutAdapter.removeItem(position)
                        detectWorkoutFragmentListener?.onDismissWorkout(data, key)
                    }
                }

                navigate(
                    DetectWorkoutListFragmentDirections.actionDetectWorkoutListFragmentToAlertTextBottomSheet(
                        getString(R.string.text_dismiss_activity_title),
                        getString(R.string.text_dismiss_activity_desc), "", ""
                    )
                )
            }

        })
    }


    private fun setAdapter() {
        with(binding.rv) {
            adapter = detectWorkoutAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

        viewModel.dayTimeMovementList.observe(this) {
            it?.let {
                setData(it)
            }
        }
    }

    fun setDetectWorkoutListener(detectWorkoutFragmentListener: DetectWorkoutFragmentListener) {
        this.detectWorkoutFragmentListener = detectWorkoutFragmentListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        if (oreoAutoSportData.isNotEmpty()) {
            val date = DateFormats.convertTimestampToDate(
                oreoAutoSportData[0].startTime,
                DateFormats.dateFormat3
            )
            viewModel.getDayTimeMovement(date)
        }


    }

    private fun setData(movementList: List<Int>) {

        val baseHrList = UtilClass.graphBaseInterval(null, null, movementList.size)
        val candleChartModelList: MutableList<CandleChartModel> =
            java.util.ArrayList<CandleChartModel>()
        movementList.forEachIndexed { index, data ->

            val chartModel = CandleChartModel()

            chartModel.bottomLineText = baseHrList[index]

            when (data) {
                1 -> {

                    chartModel.length =
                        (binding.candleChart.max * 0.4).toInt()
                    chartModel.color = Color.parseColor("#4cffd230")
                    chartModel.type = CandleChartModel.Type.LOW

                }

                2 -> {

                    chartModel.length =
                        (binding.candleChart.max * 0.6).toInt()
                    chartModel.color = Color.parseColor("#ffd230")
                    chartModel.type = CandleChartModel.Type.MEDIUM
                }

                3, 4 -> {

                    chartModel.length =
                        (binding.candleChart.max * 0.8).toInt()
                    chartModel.color = Color.parseColor("#ffffff")

                    chartModel.type = CandleChartModel.Type.HIGH
                }

                else -> {
                    chartModel.length =
                        (binding.candleChart.max * 0.8).toInt()
                    chartModel.color = Color.parseColor("#4c4c4c")
                    chartModel.type = CandleChartModel.Type.INACTIVE

                }
            }
            chartModel.value = data
            candleChartModelList.add(chartModel)
        }
        binding.candleChart.updateData(candleChartModelList)
        detectWorkoutAdapter.setData(oreoAutoSportData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getParcelableArrayList<OreoAutoSportData>(ARG_PARAM1)
                ?.let { it1 -> oreoAutoSportData.addAll(it1) }

            key = it.getString(ARG_PARAM2)!!
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(dataList: ArrayList<OreoAutoSportData>, key: String) =
            DetectWorkoutFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, dataList as ArrayList)
                    putString(ARG_PARAM2, key)
                }
            }
    }
}

interface DetectWorkoutFragmentListener {
    fun onIdentifyWorkout(data: OreoAutoSportData, key: String)
    fun onDismissWorkout(data: OreoAutoSportData, key: String)
}