package com.noisefit.ui.onboarding.onboardProfile.userDetails

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardWeightBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.onboarding.onboardProfile.GuestProfileSetupActivity
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit.ui.onboarding.pairing.DeviceSetupActivity
import com.noisefit_commans.models.WeightUnitSystem
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.WheelAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardWeightFragment :
    BaseFragment<FragmentOnBoardWeightBinding>(FragmentOnBoardWeightBinding::inflate) {

    private val viewModel: SetupProfileViewModel by activityViewModels()

    private val listDefaultPadding by lazy {
        viewModel.getPadding(requireContext())
    }
    private val weightAdapter by lazy {
        WeightSelectionAdapter()
    }
    private val wheelAdapter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.LAND_ON_ENTER_WEIGHT_PAGE_VISIT)
        binding.lytOnBoardProgress.apply {
            pgBr.progress = 100
            tvCount.text = getString(R.string.text_5)
        }

        viewModel.initialWeightUnit()
        handleButton()
        setWeightPicker()
    }

    private fun setWeightPicker() {
        binding.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapter.data = viewModel.getWeightDataForPicker()
        wheelAdapter.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )
            Log.d("TAG", "get current item ${wheelAdapter.currentItemPosition}")
            viewModel.updateWeightIndex(wheelAdapter.currentItemPosition)
            wheelAdapter.selectedItemPosition = viewModel.getWeightIndex()
        }
        wheelAdapter.bind(binding.wheelPicker)
        wheelAdapter.selectedItemPosition = viewModel.getWeightIndex()
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnContinue.setOnClickListener {
            viewModel.saveUserInfoLocally()
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_ENTER_WEIGHT_CLICK)
            //navigate(R.id.onBoardStepsGoalFragment)

            viewModel.updateUserProfile()

        }

        binding.btnMetric.setOnClickListener {
            viewModel.setWeightUnit(WeightUnitSystem.METRIC)
            handleButton()
            setWeightPicker()
        }
        binding.btnImperial.setOnClickListener {
            viewModel.setWeightUnit(WeightUnitSystem.IMPERIAL)
            handleButton()
            setWeightPicker()
        }

    }

    private fun handleButton() {
        if (viewModel.weightUnitSystem == WeightUnitSystem.METRIC) {
            binding.btnMetric.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.back_primary_button)
            binding.btnImperial.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.rounded_corner)
        } else {
            binding.btnImperial.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.back_primary_button)
            binding.btnMetric.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.rounded_corner)
        }
    }


    override fun subscribeObservers() {
        viewModel.successMessage.observe(this) {
            it.getContent()?.let {
                val openProfile = activity is GuestProfileSetupActivity
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_ENTER_DAILY_GOAL_CLICK)
                if (viewModel.isDevicePaired()) {
                    goToDeviceSetupActivity(openProfile)
                } else {
                    startActivity(OreoMainActivity.getStartIntent(requireContext()))
                    activity?.finish()
                }
            }
        }
    }

    private fun goToDeviceSetupActivity(openProfile: Boolean) {
        startActivity(DeviceSetupActivity.getStartIntent(requireContext(), openProfile))
        activity?.finish()
    }
}
