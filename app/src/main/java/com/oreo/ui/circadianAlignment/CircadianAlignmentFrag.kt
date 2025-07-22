package com.oreo.ui.circadianAlignment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianAlignmentBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.CircadianGraphModel
import com.oreo.data.model.CircadianMidPointModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt

@AndroidEntryPoint
class CircadianAlignmentFrag :
    BaseFragment<FragmentCircadianAlignmentBinding>(FragmentCircadianAlignmentBinding::inflate) {

    private val viewModel: CircadianAlignmentViewModel by viewModels()

    private val correctiveActivitiesAdapter by lazy {
        CorrectiveActivitiesAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUi()
        setRecycler()
        setCircadianGraph()
        correctiveActivitiesAdapter.updateDataSet(viewModel.prepareCorrectiveActivitiesData())
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

    }

}