package com.noisefit.ui.watchface2.bottom

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.databinding.BottomSheetUpdateWatchfaceBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.setIndicatorColor1
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.varunest.sparkbutton.SparkEventListener
import dagger.hilt.android.AndroidEntryPoint

const val WATCHFACE_UPLOAD_KEY = "WATCHFACE_UPLOAD_KEY"

@AndroidEntryPoint
class UploadWatchfaceBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetUpdateWatchfaceBinding>(
        BottomSheetUpdateWatchfaceBinding::inflate
    ) {

    private var uploadWatchfaceBottomSheetListener: UploadWatchfaceBottomSheetListener? = null

    companion object {
        fun getInstance(
            watchface: Watchface2,
            fromFav: Boolean
        ): UploadWatchfaceBottomSheet {
            val data = Bundle()
            data.putParcelable("watchFace", watchface)
            data.putBoolean("fromFav", fromFav)
            return UploadWatchfaceBottomSheet().apply {
                arguments = data
            }
        }
    }

    private val viewModel: WatchfaceMarkFavouriteModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.watchface = arguments?.getParcelable("watchFace")
        viewModel.fromFav = arguments?.getBoolean("fromFav") ?: false
        viewModel.watchface?.let {
            setData(it)
        }

        binding.layoutProgress.pbSteps.setIndicatorColor1(R.color.primary_btn_selected_color)

        setBSCancel(true)
    }

    private fun setBSCancel(state:Boolean){
        this.isCancelable = state
        this.dialog?.setCanceledOnTouchOutside(state)
    }
    fun showProgress(showProgress: Boolean) {
        if (showProgress) {
            setBSCancel(false)
            binding.btnUpload.gone()
            binding.ivFavouriteBack.invisible()
            binding.ivFavourite.invisible()
            binding.tvUploadingPerc.visible()
            binding.tvUploadingText.visible()
            binding.layoutProgress.root.visible()
        } else {
            setBSCancel(true)
            binding.btnUpload.visible()
            binding.ivFavourite.visible()
            binding.ivFavouriteBack.visible()
            binding.tvUploadingPerc.gone()
            binding.tvUploadingText.gone()
            binding.layoutProgress.root.gone()
        }
    }

    fun setState(state: String) {
        if (binding.tvUploadingText.visibility == View.GONE) {
            showProgress(true)
        }
        binding.tvUploadingText.text = state
    }

    fun setProgress(progress: Int) {
        tryCatch {
            binding.layoutProgress.pbSteps.progress = progress
            binding.tvUploadingPerc.text = "$progress%"
        }
    }

    private fun setData(watchFace: Watchface2) {
        binding.tvTitle.text = watchFace.name
        binding.bgImv.loadImageCacheWithProgress(binding.bgImv.context, watchFace.imageUrl)

        if (viewModel.fromFav) {
            binding.ivFavourite.invisible()
            binding.ivFavouriteBack.invisible()
        } else {
            binding.ivFavourite.isChecked = watchFace.isFav()
        }


        if (watchFace.rating.isNullOrEmpty()) {
            binding.imvRating.gone()
            binding.tvStarCount.gone()
        } else {
            binding.tvStarCount.text = watchFace.rating
            binding.imvRating.visible()
            binding.tvStarCount.visible()
        }

        binding.ivFavourite.setEventListener(object : SparkEventListener {
            override fun onEvent(button: ImageView?, buttonState: Boolean) {
                viewModel.markFavourite(buttonState, watchFace)
            }

            override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {}

            override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {}

        })

    }

    override fun initListener() {
        binding.btnUpload.setOnClickListener {
            uploadWatchfaceBottomSheetListener?.startWatchfaceService()
            showProgress(true)
        }

    }


    override fun subscribeObservers() {
        viewModel.favMarked.observe(this){
            it?.getContent()?.let {
                uploadWatchfaceBottomSheetListener?.favMark(binding.ivFavourite.isChecked)
//                setFragmentResult(
//                    WATCHFACE_UPLOAD_KEY,
//                    bundleOf("favMark" to binding.ivFavourite.isChecked.toString())
//                )
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let {
                context.showShortToast(getString(R.string.text_something_went_wrong))
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }



    fun setUploadWatchfaceBottomSheetListen(uploadWatchfaceBottomSheetListener: UploadWatchfaceBottomSheetListener) {
        this.uploadWatchfaceBottomSheetListener = uploadWatchfaceBottomSheetListener
    }

}

interface UploadWatchfaceBottomSheetListener {
    fun startWatchfaceService()
    fun favMark(isFav: Boolean)

}