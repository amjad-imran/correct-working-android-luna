package com.oreo.ui.workout.detect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDetectWorkoutBinding
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM1 = "ARG_PARAM1"
private const val ARG_PARAM2 = "ARG_PARAM2"

@AndroidEntryPoint
class DetectWorkoutFragment :
    BaseFragment<FragmentDetectWorkoutBinding>(FragmentDetectWorkoutBinding::inflate) {

    private val viewModel: DetectWorkoutViewModel by viewModels()
    private var detectWorkoutFragmentListener: DetectWorkoutFragmentListener? = null
    private val oreoAutoSportData = ArrayList<OreoAutoSportData>()


    private val detectWorkoutAdapter: DetectWorkoutAdapter by lazy {
        DetectWorkoutAdapter(object : DetectWorkoutListener {
            override fun onIdentifyWorkout(data: OreoAutoSportData, position: Int) {


                viewModel.addWorkout(data, onAddSuccess = {
                    viewModel.markWorkoutSynced(data.id, position)
                })
            }

            override fun onDismissWorkout(data: OreoAutoSportData, position: Int) {

                requireActivity().supportFragmentManager.setFragmentResultListener(
                    ALERT_REQUEST_KEY,
                    viewLifecycleOwner
                ) { _, bundle ->
                    val updated = bundle.getBoolean("allow")

                    if (updated) {
                        viewModel.markWorkoutSynced(data.id, position)

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

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.workoutDeleted.observe(this) {
            it.getContent()?.let { data ->
                detectWorkoutAdapter.removeItem(data.second)
                detectWorkoutFragmentListener?.onDismissWorkout(data.first, viewModel.dayKey)
            }
        }

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
        viewModel.movementList = movementList
        /* val topIndexList = UtilClass.getDetectedWorkoutMovementList(oreoAutoSportData)
         val baseHrList = UtilClass.graphBaseInterval(null, null, movementList.size)
         val candleChartModelList: MutableList<CandleChartModel> =
             java.util.ArrayList<CandleChartModel>()*/
        /*movementList.forEachIndexed { index, data ->

            val chartModel = CandleChartModel()

            chartModel.bottomLineText = baseHrList[index]
            chartModel.topText = topIndexList[index].toString()
            when (data) {
                1 -> {

                    chartModel.length =
                        (binding.candleChart.max * 0.4).toInt()
                    chartModel.color = Color.parseColor("#8088d6eb")
                    chartModel.type = CandleChartModel.Type.LOW

                }

                2 -> {

                    chartModel.length =
                        (binding.candleChart.max * 0.6).toInt()
                    chartModel.color = Color.parseColor("#88d6eb")
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
                        (binding.candleChart.max * 0.2).toInt()
                    chartModel.color = Color.parseColor("#3d3d3d")
                    chartModel.type = CandleChartModel.Type.INACTIVE

                }
            }
            chartModel.value = data
            candleChartModelList.add(chartModel)
        }*/
        //binding.candleChart.updateData(candleChartModelList)


        handleMovementNewViews(movementList)

        detectWorkoutAdapter.setData(oreoAutoSportData)
    }

    private fun handleMovementNewViews(movementList: List<Int>) {

        val newList = viewModel.getCombinedMovementData(movementList)
        val workoutList = viewModel.getWorkoutPointsList(oreoAutoSportData)
        binding.movementChart.setData(newList, workoutList)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getParcelableArrayList<OreoAutoSportData>(ARG_PARAM1)
                ?.let { it1 -> oreoAutoSportData.addAll(it1) }

            viewModel.dayKey = it.getString(ARG_PARAM2)!!
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
    fun onIdentifyWorkout(data: OreoAutoSportData, key: String, movementList: List<Int>?)
    fun onDismissWorkout(id: Int, key: String)
}