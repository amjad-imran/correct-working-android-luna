package com.oreo.ui.sleep2.sleepplanner.exercise

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BreathingExerciseViewModel @Inject constructor() : BaseViewModel() {

    var timer: CountDownTimer? = null
    private val TOTAL_TIME = 4 * 19 * 1000L

    val timerRunning = MutableLiveData<Long?>(null)//seconds remaining

    fun startTimer() {
        timer?.cancel()
        timerRunning.value = (null)
        Handler(Looper.getMainLooper()).postDelayed({
            timerRunning.value = (TOTAL_TIME / 1000)
            timer = object : CountDownTimer(TOTAL_TIME, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerRunning.value = (millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    timerRunning.value = (0L)
                }
            }
            timer?.start()
        }, 3000)
    }

    fun cancelTimer() {
        timerRunning.value = (null)
        timer?.cancel()
    }

}