package com.oreo.ui.chatGpt.functions

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.noisefit.data.model.AiWorkout
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAiWorkoutDetailBinding
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import com.oreo.ui.lifeos.LifeOsChatFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AiWorkoutDetailFragment :
    BaseFragment<FragmentAiWorkoutDetailBinding>(FragmentAiWorkoutDetailBinding::inflate) {

    val navArgs: AiWorkoutDetailFragmentArgs by navArgs()
    private val TIMER_DURATION = 3000L

    private var dataList = ArrayList<AiWorkout>()

    //move to viewModel
    private var timer: CountDownTimer? = null
    private val messagesStrings = ArrayList<String>()
    private val displayMessage = MutableLiveData<String>()
    private var currentPos = 0

    @Inject
    lateinit var ringDataStore: RingDataStore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataList = navArgs.data.toMutableList()

        this.dataList.clear()
        this.dataList.addAll(dataList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI(navArgs.workoutType)
    }

    private fun setUI(workoutType: String) {
        binding.toolbar.tvTitle.text = getString(R.string.text_workout)
        binding.toolbar.tvTitle.setTextColor(Color.parseColor("#A8FFFF"))

        val workout = dataList.first()

        messagesStrings.add(
            getString(
                R.string.text_find_alternatives_of_value,
                workout.workout_name
            )
        )
        messagesStrings.add(
            getString(
                R.string.text_how_does_help_my_body_value,
                workout.workout_name
            )
        )
        messagesStrings.add(getString(R.string.text_how_do_i_do_value, workout.workout_name))

        displayMessage.postValue(getWorkoutAiString(currentPos))
        startTimer()

        binding.lytWorkoutDetails.apply {
            tvWorkoutType.text = workoutType
            tvReps.text = workout.reps
            tvWorkoutName.text = workout.workout_name
            tvDescription.text = workout.description
        }

        if (dataList.size > 1) {
            binding.ivNext.visible()
            binding.tvNextWorkout.visible()
        } else {
            binding.ivNext.gone()
            binding.tvNextWorkout.gone()
        }
    }

    override fun initListener() {

        binding.ivMic.setOnClickListener {
            if (ringDataStore.getRingDevice() == null) {
                context.showShortToast(getString(R.string.text_luna_ai_message))
                return@setOnClickListener
            }
            //val workout = dataList.first()
            val ques = getWorkoutAiString(currentPos)
            val (frag, bundle) = AudioAiFragment.getStartData(
                PlanType.WORKOUT,
                ques
            )
            navigate(frag, bundle)
        }
        binding.ivTextChat.setOnClickListener {
            if (ringDataStore.getRingDevice() == null) {
                context.showShortToast(getString(R.string.text_luna_ai_message))
                return@setOnClickListener
            }

            val ques = getWorkoutAiString(currentPos)
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = ques,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            navigate(frag, bundle)
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvNextWorkout.setOnClickListener {
            onNextClicked()
        }
        binding.ivNext.setOnClickListener {
            onNextClicked()
        }

    }

    private fun onNextClicked() {
        this.dataList.removeAt(0)
        setUI(navArgs.workoutType)
    }

    override fun subscribeObservers() {

        displayMessage.observe(this) {
            if (it.isNotEmpty()) {
                binding.tvMessages.text = it
            }
        }
    }

    private fun getWorkoutAiString(position: Int): String {
        return messagesStrings.getOrNull(position) ?: ""
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                currentPos += 1

                if (currentPos > (messagesStrings.size - 1)) {
                    currentPos = 0
                }
                displayMessage.postValue(getWorkoutAiString(currentPos))
                startTimer()
            }
        }
        timer?.start()
    }
}