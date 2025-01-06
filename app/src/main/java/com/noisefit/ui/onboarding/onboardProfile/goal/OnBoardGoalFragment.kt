package com.noisefit.ui.onboarding.onboardProfile.goal

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardGoalBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.onboarding.onboardProfile.GuestProfileSetupActivity
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit.ui.onboarding.setup.DeviceSetupActivityV2
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardGoalFragment :
    BaseFragment<FragmentOnBoardGoalBinding>(FragmentOnBoardGoalBinding::inflate) {

    private val viewModel: SetupProfileViewModel by activityViewModels()

    private val adapter: GoalAdapter by lazy {
        GoalAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.resetGoalsData()

        binding.lytOnBoardProgress.apply {
            pgBr.progress = viewModel.getProgress(6)
            tvCount.text = getString(R.string.text_6)
        }
        setRecycler()
        viewModel.getUserGoalsList()
    }

    private fun setRecycler() {
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = adapter
    }

    override fun initListener() {
        binding.lytOther.lytOther.root.setOnClickListener {
            if (viewModel.goalOtherSelected) {
                binding.lytOther.apply {
                    lytOther.ivCheck.setImageResource(R.drawable.ic_goal_uncheck)
                    lytOtherEdit.gone()
                    etOther.setText("")
                }
                uiController.hideSoftKeyboard()
            } else {
                binding.lytOther.lytOther.ivCheck.setImageResource(R.drawable.ic_goal_check)
                binding.lytOther.lytOtherEdit.visible()
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, binding.scrollView.bottom)
                }
            }
            viewModel.goalOtherSelected = viewModel.goalOtherSelected.not()
        }

        binding.lytOther.etOther.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.goalOtherText = text.toString()
        })

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnContinue.setOnClickListener {

            val otherText = if (viewModel.goalOtherSelected) {
                viewModel.goalOtherText
            } else {
                null
            }
            viewModel.selectedGoalsKeys.clear()
            viewModel.selectedGoalsKeys.addAll(adapter.getSelectedGoals().map {
                it.key!!
            })

            if (otherText == null && viewModel.selectedGoalsKeys.isEmpty()) {
                return@setOnClickListener
            }

            viewModel.updateUserProfile()
        }

    }

    override fun subscribeObservers() {

        viewModel.goalsList.observe(this) {
            adapter.setDataSet(it)
            binding.lytOther.lytOther.tvName.text = getString(R.string.text_other)
            binding.lytOther.lytOther.ivCheck.setImageResource(
                if (viewModel.goalOtherSelected) R.drawable.ic_goal_check else R.drawable.ic_goal_uncheck
            )
            binding.lytListData.visible()
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }


        viewModel.successMessage.observe(this) {
            it.getContent()?.let {
                val openProfile = activity is GuestProfileSetupActivity
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_profile_successful_setup)
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
        startActivity(
            DeviceSetupActivityV2.getStartIntent(
                requireContext(), fullSetup = true
            )
        )
        activity?.finish()
    }


}