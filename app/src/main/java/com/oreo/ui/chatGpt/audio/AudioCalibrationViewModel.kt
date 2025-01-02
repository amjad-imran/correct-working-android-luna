package com.oreo.ui.chatGpt.audio

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.noisefit.NoiseFitApplicationMain
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.util.audiorecorder.RecorderState
import com.oreo.util.audiorecorder.WaveRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class AudioCalibrationViewModel @Inject constructor(
    private val localDataStore: DataStoredInterface
) : BaseViewModel() {

    private val TIMER_DURATION = 5000L

    var videoPlayState = MutableLiveData<Boolean>()

    private var lastFile: File? = null
    var waveRecorder: WaveRecorder? = null

    val maxAmpList = mutableListOf<Int>()

    var uiStates = MutableLiveData<Event<AudioCalibUiStates>>()
    var completionState = MutableLiveData<Int?>()

    var currentSessionMaxAmp = 0
    var currentTimer = MutableLiveData<Event<Int>>()
    var isRecording = false

    init {
        startUIFlow()
    }


    fun startNewRecording() {
        setLoading(true)
        LOGS.d("VOICE_RECORDER - Start New Recording")
        isRecording = true
        currentSessionMaxAmp = 0

        val fileName = "recording_${System.currentTimeMillis()}.wav"
        val filesDir = NoiseFitApplicationMain.context!!.filesDir
        val audioFolder = File(filesDir, "audio")
        if (!audioFolder.exists()) {
            audioFolder.mkdirs()
        }

        lastFile = File(audioFolder, fileName)

        waveRecorder = WaveRecorder(filePath = lastFile!!.absolutePath).apply {

            startRecording()
            onStateChangeListener = {
                LOGS.d("VOICE_RECORDER  ${it.name}")
                when (it) {
                    RecorderState.RECORDING -> {
                        setLoading(false)
                        startTimer()
                    }

                    RecorderState.STOP -> {
                        isRecording = false
                    }

                    RecorderState.PAUSE -> {}
                    RecorderState.SKIPPING_SILENCE -> {}
                }
            }
        }

        waveRecorder?.onAmplitudeListener = null
        waveRecorder?.onAmplitudeListener = {
            if (currentSessionMaxAmp < it) {
                currentSessionMaxAmp = it
            }
        }
    }

    /**
     * create a timer of 3 seconds with callback
     */
    var timer: CountDownTimer? = null
    fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(TIMER_DURATION, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val percent =
                    ((TIMER_DURATION - millisUntilFinished).toFloat() / TIMER_DURATION) * 100
                val percentInt = percent.toInt()
                LOGS.d("AudioCalibrationViewModel ${percentInt}")

                if(percentInt<100){
                    currentTimer.value = (Event(percent.toInt()))
                }
            }

            override fun onFinish() {
                waveRecorder?.stopRecording(true)
                maxAmpList.add(currentSessionMaxAmp)

                currentTimer.value = (Event(100))
                //sendMessage("Max amp $currentSessionMaxAmp")
            }
        }
        timer?.start()
    }

    fun sameMaxAmp() {
        if (maxAmpList.size < 3) {
            return
        }

        val ampAverage = maxAmpList.average()
        LOGS.d("AudioCalibrationViewModel $ampAverage")
        var calibrated = (ampAverage / 2).roundToInt()
        LOGS.d("AudioCalibrationViewModel $calibrated")

        if (calibrated < 100) {
            calibrated = 100
        }
        AppLogs.sendAppLogs("Audio AI calibrated value - $calibrated | raw ->${maxAmpList}")
        localDataStore.saveAudioMaxAmp(calibrated)
    }

    private fun startUIFlow() {
        uiStates.postValue(Event(AudioCalibUiStates.STEP_1))
        Handler(Looper.getMainLooper()).postDelayed({
            uiStates.postValue(Event(AudioCalibUiStates.STEP_2))
        }, 2000)
    }

}

enum class AudioCalibUiStates {
    STEP_1, STEP_2
}