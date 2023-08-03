package com.noisefit.ui.onboarding.pairing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityPairDeviceBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.ui.playAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PairDeviceActivity : BaseActivity<ActivityPairDeviceBinding>() {

    companion object {
        var showBack = false
        fun getStartIntent(context: Context, showBack: Boolean = false): Intent {
            this.showBack = showBack
            return Intent(context, PairDeviceActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {
    }

    override fun initListener() {
    }

    override fun observeSubscriber() {
    }

    override fun getViewBinding() = ActivityPairDeviceBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null

}