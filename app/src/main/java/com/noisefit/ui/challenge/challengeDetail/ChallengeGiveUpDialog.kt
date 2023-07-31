package com.noisefit.ui.challenge.challengeDetail

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.DialogChallengeGiveUpBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val LEAVE_COMMENT = "leaveComment"

@AndroidEntryPoint
class ChallengeGiveUpDialog :
    BaseBottomSheetWithTransparent<DialogChallengeGiveUpBinding>(DialogChallengeGiveUpBinding::inflate) {

    var firstState = true

    val args: ChallengeGiveUpDialogArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvLeaveChallengeText.text =
            getString(R.string.text_leave_challenge_message, args.eventName)


        binding.bCancel.setOnClickListener {
            dismiss()
        }
        binding.bLeave.setOnClickListener {
            /*if (firstState) {
                binding.textView28.text = "Feedback"
                binding.tvLeaveChallengeText.gone()
                binding.etFeedback.visible()
                binding.bLeave.text = "Give Up"
                firstState = false
                return@setOnClickListener
            }*/

            dismiss()
            setFragmentResult(
                LEAVE_COMMENT,
                bundleOf("comment" to binding.etFeedback.text.toString())
            )

        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}