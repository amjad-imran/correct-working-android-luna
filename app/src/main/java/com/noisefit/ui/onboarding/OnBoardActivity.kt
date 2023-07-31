package com.noisefit.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import com.airbnb.lottie.LottieDrawable
import com.noisefit.R
import com.noisefit.databinding.ActivityOnBoardBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.ui.playAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardActivity : BaseActivity<ActivityOnBoardBinding>() {

    companion object {
        fun getStartIntent(context: Context, showLogin: Boolean = false): Intent {
            return Intent(context, OnBoardActivity::class.java).apply {
                this.putExtra("showLogin", showLogin)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lottieBackAnim.playAnimation(
            LottieDrawable.INFINITE,
            R.raw.anim_challenge_back
        )

        val showLogin = intent.getBooleanExtra("showLogin", false)

        if (showLogin) {
            findNavController(R.id.nav_host_fragment).navigate(R.id.joinNoisefitFragment)
        }
    }

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }

    override fun initListener() {

    }

    override fun observeSubscriber() {

    }

    override fun getViewBinding() = ActivityOnBoardBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding = binding.progressBar
}