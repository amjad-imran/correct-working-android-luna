package com.oreo.ui.femalehealth.cycletracker.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCycleTrackerSettingsBinding
import com.noisefit.luna.databinding.FragmentCycleTrackerSettingsNewBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.FemaleHealthCardState
import com.oreo.data.model.OHealthOverview
import com.oreo.ui.femalehealth.onboarding.GoalType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleTrackerSettingsNewFragment :
    BaseFragment<FragmentCycleTrackerSettingsNewBinding>(FragmentCycleTrackerSettingsNewBinding::inflate) {

    private val viewModel: CycleTrackerSettingNewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()
        viewModel.getPeriodTrackerStatus()
    }

    private fun updateUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_cycle_tracker)
    }


    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.sCycleTracker.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                viewModel.updatePeriodToggle(isChecked)
            }
        }


    }

    private fun setFemaleGetStartedUI() {
        binding.lytFemaleHealthGetStarted.apply {
            root.visible()
            ivCross.invisible()
            tvRemindMeLater.gone()
            lytDummyView.visible()
            this.btnGetStarted.text =
                this.btnGetStarted.context.getString(R.string.text_get_started)
            this.textView92.text =
                this.textView92.context.getString(R.string.text_track_your_cycle_desc)

            this.btnGetStarted.setOnClickListener {
                navigate(CycleTrackerSettingsNewFragmentDirections.actionCycleTrackerSettingsNewFragmentToFemaleHealthSplashFragment())
            }
        }
    }

    override fun subscribeObservers() {


        viewModel.cycleTrackerEnabled.observe(this) {
            if (it) {
                binding.sCycleTracker.isChecked = true
                setFemaleGetStartedUI()
            } else {
                binding.sCycleTracker.isChecked = false
                binding.lytFemaleHealthGetStarted.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

}