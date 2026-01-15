package com.oreo.ui.lifeos.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.setFragmentResult
import com.noisefit.data.local.dataStored.implementation.DataStoredImpl
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsOnboardBeginBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LifeOsOnboardBeginFragment : BaseFragment<FragmentLifeOsOnboardBeginBinding>(FragmentLifeOsOnboardBeginBinding::inflate) {

    @Inject
    lateinit var localStoredImpl: DataStoredImpl

    companion object{
        const val LIFE_OS_ONBOARD_BEGIN_KEY = "LIFE_OS_ONBOARD_BEGIN_KEY"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                onBackPress()
            }

        setUi()
    }

    private fun setUi() {
        // lytLogYourMeals
        binding.lytLogYourMeals.apply {
            ivIcon.setImageResource(R.drawable.ic_lifeos_log_your_meals)
            tvText.text =
                getString(R.string.text_log_your_meals_or_click_a_photo_to_learn_more)
        }

        // lytBiomarkers
        binding.lytBiomarkers.apply {
            ivIcon.setImageResource(R.drawable.ic_lifeos_dive_biomarkers)
            tvText.text =
                getString(R.string.text_deep_dive_with_your_luna_data_and_learn_trends)
        }

        // lytEffectsMoodAndSleep
        binding.lytEffectsMoodAndSleep.apply {
            ivIcon.setImageResource(R.drawable.ic_lifeos_share_your_medications)
            tvText.text =
                getString(R.string.text_share_your_medical_reports_or_log_your_medications)
        }

        // lytHealthAssistant
        binding.lytHealthAssistant.apply {
            ivIcon.setImageResource(R.drawable.ic_lifeos_24x7_assistant)
            tvText.text =
                getString(R.string.text_ask_anything_about_your_health_privately_and_safely)
        }
    }

    override fun initListener() {
        binding.btnBegin.setOnClickListener {
            localStoredImpl.setLifeOsOnboardInitiated(true)
            navigateUpSafe()
            navigate(R.id.lifeOsOnboardingQuesFragment)
        }

        binding.ivBackBtn.setOnClickListener {
            onBackPress()
        }
    }

    private fun onBackPress(){
        setFragmentResult(
            LIFE_OS_ONBOARD_BEGIN_KEY,
            Bundle().apply {
                putBoolean("isBackClicked", true)
            }
        )
        navigateUpSafe()
    }

    override fun subscribeObservers() {

    }


}