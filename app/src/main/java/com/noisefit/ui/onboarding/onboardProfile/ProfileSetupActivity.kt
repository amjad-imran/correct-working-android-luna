package com.noisefit.ui.onboarding.onboardProfile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.R
import com.noisefit.databinding.ActivityProfileSetupBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.ui.playAnimation
import com.noisefit.ui.onboarding.FirebaseUpdateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSetupActivity : BaseActivity<ActivityProfileSetupBinding>() {

    val firebaseViewModel: FirebaseUpdateViewModel by viewModels()


    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, ProfileSetupActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseViewModel.generateToken()
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

    override fun getViewBinding() = ActivityProfileSetupBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding = binding.progressBar
}