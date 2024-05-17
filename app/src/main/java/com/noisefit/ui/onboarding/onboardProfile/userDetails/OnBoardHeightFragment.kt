package com.noisefit.ui.onboarding.onboardProfile.userDetails

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardHeightBinding
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit_commans.models.HeightUnitSystem
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.WheelAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnBoardHeightFragment :
    BaseFragment<FragmentOnBoardHeightBinding>(FragmentOnBoardHeightBinding::inflate) {
    private val viewModel: SetupProfileViewModel by activityViewModels()

    private val listDefaultPadding by lazy {
        viewModel.getPadding(requireContext())
    }

    private val heightAdapter by lazy {
        HeightSelectionAdapter()
    }

    private val wheelAdapter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_land_on_enter_height_page_visit)
        binding.lytOnBoardProgress.apply {
            pgBr.progress = 80
            tvCount.text = getString(R.string.text_4)
        }

        handleButton()
        setWheelPicker()
    }

    private fun setWheelPicker() {
        binding.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapter.data = viewModel.getHeightDataForPicker()
        wheelAdapter.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )
            Log.d("TAG", "get current item ${wheelAdapter.currentItemPosition}")
            viewModel.updateHeightIndex(wheelAdapter.currentItemPosition)
        }
        wheelAdapter.bind(binding.wheelPicker)
        wheelAdapter.selectedItemPosition = viewModel.getHeightIndex()
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnContinue.setOnClickListener {
            viewModel.saveUserInfoLocally()

//            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_ENTER_HEIGHT_CLICK)
            navigate(R.id.onBoardWeightFragment)
        }
        binding.btnMetric.setOnClickListener {
            if (viewModel.heightUnitSystem == HeightUnitSystem.METRIC) return@setOnClickListener
            viewModel.setHeightUnit(HeightUnitSystem.METRIC)
            handleButton()
            setWheelPicker()
        }
        binding.btnImperial.setOnClickListener {
            if (viewModel.heightUnitSystem == HeightUnitSystem.IMPERIAL) return@setOnClickListener
            viewModel.setHeightUnit(HeightUnitSystem.IMPERIAL)
            handleButton()
            setWheelPicker()
        }
    }

    private fun handleButton() {

        if (viewModel.heightUnitSystem == HeightUnitSystem.METRIC) {
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

    }
}

