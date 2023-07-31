package com.noisefit.ui.settings.help

import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentFitnessHealthBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FitnessHealthFragment :
    BaseFragment<FragmentFitnessHealthBinding>(FragmentFitnessHealthBinding::inflate) {
    private val viewModel: FitnessHealthViewModel by viewModels()

    override fun initListener() {
        binding.layoutToolbar.tvTitle.text = getString(R.string.text_health_fitness)
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.rowHeartRate.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_HEART_RATE_PAGE_VISIT)
            navigate(
                FitnessHealthFragmentDirections.actionNavigationActivityToFitnessHealthDetailsFragment(
                    getString(R.string.text_heart_rate_updated),
                    viewModel.tempHeartRateData().toTypedArray()
                )
            )

        }
        binding.rowSleep.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_SLEEP_PAGE_VISIT)
            navigate(
                FitnessHealthFragmentDirections.actionNavigationActivityToFitnessHealthDetailsFragment(
                    getString(R.string.text_sleep),
                    viewModel.sleepData().toTypedArray()
                )
            )
        }
        binding.rowBloodOxygen.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_BLOOD_OXYGEN_PAGE_VISIT)
            navigate(
                FitnessHealthFragmentDirections.actionNavigationActivityToFitnessHealthDetailsFragment(
                    getString(R.string.text_blood_oxygen_title),
                    viewModel.bloodOxygenData().toTypedArray()
                )
            )
        }
        binding.rowStress.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_STRESS_PAGE_VISIT)
            navigate(
                FitnessHealthFragmentDirections.actionNavigationActivityToFitnessHealthDetailsFragment(
                    getString(R.string.text_stress),
                    viewModel.stressData().toTypedArray()
                )
            )
        }
        binding.rowSteps.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_STEPS_PAGE_VISIT)
            navigate(
                FitnessHealthFragmentDirections.actionNavigationActivityToFitnessHealthDetailsFragment(
                    getString(R.string.text_steps),
                    viewModel.stepData().toTypedArray()
                )
            )
        }
        binding.rowDistance.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_DISTANCE_PAGE_VISIT)
            navigate(
                FitnessHealthFragmentDirections.actionNavigationActivityToFitnessHealthDetailsFragment(
                    getString(R.string.text_distance),
                    viewModel.distanceData().toTypedArray()
                )
            )
        }
        binding.rowTemperature.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_BODY_TEMPREATURE_PAGE_VISIT)
            navigate(
                FitnessHealthFragmentDirections.actionNavigationActivityToFitnessHealthDetailsFragment(
                    getString(R.string.text_body_temperature),
                    viewModel.bodyTemperatureData().toTypedArray()
                )
            )
        }
    }

    override fun subscribeObservers() {

    }


}