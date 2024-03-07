package com.noisefit.ui.onboarding.onboardProfile.calories

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardCaloriesBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.onboarding.onboardProfile.GuestProfileSetupActivity
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit.ui.onboarding.pairing.DeviceSetupActivity
import com.noisefit.ui.onboarding.setup.DeviceSetupActivityV2
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.WheelAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardCaloriesFragment :
    BaseFragment<FragmentOnBoardCaloriesBinding>(FragmentOnBoardCaloriesBinding::inflate) {
    private val viewModel: SetupProfileViewModel by activityViewModels()

    private val wheelAdapter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytOnBoardProgress.apply {
            pgBr.progress = 100
            tvCount.text = getString(R.string.text_8)
        }

        val bmrValue = ApplicationUtils.bmrCalculate(
            viewModel.getHeight().toFloat(),
            viewModel.getWeight().toFloat(),
            viewModel.weightUnitSystem.type,
            viewModel.heightUnitSystem.type,
            viewModel.getAge(),
            viewModel.gender.value!!
        )

        LOGS.d("Calories  $bmrValue")

        binding.tvSubHeading.text =
            "According to your BMR, we suggest you to burn minimum $bmrValue kcal in a day."
        viewModel.setCaloriesDefault(bmrValue)

        setCaloriesPicker()

    }

    private fun setCaloriesPicker() {
        binding.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapter.data = viewModel.getCaloriesDataForPicker()
        wheelAdapter.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )
            Log.d("TAG", "get current item ${wheelAdapter.currentItemPosition}")
            viewModel.updateCaloriesIndex(wheelAdapter.currentItemPosition)
            wheelAdapter.selectedItemPosition = viewModel.getCaloriesIndex()
        }
        wheelAdapter.bind(binding.wheelPicker)
        wheelAdapter.selectedItemPosition = viewModel.getCaloriesIndex()
    }


    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnContinue.setOnClickListener {
            viewModel.updateUserProfile()
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

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
    }

    private fun goToDeviceSetupActivity(openProfile: Boolean) {
        startActivity(DeviceSetupActivityV2.getStartIntent(requireContext()))
        activity?.finish()
    }
}