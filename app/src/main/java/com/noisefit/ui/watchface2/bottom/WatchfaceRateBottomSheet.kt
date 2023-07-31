package com.noisefit.ui.watchface2.bottom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.luna.databinding.BottomSheetRateWachFaceBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WatchfaceRateBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetRateWachFaceBinding>(
        BottomSheetRateWachFaceBinding::inflate
    ) {

    private val viewModel: WatchfaceMarkFavouriteModel by viewModels()
    private var watchFaceOpenType: String? = null

    var watchface: Watchface2? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        watchface = arguments?.getParcelable("watchFace")
        watchFaceOpenType = arguments?.getString("watchFaceOpenType")

        watchface?.let {
            setData(it)
        }
        setBSCancel(false)
    }

    private fun setBSCancel(state: Boolean) {
        this.isCancelable = state
        this.dialog?.setCanceledOnTouchOutside(state)
    }

    private fun setData(watchFace: Watchface2) {
        binding.tvTitle.text = "Hey, how does it look?"
        binding.bgImv.loadImageCacheWithProgress(binding.bgImv.context, watchFace.imageUrl)


    }

    override fun initListener() {
        binding.btnCancel.setOnClickListener {
            if (watchFaceOpenType == "category")
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_CATEGORY_RATING_NOTHANKYOU)
            else
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_RATING_NOTHANKYOU)
            navigateUpSafe()

        }
        binding.btnSubmit.setOnClickListener {
            if (viewModel.rating != -1 && watchface != null) {
                val eventName: String = if (watchFaceOpenType == "category")
                    InsiderAppEvents.WATCHFACES_CATEGORY_RATING_SUBMIT
                else
                    InsiderAppEvents.WATCHFACES_RATING_SUBMIT
                viewModel.sessionManager.logInsiderAppEvent(
                    eventName,
                    HashMap<String, Any>().apply {
                        this["rating"] = viewModel.rating
                    })
                viewModel.rateWatchFace(viewModel.rating, watchface!!)
                navigateUpSafe()
            } else {
                //TODO show message here
            }
        }

        binding.lvAnimFirst.setOnClickListener {
            binding.btnSubmit.enable()
            viewModel.rating = 1
            handleEmoji(1)
            handleInsiderEvent()
        }
        binding.lvAnimSecond.setOnClickListener {
            binding.btnSubmit.enable()
            viewModel.rating = 2
            handleEmoji(2)
            handleInsiderEvent()
        }
        binding.lvAnimThird.setOnClickListener {
            binding.btnSubmit.enable()
            viewModel.rating = 3
            handleEmoji(3)
            handleInsiderEvent()
        }
        binding.lvAnimFourth.setOnClickListener {
            binding.btnSubmit.enable()
            viewModel.rating = 4
            handleEmoji(4)
            handleInsiderEvent()
        }
        binding.lvAnimFifth.setOnClickListener {
            binding.btnSubmit.enable()
            viewModel.rating = 5
            handleEmoji(5)
            handleInsiderEvent()
        }

    }

    private fun handleInsiderEvent() {
        val eventName: String = if (watchFaceOpenType == "category") {
            InsiderAppEvents.WATCHFACES_CATEGORY_RATING
        } else
            InsiderAppEvents.WATCHFACES_RATING
        viewModel.sessionManager.logInsiderAppEvent(eventName + viewModel.rating)

    }

    private fun handleEmoji(star: Int) {
        when (star) {
            1 -> {

                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimThird.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star_border)
            }

            2 -> {
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star)
                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimThird.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star_border)
            }

            3 -> {
                binding.lvAnimThird.setImageResource(R.drawable.ic_star)
                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star)
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star_border)
            }

            4 -> {
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star)
                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star)
                binding.lvAnimThird.setImageResource(R.drawable.ic_star)
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star_border)
            }

            5 -> {
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star)
                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star)
                binding.lvAnimThird.setImageResource(R.drawable.ic_star)
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star)
            }
        }
    }


    override fun subscribeObservers() {
//        viewModel.watchFaceRated.observe(this) {
//            it.getContent()?.let {
//                navigateUpSafe()
//            }
//        }


    }

}