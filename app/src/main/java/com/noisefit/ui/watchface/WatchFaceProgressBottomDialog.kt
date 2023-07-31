package com.noisefit.ui.watchface

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetWatchFaceProgressBinding
import com.noisefit.watch.WatchForm
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadCircleCacheWithProgress
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WatchFaceProgressBottomDialog :
    BaseBottomSheetWithTransparent<BottomSheetWatchFaceProgressBinding>(
        BottomSheetWatchFaceProgressBinding::inflate
    ) {

    var title: String? = null
    var message: String? = null
    var typeText: String? = null
    var watchfaceImageUrl: String? = null
    var watchFaceName: String? = null
    var isCustom: Boolean = false


    @Inject
    lateinit var watchesSDK: WatchesSDK



    companion object {
        fun getInstance(
            title: String,
            message: String,
            typeText: String,
            watchfaceImageUrl: String?,
            watchFaceName: String?,
            isCustom: Boolean = false
        ): WatchFaceProgressBottomDialog {
            val data = Bundle()
            data.putString("title", title)
            data.putString("message", message)
            data.putString("typeText", typeText)
            data.putString("watchfaceImageUrl", watchfaceImageUrl)
            data.putString("watchFaceName", watchFaceName)
            data.putBoolean("isCustom", isCustom)
            return WatchFaceProgressBottomDialog().apply {
                arguments = data
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val screenType = watchesSDK.getWatchForm()

        this.title = arguments?.getString("title") ?: ""
        this.message = arguments?.getString("message") ?: ""
        this.typeText = arguments?.getString("typeText") ?: ""
        this.watchfaceImageUrl = arguments?.getString("watchfaceImageUrl") ?: ""
        this.watchFaceName = arguments?.getString("watchFaceName") ?: ""
        this.isCustom = arguments?.getBoolean("isCustom") ?: false

        binding.tvTitle.text = title
        binding.tvMessage.text = message
        binding.tvTypeText.text = typeText


        if(!watchfaceImageUrl.isNullOrEmpty()){
            binding.ivWatchFace.visible()
            /*if (isCustom) {
                binding.ivWatchFace.setImageURI(null)
                binding.ivWatchFace.setImageURI(Uri.parse(watchfaceImageUrl))
            } else {*/

                if(screenType== WatchForm.CIRCLE){
                    binding.ivWatchFace.loadCircleCacheWithProgress(binding.ivWatchFace.context, watchfaceImageUrl)
                }else{
                    binding.ivWatchFace.loadImageCacheWithProgress(binding.ivWatchFace.context, watchfaceImageUrl)
                }

               /* Glide.with(binding.ivWatchFace.context)
                    .load(watchfaceImageUrl)
                    .placeholder(R.drawable.ic_placeholder_watchface)
                    .error(R.drawable.ic_placeholder_watchface)
                    .into(binding.ivWatchFace)*/
            //}
        }else{
            binding.ivWatchFace.gone()
        }

        binding.tvWatchFaceName.text = watchFaceName

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    fun setProgress(progress: Int) {
        try {
            binding.pbUpdateProgress.progress = progress
            binding.tvProgress.text = "$progress%"
        } catch (exp: Exception) {
        }
    }


}