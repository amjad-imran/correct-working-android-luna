package com.noisefit.ui.reward.streak

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.StreakDetailsResponse
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class StreakDetailsViewModel @Inject constructor(
    val rewardsRepository: RewardsRepository,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) :
    BaseViewModel() {

    private val _streakData = MutableLiveData<StreakDetailsResponse>()
    val streakData: LiveData<StreakDetailsResponse>
        get() = _streakData


    /**
     * if in danger boolean will be true
     */
    private val _streakMessage = MutableLiveData<Pair<String, Boolean>>()
    val streakMessage: LiveData<Pair<String, Boolean>>
        get() = _streakMessage


    fun getStreakData() {
        timer?.cancel()
        viewModelScope.launch {
            rewardsRepository.getStreakData()
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }
                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getStreakData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {

                                it?.let { data ->
                                    if (it.curr_streak == null) {
                                        _streakMessage.value = (Pair(data.msg ?: "", false))
                                        sessionManager.logInsiderAppEvent(InsiderAppEvents.STEP_STREAK_NORMAL_PAGE_VISIT)
                                    } else {
                                        if (!it.current_time.isNullOrEmpty()) {
                                            checkDangerZone(it.current_time!!)
                                        }
                                    }
                                }
                                _streakData.postValue(it)
                            }

                        }
                    }
                }
        }
    }

    private fun checkDangerZone(currentTime: String) {
        val parsedTime = LocalDateTime.parse(
            currentTime,
            DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH
            )
        )

        if (parsedTime.hour >= 19) {
            val nextDate = parsedTime.plusDays(1).withHour(0)
                .withMinute(0)
                .withSecond(0)
            val duration = Duration.between(parsedTime, nextDate)
            startCountDownTimer(duration.seconds * 1000)
            sessionManager.logInsiderAppEvent(InsiderAppEvents.STEP_STREAK_ALERT_PAGE_VISIT)

        } else {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.STEP_STREAK_NORMAL_PAGE_VISIT)
            _streakMessage.value = (Pair("", false))
        }
    }

    var timer: CountDownTimer? = null
    fun startCountDownTimer(totalMillis: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millis: Long) {
                val hour = TimeUnit.MILLISECONDS.toHours(millis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
                val seconds =
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
                val stringBuilder = StringBuilder().apply {
                    if (hour != 0L) {
                        append(String.format("%02d", hour))
                        append(":")
                    }
                    append(String.format("%02d:", minutes))
                    append(String.format("%02d", seconds))
                }


                _streakMessage.value = (Pair(stringBuilder.toString(), true))
            }

            override fun onFinish() {
                getStreakData()
            }
        }
        timer?.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }

    /**
     * Streak in danger
     * Active streak
     * Previous streak-
     * Next milestone
     */
    fun getMessageToDisplay(
        clickedDate: LocalDate,
        currentDate: LocalDate,
        currentStreakDays: HashMap<LocalDate, BackType>,
        highlightStreaks: HashMap<LocalDate, BackType>,
        streakMilestoneHighlightDays: HashSet<LocalDate>
    ): String? {

        if (highlightStreaks[clickedDate] != null) {
            return "Previous streak"
        }

        if (currentStreakDays[clickedDate] != null) {
            if (clickedDate == currentDate && streakMessage.value?.second == true) {
                return "Streak in danger"
            }
            return "Active streak"
        }

        val indexOf = streakMilestoneHighlightDays.indexOf(clickedDate)

        if (indexOf != -1) {
            streakMilestoneHighlightDays.forEach {
                if (it > currentDate) {
                    return "Next milestone"
                }
            }
        }
        return null
    }
}