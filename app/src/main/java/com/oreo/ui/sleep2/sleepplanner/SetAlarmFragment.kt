package com.oreo.ui.sleep2.sleepplanner

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetAlarmFragment : BaseFragment<FragmentSetAlarmBinding>(FragmentSetAlarmBinding::inflate) {
    private val viewModel: SetAlarmViewModel by viewModels()
    private val mAdapter: SAActiveDaysAdapter by lazy {
        SAActiveDaysAdapter(object : OnActiveDayItemClick {
            override fun onItemClick(data: SAActiveDayDataModel, position: Int) {
                mAdapter.updateItem(data, position)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_set_alarm)
        with(binding.lytActiveDays.rvDays) {
            adapter = mAdapter
        }
        mAdapter.setData(viewModel.getAlarmData())
        binding.lytTopView.lytBedTime.ivIcon.setImageResource(R.drawable.ic_bedtime_sleep)
        binding.lytTopView.lytBedTime.tvTitle.text=getString(R.string.text_bedtime)

        binding.lytTopView.lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_sleep)
        binding.lytTopView.lytWakeupTime.tvTitle.text=getString(R.string.text_wakeup)
        binding.lytAlarmTime.tvHour.text="4"
        binding.lytAlarmTime.tvMin.text="30"
        binding.lytAlarmTime.tvHour.paint.setShader(viewModel.setViewGradient(binding,4.toString()))
        binding.lytAlarmTime.tvMin.paint.setShader(viewModel.setViewGradient(binding,30.toString()))
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnSave.setOnClickListener {
            //
        }

    }

    override fun subscribeObservers() {

    }

}