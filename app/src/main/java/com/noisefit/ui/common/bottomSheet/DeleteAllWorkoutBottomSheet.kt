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

const val DELETE_REQ_REQUEST_KEY = "DELETE_REQ_REQUEST_KEY"

@AndroidEntryPoint
class DeleteAllWorkoutBottomSheet :
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
            title = DeleteAllWorkoutBottomSheetArgs.fromBundle(it).title
            description = DeleteAllWorkoutBottomSheetArgs.fromBundle(it).description
            acceptText = DeleteAllWorkoutBottomSheetArgs.fromBundle(it).acceptText
            declineText = DeleteAllWorkoutBottomSheetArgs.fromBundle(it).declineText
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
            setFragmentResult(
                DELETE_REQ_REQUEST_KEY,
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
