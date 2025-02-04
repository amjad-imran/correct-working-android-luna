package com.oreo.ui.sleep2.sleepplanner

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.noisefit.data.model.AlarmSoundDataModel
import com.noisefit.luna.databinding.BottomSheetAlarmSoundBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.oreo.util.alarm.AlarmUtil

const val ALARM_SOUND = "ALARM_SOUND"

class BottomSheetAlarmSound : BaseBottomSheetWithTransparent<BottomSheetAlarmSoundBinding>(
    BottomSheetAlarmSoundBinding::inflate
) {
    private var selectedTone: AlarmSoundDataModel? = null

    private val args: BottomSheetAlarmSoundArgs by navArgs()
    var mediaPlayer: Ringtone? = null


    private val soundAdapter: AlarmSoundAdapter by lazy {
        AlarmSoundAdapter(object : OnSoundItemClick {
            override fun onItemClick(data: AlarmSoundDataModel, position: Int) {
                selectedTone = data
                playSoundById(data.resId)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()

        val tones = args.alarmToneList
        selectedTone = tones.find { it.key == args.selectedKey }
        soundAdapter.setData(tones.toList(), selectedTone?.key ?: 1)

        selectedTone?.let {
            playSoundById(it.resId)
        }
    }

    private fun playSoundById(resId: Int) {
        mediaPlayer?.stop()
        mediaPlayer = RingtoneManager.getRingtone(
            context,
            Uri.parse("android.resource://" + context?.packageName + "/" + resId)
        )

        val audioAttributes: AudioAttributes =  AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        mediaPlayer?.setAudioAttributes(audioAttributes)

        mediaPlayer?.play()
    }

    private fun setRecycler() {
        with(binding.rvSound) {
            adapter = soundAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.stop()
    }

    override fun initListener() {
        binding.btnSave.setOnClickListener {
            if (selectedTone != null) {
                setFragmentResult(
                    ALARM_SOUND,
                    bundleOf("alarmTone" to selectedTone)
                )
            }
            navigateUpSafe()
        }

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }
}