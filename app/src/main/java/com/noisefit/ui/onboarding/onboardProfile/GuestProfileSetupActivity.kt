package com.noisefit.ui.onboarding.onboardProfile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityGuestProfileSetupBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.ui.playAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuestProfileSetupActivity : BaseActivity<ActivityGuestProfileSetupBinding>() {
    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, GuestProfileSetupActivity::class.java).apply {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lottieBackAnim.playAnimation(
            LottieDrawable.INFINITE,
            R.raw.anim_challenge_back
        )
    }

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }

    override fun initListener() {

    }

    override fun observeSubscriber() {

    }

    override fun getViewBinding() = ActivityGuestProfileSetupBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding = binding.progressBar
}