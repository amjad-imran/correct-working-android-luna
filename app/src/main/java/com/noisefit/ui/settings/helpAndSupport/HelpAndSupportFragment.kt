package com.noisefit.ui.settings.helpAndSupport

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.response.HelpAndSupportResponse
import com.noisefit.databinding.FragmentHelpAndSupportBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.common.MarginSideItemDecoration
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HelpAndSupportFragment : BaseFragment<FragmentHelpAndSupportBinding>(
    FragmentHelpAndSupportBinding::inflate
), HelpAndSupportInteractionListener {

    private val viewModel: HelpAndSupportViewModel by viewModels()

    private val args: HelpAndSupportFragmentArgs by navArgs()

    private val helpAndSupportAdapter by lazy {
        HelpAndSupportAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.highlightTopic = args.highlightTopic

        viewModel.fetchHelpAndSupportData()
    }

    override fun initListener() {
        binding.toolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(com.noisefit_commans.R.string.text_help_and_support)
        }

        binding.tvPhoneModal.text = viewModel.deviceN
        binding.tvPhoneVersion.text = viewModel.osVersion
        setAdapter()
    }

    private fun setAdapter() {
        binding.rv.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = helpAndSupportAdapter
            addItemDecoration(MarginSideItemDecoration(24))
        }
    }

    override fun subscribeObservers() {
        viewModel.helpAndSupportResponseList.observe(this) {
            helpAndSupportAdapter.setDataSet(it)
            if (viewModel.highlightTopic != HelpAndSupportType.NONE) {
                helpAndSupportAdapter.highlightTopic(viewModel.highlightTopic)
                viewModel.highlightTopic = HelpAndSupportType.NONE
            }
        }
        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    override fun onHelpAndSupportClick(helpAndSupportResponse: HelpAndSupportResponse) {
        if (helpAndSupportResponse.type?.equals(
                HelpAndSupportType.RAISE_COMPLAINT.name,
                true
            ) == true
        ) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SUPPORT + "_" + helpAndSupportResponse.title.toString() + "_CLICK")
            navigate(R.id.feedbackFragment)
            return
        }

        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SUPPORT + "_" + helpAndSupportResponse.title.toString() + "_CLICK")
        navigate(
            HelpAndSupportFragmentDirections.actionNavigationHelpAndSupportToSupportListFragment(
                helpAndSupportResponse,
                null
            )
        )
    }


}

enum class HelpAndSupportType {
    PAIRING_AND_CONNECTIVITY, RAISE_COMPLAINT, NOTIFICATION, WATCHFACE_TRANSFER, BATTERY_AND_CHARGING,
    DATA_SYNC, BLUETOOTH_CALLING, HARDWARE_RELATED, CHALLENGES_AND_REWARDS, WATCH_AND_APP_UPDATE, NONE

}