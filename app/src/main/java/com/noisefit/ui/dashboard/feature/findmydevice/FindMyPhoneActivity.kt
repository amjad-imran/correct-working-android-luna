package com.noisefit.ui.dashboard.feature.findmydevice

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import com.airbnb.lottie.LottieDrawable
import com.noisefit.R
import com.noisefit.databinding.ActivityFindMyPhoneBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.interfaces.QueryCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FindMyPhoneActivity : BaseActivity<ActivityFindMyPhoneBinding>() {

    @Inject
    lateinit var sessionManager: SessionManager

    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, FindMyPhoneActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        turnScreenOnAndKeyguardOff()

        binding.animateView.repeatCount = 0
        binding.animateView.setAnimation(R.raw.anim_finding_device_default)
        binding.animateView.playAnimation()
        binding.animateView.repeatCount = LottieDrawable.INFINITE


    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOffAndKeyguardOn()
    }

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }

    override fun initListener() {
        binding.bStop.setOnClickListener {
            finish()
        }
    }

    override fun observeSubscriber() {
        sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.OpenFindMyPhoneActivity -> {
                    if (!it.isRinging) {
                        finish()
                    }

                }
                else -> {}
            }
        }
    }

    override fun getViewBinding() = ActivityFindMyPhoneBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? {
        return null
    }
}


fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }

    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
        }
    }
}

fun Activity.turnScreenOffAndKeyguardOn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(false)
        setTurnScreenOn(false)
    } else {
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }
}