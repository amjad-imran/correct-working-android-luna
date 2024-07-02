package com.oreo.ui.sleep2.sleepplanner

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.data.model.AlarmSoundDataModel
import com.noisefit.luna.databinding.BottomSheetAlarmSoundBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val ALARM_SOUND = "ALARM_SOUND"

class BottomSheetAlarmSound : BaseBottomSheetWithTransparent<BottomSheetAlarmSoundBinding>(
    BottomSheetAlarmSoundBinding::inflate
) {
    private var soundName: String = "N/A"
    private val soundAdapter: AlarmSoundAdapter by lazy {
        AlarmSoundAdapter(object : OnSoundItemClick {
            override fun onItemClick(data: AlarmSoundDataModel, position: Int) {
                soundAdapter.updateItem(data, position)
                soundName = data.title
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvSound) {
            adapter = soundAdapter
        }
        soundAdapter.setData(prepareData())
    }

    override fun initListener() {
        binding.btnSave.setOnClickListener {
            setFragmentResult(
                ALARM_SOUND,
                bundleOf("soundName" to soundName)
            )
            navigateUpSafe()
        }

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

    private fun prepareData(): ArrayList<AlarmSoundDataModel> {
        val dataList = ArrayList<AlarmSoundDataModel>()
        dataList.add(AlarmSoundDataModel(title = "BeepBeep", false))
        dataList.add(AlarmSoundDataModel(title = "Helios", false))
        dataList.add(AlarmSoundDataModel(title = "Bazzle", false))
        dataList.add(AlarmSoundDataModel(title = "BeepBeep", false))
        dataList.add(AlarmSoundDataModel(title = "Helios", false))
        dataList.add(AlarmSoundDataModel(title = "Bazzle", false))
        dataList.add(AlarmSoundDataModel(title = "BeepBeep", false))
        dataList.add(AlarmSoundDataModel(title = "Helios", false))
        dataList.add(AlarmSoundDataModel(title = "Bazzle", false))
        dataList.add(AlarmSoundDataModel(title = "BeepBeep", false))
        dataList.add(AlarmSoundDataModel(title = "Helios", false))
        dataList.add(AlarmSoundDataModel(title = "Bazzle", false))
        return dataList
    }
}