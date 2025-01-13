package com.oreo.ui.googlefit

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentGoogleFitDataSyncedBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleFitDataSyncedFragment :
    BaseFragment<FragmentGoogleFitDataSyncedBinding>(FragmentGoogleFitDataSyncedBinding::inflate) {
    private val args: GoogleFitDataSyncedFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isSuccess = args.isSuccess
        val syncMessage = args.syncTitle
        val message = args.syncMessage

        setUI(isSuccess, syncMessage, message)
    }

    private fun setUI(state: Boolean, syncMessage: String, message: String) {
        binding.tvSyncMessage.text = syncMessage
        if (state) {
            binding.tvMessage.text = message
            binding.tvMessage.visible()
            binding.image.setImageResource(R.drawable.ic_g_fit_success)
        } else {
            binding.tvMessage.gone()
            binding.image.setImageResource(R.drawable.ic_g_fit_failed)
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}