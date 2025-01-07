package com.noisefit.ui.onboarding

import android.content.Intent
import android.os.Bundle
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityAllDoneBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.ui.loadImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AllDoneActivity : BaseActivity<ActivityAllDoneBinding>() {
    @Inject
    lateinit var ringDataStore: RingDataStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //todo will change raw file once received
//        binding.vPlayer.repeatCount = 0
//        binding.vPlayer.setAnimation(R.raw.anim_3_2_1_go)
//        binding.vPlayer.playAnimation()

        binding.vPlayer.repeatCount = LottieDrawable.INFINITE
        binding.vPlayer.setAnimation(R.raw.anim_pairing)
        binding.vPlayer.playAnimation()

        binding.ivWatchImage.loadImage(this, ringDataStore.getRingDevice()?.ringInfo?.image2)

    }

    override fun initListener() {
        binding.bLetsGo.setOnClickListener {
            startActivity(OreoMainActivity.getStartIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    override fun observeSubscriber() {

    }

    override fun getViewBinding() = ActivityAllDoneBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null

    override fun logAppEvent(eventName: String, data: HashMap<String, Any>?) {

    }


}