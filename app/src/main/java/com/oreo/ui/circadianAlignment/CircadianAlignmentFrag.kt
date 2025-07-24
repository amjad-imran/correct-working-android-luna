package com.oreo.ui.circadianAlignment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianAlignmentBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.CircadianGraphModel
import com.oreo.data.model.CircadianMidPointModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.circadian.CircadianResponseModel

@AndroidEntryPoint
class CircadianAlignmentFrag :
    BaseFragment<FragmentCircadianAlignmentBinding>(FragmentCircadianAlignmentBinding::inflate) {

    private val viewModel: CircadianAlignmentViewModel by viewModels()

    private val correctiveActivitiesAdapter by lazy {
        CorrectiveActivitiesAdapter(){
            setFragmentResultListener(LOG_CIRCADIAN_BOTTOM_SHEET_KEY) { _, bundle ->
                val key = bundle.getString("key")
                val isLogged = bundle.getBoolean("isLogged")
                viewModel.postLogData(key, isLogged)
            }
            navigate(R.id.logCircadianBottomSheetFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUi()
        setRecycler()
        setCircadianGraph()
//        correctiveActivitiesAdapter.updateDataSet(viewModel.prepareCorrectiveActivitiesData(it.activities))
    }

    private fun setCircadianGraph(){
        val graphView = binding.lytSleepMidPoint.circadianGraph

        val circadianGraphModelList = ArrayList<CircadianGraphModel>()
        for (index in 0..binding.lytSleepMidPoint.circadianGraph.totalBars - 1) {
            var xAxis: String? = null
            var firstMidPoint: CircadianMidPointModel? = null
            var secondMidPoint: CircadianMidPointModel? = null

            when (index) {
                0 -> {
                    xAxis = "12:00 am"

                }

                4 -> {
                    firstMidPoint = CircadianMidPointModel()
                    firstMidPoint.apply {
                        this.index = index
                        this.color = "#D2D2D2".toColorInt()
                        this.bgColor = "#3D3F43".toColorInt()
                        this.title = "Avg Before"
                    }


                }

                27 -> {
                    secondMidPoint = CircadianMidPointModel()
                    secondMidPoint.apply {
                        this.index = index
                        this.color = "#F3F19C".toColorInt()
                        this.bgColor = "#2C241F".toColorInt()
                        this.title = "Avg Now"
                    }
                }

                binding.lytSleepMidPoint.circadianGraph.totalBars - 1 -> {
                    xAxis = "5:00 am"
                }
            }

            circadianGraphModelList.add(
                CircadianGraphModel(
                    xAxis = xAxis,
                    firstMidPoint = firstMidPoint,
                    secondMidPoint = secondMidPoint,
                )
            )
        }

        val colors = List(binding.lytSleepMidPoint.circadianGraph.totalBars) {
            when (it) {
                in 0..5 -> "#444444".toColorInt()
                in 6..12 -> "#aa8866".toColorInt()
                in 13..20 -> "#7799cc".toColorInt()
                else -> "#333333".toColorInt()
            }
        }

        graphView.avgBeforeIndex = 6
        graphView.avgNowIndex = 9

        graphView.updateBars(circadianGraphModelList, colors)
    }
    private fun setUi() {
        binding.toolbar.tvTitle.text = getString(R.string.text_circadian_alignment)

    }

    private fun setRecycler() {
        binding.lytCorrectiveActivities.recyclerV.layoutManager = LinearLayoutManager(context)
        binding.lytCorrectiveActivities.recyclerV.adapter = correctiveActivitiesAdapter
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytYourChronotype.tvRetakeQuiz.setOnClickListener {
            navigate(R.id.quizCircadianFragment)
        }
    }

    override fun subscribeObservers() {
        viewModel.circadianResponseData.observe(this){
            LOGS.d("abcjacjcab Observing data: $it")
            setData(it)
        }

        viewModel.correctiveActivitiesListData.observe(this){
            if(!it.isNullOrEmpty()){
                correctiveActivitiesAdapter.updateDataSet(it)
            }
        }
    }

    fun setData(data: CircadianResponseModel){
        LOGS.d("ansckaasc: $data")
        // activity monitor
        val activityMonitorData = data.activity_monitor
        binding.lytActivityMonitor.apply {
            activityMonitorData.forEach {
                when(it.type){
                    CircadianAlignmentViewModel.daily_steps_key -> {
                        ivStateSteps.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.meal_window_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.light_exposure_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.caffeine_window_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.workout_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    else -> {}
                }
            }
        }

        // corrective activities
        /*val correctiveActivitiesData = data.activities
        binding.lytCorrectiveActivities.apply {
            correctiveActivitiesData.
        }*/

        // your chronotype
        val chronotypeData = data.chronotype
        binding.lytYourChronotype.apply {
            tvTitle.text = chronotypeData.type
            tvDesc.text = chronotypeData.description
        }
    }

    private fun getActMoniStatusIcon(status: String?): Int {
        return when(status){
            CircadianAlignmentViewModel.actMonStatusList.get(0) -> R.drawable.ic_cancel
            CircadianAlignmentViewModel.actMonStatusList.get(1) -> R.drawable.ic_hm_check_mark
            else -> R.drawable.ic_hm_check_default_circadian
        }
    }

}