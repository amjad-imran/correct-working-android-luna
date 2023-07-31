package com.noisefit.ui.watchface2.bottom

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.databinding.BottomSheetWatch2DetailsBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

const val WF2_DETAILS_REQUEST_KEY = "WF2_DETAILS_REQUEST_KEY"

@AndroidEntryPoint
class Watchface2DetailsBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetWatch2DetailsBinding>(
        BottomSheetWatch2DetailsBinding::inflate
    ) {

    private val viewModel: Watchface2DetailsViewModel by viewModels()


    private val args: Watchface2DetailsBottomSheetArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.let {
            viewModel.watchFace2 = args.wfData
        }

        setData()
    }

    private fun setData() {
        viewModel.watchFace2?.let {

            binding.ivFavourite.isChecked = it.isFav()
            binding.tvTitle.text = it.name
            if (it.rating.isNullOrEmpty()) {
                binding.tvStarCount.gone()
                binding.imvRating.gone()
            } else {
                binding.tvStarCount.text = it.rating
            }
        }

    }

    override fun initListener() {
        binding.btnUpload.setOnClickListener {
            setFragmentResult(
                WF2_DETAILS_REQUEST_KEY,
                bundleOf("upload" to true)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                //  uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

}