package com.oreo.ui.chatGpt.functions

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.data.model.AiWorkout
import com.noisefit.luna.databinding.FragmentAiWorkoutDetailBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiWorkoutDetailFragment :
    BaseFragment<FragmentAiWorkoutDetailBinding>(FragmentAiWorkoutDetailBinding::inflate) {

    val navArgs: AiWorkoutDetailFragmentArgs by navArgs()

    var dataList = ArrayList<AiWorkout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataList = navArgs.data.toMutableList()

        this.dataList.clear()
        this.dataList.addAll(dataList)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
    }

    private fun setUI() {
        val workout = dataList.first()

        binding.tvWorkoutName.text = workout.workout_name
        binding.tvSetsData.text = workout.reps
        binding.tvDescription.text = workout.description

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
            val workout = dataList.first()
            val (frag, bundle) = AudioAiFragment.getStartData(
                PlanType.WORKOUT,
                workout.workout_name
            )
            navigate(frag, bundle)
        }
        binding.ivTextChat.setOnClickListener {
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                null,
                null,
                AITopics.GENERAL,
                workout = dataList.first()
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
        setUI()
    }

    override fun subscribeObservers() {

    }
}