package com.noisefit.ui.profile

import android.os.Bundle
import com.bumptech.glide.Glide
import com.noisefit.R
import com.noisefit.databinding.ActivityProfilePicBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.utils.LOGS

class ProfilePicActivity :
    BaseActivity<ActivityProfilePicBinding>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUrl = intent.getStringExtra("imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .error(R.drawable.ic_default_profile_image)
            .placeholder(R.drawable.ic_default_profile_image)
            .into(binding.ivProfile)
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            try {
                onBackPressed()
                supportFinishAfterTransition()
            } catch (exp: Exception) {
                LOGS.d("Safe navigate up")
            }
        }

        binding.constMain.setOnClickListener {
            binding.backBtn.performClick()
        }
        binding.ivProfile.setOnClickListener {
            //DON'T remove
        }
    }

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {}

    override fun observeSubscriber() {}

    override fun getViewBinding() = ActivityProfilePicBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null
}