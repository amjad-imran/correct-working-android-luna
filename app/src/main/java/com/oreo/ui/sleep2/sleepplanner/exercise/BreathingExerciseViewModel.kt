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
    private val TOTAL_TIME = 3 * 19 * 1000L

    val timerRunning = MutableLiveData<Long?>(null)//seconds remaining

    fun startTimer() {
        timer?.cancel()
        timerRunning.postValue(null)
        Handler(Looper.getMainLooper()).postDelayed({
            timerRunning.postValue(TOTAL_TIME / 1000)
            timer = object : CountDownTimer(TOTAL_TIME, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerRunning.postValue(millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    timerRunning.postValue(0L)
                }
            }
            timer?.start()
        }, 4000)
    }

    fun cancelTimer() {
        timer?.cancel()
    }

}