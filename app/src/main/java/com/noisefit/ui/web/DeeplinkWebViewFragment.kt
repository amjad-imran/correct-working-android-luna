package com.noisefit.ui.web

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.noisefit.databinding.FragmentDeeplinkWebViewBinding
import com.noisefit_commans.ui.BaseFragment


class DeeplinkWebViewFragment :
    BaseFragment<FragmentDeeplinkWebViewBinding>(FragmentDeeplinkWebViewBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.let {
            try {

                val deepLinkKey = it.keySet()?.firstOrNull()
                val uri = ((it.get(deepLinkKey) as Intent).data as Uri)
                val url = uri.getQueryParameter("link")

                if (url.isNullOrEmpty()){
                    navigateUpSafe()
                    return
                }

                activity?.let { activity ->
                    startActivity(
                        WebViewActivity.getStartIntent(
                            activity,
                            WebViewActivity.DEFAULT_TITLE,
                            url
                        )
                    )
                }
                navigateUpSafe()
            } catch (exp: Exception) {
                navigateUpSafe()
            }
        }


    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}