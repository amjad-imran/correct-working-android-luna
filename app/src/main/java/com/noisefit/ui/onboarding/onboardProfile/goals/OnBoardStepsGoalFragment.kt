package com.noisefit.ui.onboarding.onboardProfile.goals

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardStepsGoalBinding
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.numberFormatter
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.WheelAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnBoardStepsGoalFragment :
    BaseFragment<FragmentOnBoardStepsGoalBinding>(FragmentOnBoardStepsGoalBinding::inflate) {

    private val viewModel: SetupProfileViewModel by activityViewModels()

    private val wheelAdapter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.LAND_ON_DAILY_GOAL_SET_PAGE_VISIT)
        binding.lytOnBoardProgress.apply {
            pgBr.progress = 84
            tvCount.text = getString(R.string.text_7)
        }

        val msg = ApplicationUtils.bmiCalculate(
            viewModel.getHeight().toFloat(),
            viewModel.getWeight().toFloat(),
            viewModel.weightUnitSystem.type,
            viewModel.heightUnitSystem.type
        )
        val formattedStepsValue = if (msg.second >= 10000) {
            msg.second.numberFormatter()
        } else msg.second

        binding.tvSubHeading.text =
            "According to your BMI, ${msg.first} we suggest you to walk $formattedStepsValue steps in a day."
        viewModel.setStepsDefaultGoal(msg.second.toInt())

        setGoalsPicker()
    }

    private fun setGoalsPicker() {
        binding.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapter.data = viewModel.getGoalsDataForPicker()
        wheelAdapter.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )
            Log.d("TAG", "get current item ${wheelAdapter.currentItemPosition}")
            viewModel.updateStepsIndex(wheelAdapter.currentItemPosition)
            wheelAdapter.selectedItemPosition = viewModel.getStepsIndex()
        }
        wheelAdapter.bind(binding.wheelPicker)
        wheelAdapter.selectedItemPosition = viewModel.getStepsIndex()
    }


    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnContinue.setOnClickListener {
//            viewModel.updateUserProfile()
            navigate(R.id.onBoardCaloriesFragment)
        }
    }

    override fun subscribeObservers() {
//        viewModel.successMessage.observe(this) {
//            it.getContent()?.let {
//
//                val openProfile = activity is GuestProfileSetupActivity
//                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_ENTER_DAILY_GOAL_CLICK)
//                if (viewModel.isDevicePaired()) {
//                    goToDeviceSetupActivity(openProfile)
//
//                } else {
//                    if (viewModel.localDataStore.getPairDeviceType() == Device.RING) {
//                        startActivity(OreoMainActivity.getStartIntent(requireContext()))
//                        activity?.finish()
//                    }
//                    else{
//                        startActivity(MainActivity.getStartIntent(requireContext(), openProfile))
//                        activity?.finish()
//                    }
//                }
//            }
//        }
//
//        viewModel.getMessages().observe(this) {
//            it.getContent()?.let { message ->
//                context.showShortToast(message)
//            }
//        }
//
//        viewModel.getApiErrors().observe(this) {
//            it?.getContent()?.let { response ->
//                uiController.onApiErrorReceived(response)
//            }
//        }
//
//        viewModel.getLoading().observe(this) {
//            uiController.displayProgressBar(it, "")
//        }
    }

//    private fun goToDeviceSetupActivity(openProfile: Boolean) {
//        startActivity(DeviceSetupActivity.getStartIntent(requireContext(), openProfile))
//        activity?.finish()
//    }
}
