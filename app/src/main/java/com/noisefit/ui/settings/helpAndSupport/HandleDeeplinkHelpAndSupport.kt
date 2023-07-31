package com.noisefit.ui.settings.helpAndSupport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.noisefit.R
import com.noisefit.databinding.LayoutWatchfaceDeeplinkBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragmentArgs


class HandleDeeplinkHelpAndSupport :
    BaseFragment<LayoutWatchfaceDeeplinkBinding>(LayoutWatchfaceDeeplinkBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //findNavController().popBackStack(R.id.handleDeeplinkHelpAndSupport, true)

        arguments?.let {
            try {
                //https://noisefit.page.link/HelpAndSupportDetail?id=1
                val deepLinkKey = it.keySet()?.firstOrNull()
                val uri = ((it.get(deepLinkKey) as Intent).data as Uri)
                val questionId = uri.getQueryParameter("id")

                if (questionId == null) navigateUpSafe()

                val parsedId = questionId?.toIntOrNull()

                if (parsedId != null) {
                    navigate(HandleDeeplinkHelpAndSupportDirections.actionHandleDeeplinkHelpAndSupportToHelpAndSupportDetailsFragment(
                        parsedId
                    ))
                } else {
                    navigateUpSafe()
                }

            } catch (exp: Exception) {

            }
        }


    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}