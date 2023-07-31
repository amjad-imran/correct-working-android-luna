package com.noisefit.ui.onboarding


import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentPrivacyBottomDialogListDialogBinding
import com.noisefit_commans.ui.BaseBottomSheet
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.makeLinks
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint


/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    PrivacyBottomDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */

const val PRIVACY_ACCEPTED_KEY = "PRIVACY_ACCEPTED_KEY"

@AndroidEntryPoint
class PrivacyBottomDialogFragment : BaseBottomSheet<FragmentPrivacyBottomDialogListDialogBinding>(
    FragmentPrivacyBottomDialogListDialogBinding::inflate
) {


    private var listener: PrivacyBottomInteractionListener? = null

    fun setPrivacyBottomInteractionListener(listener: PrivacyBottomInteractionListener) {
        this.listener = listener
    }

    companion object {

        fun newInstance(): PrivacyBottomDialogFragment =
            PrivacyBottomDialogFragment().apply {
                arguments = Bundle().apply {

                }
            }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPrivacy.text =
            getString(R.string.text_i_agree_with_noisefit_terms)
        binding.tvPrivacy.makeLinks(true,
            Pair("Terms & Conditions", View.OnClickListener {
                activity?.let {
                    startActivity(
                        WebViewActivity.getStartIntent(
                            it,
                            getString(R.string.text_terms_and_conditions),
                            AppConstants.URL_TERMS_OF_USE
                        )
                    )
                }

            }), Pair("Privacy Policy", View.OnClickListener {
                activity?.let {
                    startActivity(
                        WebViewActivity.getStartIntent(
                            it,
                            getString(R.string.text_privacy_policy),
                            AppConstants.URL_PRIVACY_POLICY
                        )
                    )
                }

            })
        )

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            // Code here
            if (isChecked) {
                binding.btnAgree.enable()
            } else {
                binding.btnAgree.disable()
            }
        }

        binding.btnAgree.setOnClickListener {
            listener?.onPrivacyStatus(true)
            dismiss()

        }


    }

    interface PrivacyBottomInteractionListener {
        fun onPrivacyStatus(accepted: Boolean)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState).apply {
//             window?.setDimAmount(0.6f) // Set dim amount here
            setOnShowListener {
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)
            }
        }
    }


}

