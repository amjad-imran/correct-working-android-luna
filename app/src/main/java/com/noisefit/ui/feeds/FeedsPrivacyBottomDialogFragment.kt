package com.noisefit.ui.feeds


import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentNplPrivacyBottomDialogListDialogBinding
import com.noisefit_commans.ui.BaseBottomSheet
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.ui.makeLinks
import com.noisefit_commans.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint

const val FEEDS_TERMS_KEY = "FEEDS_TERMS_KEY"

@AndroidEntryPoint
class FeedsPrivacyBottomDialogFragment :
    BaseBottomSheet<FragmentNplPrivacyBottomDialogListDialogBinding>(
        FragmentNplPrivacyBottomDialogListDialogBinding::inflate
    ) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView3.text = getString(R.string.text_noisefit_community_guidelines)

        binding.tvPrivacy.text =
            getString(R.string.text_i_agree_with_feeds_terms)
        binding.tvPrivacy.makeLinks(true,
            Pair("Community guidelines", View.OnClickListener {
                navigateUpSafe()
                activity?.let {
                    startActivity(
                        WebViewActivity.getStartIntent(
                            it,
                            getString(R.string.text_community_guidelines),
                            AppConstants.URL_FEEDS_GUIDELINES
                        )
                    )
                }

            }), Pair("Privacy Policy", View.OnClickListener {
                navigateUpSafe()
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



        binding.btnAgree.setOnClickListener {
            navigateUpSafe()
            requireActivity().supportFragmentManager.setFragmentResult(
                FEEDS_TERMS_KEY,
                bundleOf("agree" to true)
            )
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState).apply {
//             window?.setDimAmount(0.6f) // Set dim amount here
            setOnShowListener {
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)
            }
            isCancelable = false
        }
    }


}

