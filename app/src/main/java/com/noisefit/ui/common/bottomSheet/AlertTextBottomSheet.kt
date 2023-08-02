package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetAlertTextBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import dagger.hilt.android.AndroidEntryPoint

const val ALERT_REQUEST_KEY = "ALERT_REQUEST_KEY"

@AndroidEntryPoint
class AlertTextBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetAlertTextBinding>(
        BottomSheetAlertTextBinding::inflate
    ) {


    private var title: String = ""
    private var description: String = ""

    private var acceptText: String? = null
    private var declineText: String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            title = AlertTextBottomSheetArgs.fromBundle(it).title
            description = AlertTextBottomSheetArgs.fromBundle(it).description
            acceptText = AlertTextBottomSheetArgs.fromBundle(it).acceptText
            declineText = AlertTextBottomSheetArgs.fromBundle(it).declineText
        }

        binding.tvTitle.text = title
        binding.tvDesc.text = description

        handleView()
        if (!acceptText.isNullOrEmpty()) {
            binding.btnAllow.text = acceptText
        }
        if (!declineText.isNullOrEmpty()) {
            binding.btnCancel.text = declineText
        }
    }

    private fun handleView() {
        if (title == getString(R.string.text_dismiss_activity_title)) {
            binding.btnAllow.text = getString(R.string.text_remove)
            binding.btnCancel.text = getString(R.string.text_keep)
        } else if (title == getString(R.string.text_automatic_activity_detection)) {
            binding.btnAllow.text = getString(R.string.text_dismiss)
            binding.btnCancel.gone()
        }
    }


    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                ALERT_REQUEST_KEY, bundleOf("allow" to true)
            )
            setFragmentResult(
                ALERT_REQUEST_KEY,
                bundleOf("allow" to true)
            )
            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

}
