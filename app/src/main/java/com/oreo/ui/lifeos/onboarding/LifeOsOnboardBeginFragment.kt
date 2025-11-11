package com.oreo.ui.lifeos.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsOnboardBeginBinding
import com.noisefit_commans.ui.BaseFragment

class LifeOsOnboardBeginFragment : BaseFragment<FragmentLifeOsOnboardBeginBinding>(FragmentLifeOsOnboardBeginBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {

            }

        setUi()
    }

    private fun setUi() {
        // lytLogYourMeals
        binding.lytLogYourMeals.apply {
            ivIcon.setImageResource(R.drawable.ic_lifeos_log_your_meals)
            tvText.text =
                getString(R.string.text_log_your_meals_to_learn_what_helps_and_what_harms_recovery)
        }

        // lytBiomarkers
        binding.lytBiomarkers.apply {
            ivIcon.setImageResource(R.drawable.ic_lifeos_dive_biomarkers)
            tvText.text =
                getString(R.string.text_dive_deep_with_your_biomarkers)
        }

        // lytEffectsMoodAndSleep
        binding.lytEffectsMoodAndSleep.apply {
            ivIcon.setImageResource(R.drawable.ic_lifeos_affects_mood_sleep)
            tvText.text =
                getString(R.string.text_learn_how_exercise_affects_your_mood_and_sleep)
        }

        // lytHealthAssistant
        binding.lytHealthAssistant.apply {
            ivIcon.setImageResource(R.drawable.ic_lifeos_24x7_assistant)
            tvText.text =
                getString(R.string.text_your_personal_health_assistant_with_you_24x7)
        }
    }

    override fun initListener() {
        binding.btnBegin.setOnClickListener {
            navigateUpSafe()
            navigate(R.id.lifeOsOnboardingQuesFragment)
        }
    }

    override fun subscribeObservers() {

    }


}