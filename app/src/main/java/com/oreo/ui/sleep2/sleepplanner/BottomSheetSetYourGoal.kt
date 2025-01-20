package com.oreo.ui.sleep2.sleepplanner

import android.os.Bundle
import android.view.View
import com.noisefit.data.model.SAGoalDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetGoalBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
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
                binding.lytSetGoalView.btnSave.isEnabled = true
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytSetGoalView.btnSave.isEnabled = false
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.lytSetGoalView.rvFlow) {
            adapter = goalAdapter
        }
        goalAdapter.setData(prepareData())
    }


    override fun initListener() {
        binding.lytSetGoalView.btnSave.setOnClickListener {

            binding.lytSetGoalView.root.gone()
            binding.lytGoalSetDone.root.visible()
        }
        binding.lytGoalSetDone.btnSave.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytSetGoalView.btnCancel.setOnClickListener {
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
        dataList.add(SAGoalDataModel(title = getString(R.string.text_recover_sleep_debt), false,"key_1")) //todo key to be be changed
        dataList.add(SAGoalDataModel(title = getString(R.string.text_establish_consistency), false,"key_1")) //todo key to be be changed
        return dataList
    }
}