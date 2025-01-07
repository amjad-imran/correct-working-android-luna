package com.noisefit.ui.onboarding.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.noisefit.luna.databinding.ActivityDeviceSetupV2Binding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceSetupActivityV2 : BaseActivity<ActivityDeviceSetupV2Binding>() {

    private val viewModel: DeviceSetupSharedViewModel by viewModels()

    companion object {
        fun getStartIntent(context: Context, fullSetup: Boolean = false): Intent {
            return Intent(context, DeviceSetupActivityV2::class.java).apply {
                this.putExtra("fullSetup", true)//set fullSetup after release
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fullSetup = intent.getBooleanExtra("fullSetup", false)
        viewModel.fullSetup = fullSetup
        if (fullSetup) {//TODO handle - move to device setup directly hiding the top bars
            viewModel.localDataStore.setDeviceSetupStatus(1)
            binding.layoutProgressTop.visible()
        } else {
            binding.layoutProgressTop.gone()
        }
        viewModel.localDataStore.setPreviouslyPaired()
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_device_setup_start)

    }


    override fun onBackPressed() {

    }

    override fun initListener() {

    }

    override fun observeSubscriber() {
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                onApiErrorReceived(response)
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                showShortToast(message)
            }
        }

        viewModel.updateProgress1.observe(this) {
            binding.progressBar1.progress = it
        }
        viewModel.updateProgress2.observe(this) {
            binding.progressBar2.progress = it
        }
        viewModel.updateProgress3.observe(this) {
            binding.progressBar3.progress = it
        }
    }

    override fun getViewBinding() = ActivityDeviceSetupV2Binding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding = binding.progressBar

    override fun logAppEvent(eventName: String, data: HashMap<String, Any>?) {

    }

}