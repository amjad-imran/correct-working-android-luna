package com.noisefit.ui.npl


import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.R
import com.noisefit.databinding.FragmentNplPrivacyBottomDialogListDialogBinding
import com.noisefit.databinding.FragmentPrivacyBottomDialogListDialogBinding
import com.noisefit.ui.friends.location.search.CLOSED_SEARCH_STATE_KEY
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.ui.BaseBottomSheet
import com.noisefit_commans.ui.makeLinks
import com.noisefit_commans.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint

const val NPL_TERMS_KEY = "NPL_TERMS_KEY"
@AndroidEntryPoint
class NplPrivacyBottomDialogFragment : BaseBottomSheet<FragmentNplPrivacyBottomDialogListDialogBinding>(
    FragmentNplPrivacyBottomDialogListDialogBinding::inflate
) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPrivacy.text =
            getString(R.string.text_i_agree_with_npl_terms)
        binding.tvPrivacy.makeLinks(true,
            Pair("Terms & Conditions", View.OnClickListener {
                navigateUpSafe()
                activity?.let {
                    startActivity(
                        WebViewActivity.getStartIntent(
                            it,
                            getString(R.string.text_terms_and_conditions),
                            AppConstants.URL_NPL_TERMS_OF_USE
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
                NPL_TERMS_KEY,
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
        }
    }


}

