package com.oreo.ui.sleep2.sleepplanner

import android.os.Bundle
import android.view.View
import com.noisefit.data.model.SAGoalDataModel
import com.noisefit.luna.databinding.BottomSheetGoalBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val SA_GOAL = "SA_GOAL"

@AndroidEntryPoint
class BottomSheetSetYourGoal : BaseBottomSheetWithTransparent<BottomSheetGoalBinding>(
    BottomSheetGoalBinding::inflate
) {

    private val goalAdapter: SAGoalAdapter by lazy {
        SAGoalAdapter(object : OnGoalItemClick {
            override fun onItemClick(data: SAGoalDataModel, position: Int) {
                goalAdapter.updateItem(data, position)

            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvFlow) {
            adapter = goalAdapter
        }
        goalAdapter.setData(prepareData())
    }


    override fun initListener() {
        binding.btnSave.setOnClickListener {
            navigateUpSafe()
        }


        /*binding.ivLogAdd.setOnClickListener {

            setFragmentResult(
                CYCLE_LOG_SAVE,
                bundleOf("openActivity" to true)
            )
            navigateUpSafe()
        }*/
    }


    override fun subscribeObservers() {


    }

    private fun prepareData(): ArrayList<SAGoalDataModel> {
        val dataList = ArrayList<SAGoalDataModel>()
        dataList.add(SAGoalDataModel(title = "Achieve 100% goal", false))
        dataList.add(SAGoalDataModel(title = "Achieve 85% goal", false))
        dataList.add(SAGoalDataModel(title = "Maintain weekly consistency", false))
        return dataList
    }
}