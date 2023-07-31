package com.noisefit.ui.watchface2.bottom

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.imageview.ShapeableImageView
import com.noisefit.R
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.databinding.BottomSheetMyCreationDetailsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setIndicatorColor1
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.util.FilterUtils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyDiyCreationDetailBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetMyCreationDetailsBinding>(
        BottomSheetMyCreationDetailsBinding::inflate
    ) {

    private val viewModel: Watchface2DetailsViewModel by viewModels()

    private var myDiyCreationDetailBottomSheetListener: MyDiyCreationDetailBottomSheetListener? =
        null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.diyMyCreation = arguments?.getParcelable("diyMyCreation")
        viewModel.showProgress = arguments?.getBoolean("showProgress") ?: false
        binding.layoutProgress.pbSteps.setIndicatorColor1(R.color.primary_btn_selected_color)

        setData()
        showProgress()
    }


    fun setFilter(
        data: DiyMyCreation,
        bgImv: ShapeableImageView,
        link: String?,
        isTextLayer: Boolean,

        ) {

        Glide.with(bgImv.context)
            .asBitmap()
            .load(link)
            .apply(
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(viewModel.widthHeight.first, viewModel.widthHeight.second)
                    .dontTransform()
            )
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {

                    if (!resource.isRecycled) {
                        val rawBitmap = Bitmap.createBitmap(resource)
                        if (isTextLayer) {
                            data.textBitmap = rawBitmap
                            ApplicationUtils.loadImage(bgImv, data.textBitmap, viewModel.screenType)
                        } else {

                            if (data.filter.isNotEmpty()) {

                                val paint = Paint()
                                paint.colorFilter =
                                    FilterUtils().getFilter(
                                        data.filter,
                                        data.filterIntensity
                                    )?.let {
                                        ColorMatrixColorFilter(
                                            it
                                        )
                                    }
                                val canvas = Canvas(rawBitmap)
                                canvas.drawBitmap(rawBitmap, 0f, 0f, paint)
                                data.bgBitmap = rawBitmap

                            } else {
                                data.bgBitmap = rawBitmap
                            }

                            ApplicationUtils.loadImage(bgImv, data.bgBitmap, viewModel.screenType)

                        }
                    } else {
                        // The bitmap is recycled, so we need to load it again before displaying it
                        setFilter(data, bgImv, link, isTextLayer)
                    }


                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })


    }

    fun setState(state: String) {
        if (binding.tvUploadingText.visibility == View.GONE) {
            viewModel.showProgress = true
            showProgress()
        }
        binding.tvUploadingText.text = state
    }

    private fun setBSCancel(state: Boolean) {
        this.isCancelable = state
        this.dialog?.setCanceledOnTouchOutside(state)
    }

    private fun showProgress() {
        if (viewModel.showProgress) {
            setBSCancel(false)
            binding.btnUpload.gone()
            binding.btnEdit.gone()
            binding.tvUploadingPerc.visible()
            binding.tvUploadingText.visible()
            binding.layoutProgress.root.visible()
        } else {
            setBSCancel(true)
            binding.btnUpload.visible()
            binding.btnEdit.visible()
            binding.tvUploadingPerc.gone()
            binding.tvUploadingText.gone()
            binding.layoutProgress.root.gone()
        }
    }
    private fun setData() {
        viewModel.diyMyCreation?.let { data ->

            if (data.backgroundUrl.isNotEmpty()) {
                setFilter(data, binding.bgImv, data.backgroundUrl, false)
            }
            if (data.textLayerLink.isNotEmpty()) {
                binding.bgLayerImv.visible()
                setFilter(
                    data,
                    binding.bgLayerImv,
                    data.textLayerLink,
                    true
                )
            } else {
                binding.bgLayerImv.gone()
            }

            if (data.colour.isEmpty()) {
                binding.bgLayerImv.setColorFilter(Color.argb(255, 255, 255, 255))//WHITE
            } else {
                binding.bgLayerImv.setColorFilter(Color.parseColor(data.colour))
            }

        }

    }

    override fun initListener() {
        binding.btnUpload.setOnClickListener {
            myDiyCreationDetailBottomSheetListener?.startWatchfaceService()
            viewModel.showProgress = true
            showProgress()
        }

        binding.btnEdit.setOnClickListener {
            dismiss()
            myDiyCreationDetailBottomSheetListener?.editWatchFace()

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

    fun setProgress(progress: Int) {
        try {
            binding.layoutProgress.pbSteps.progress = progress
            binding.tvUploadingPerc.text = "$progress%"
        } catch (exp: Exception) {
        }
    }

    companion object {
        fun getInstance(
            diyMyCreation: DiyMyCreation,
            showProgress: Boolean
        ): MyDiyCreationDetailBottomSheet {
            val data = Bundle()
            data.putParcelable("diyMyCreation", diyMyCreation)
            data.putBoolean("showProgress", showProgress)
            return MyDiyCreationDetailBottomSheet().apply {
                arguments = data
            }
        }
    }

    fun setMyDiyCreationDetailBottomSheetList(myDiyCreationDetailBottomSheetListener: MyDiyCreationDetailBottomSheetListener) {
        this.myDiyCreationDetailBottomSheetListener = myDiyCreationDetailBottomSheetListener
    }
}

interface MyDiyCreationDetailBottomSheetListener {
    fun startWatchfaceService()
    fun editWatchFace()

}