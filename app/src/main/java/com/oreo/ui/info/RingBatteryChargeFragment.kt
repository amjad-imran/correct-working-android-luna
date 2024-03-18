package com.oreo.ui.info

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingBatteryChargeBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingBatteryChargeFragment :
    BaseFragment<FragmentRingBatteryChargeBinding>(FragmentRingBatteryChargeBinding::inflate) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = "Charging your ring"

        initUI()
    }

    private fun initUI() {
        setVideo()
        binding.rvPoints.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPoints.adapter = RingCarePointsAdapter().apply {
            this.setDataSet(
                arrayListOf(
                    getString(R.string.charge_point1),
                    getString(R.string.charge_point2),
                    getString(R.string.charge_point3)
                )
            )
        }
    }



    private fun setVideo() {
        binding.videoOnboard.apply {
            setVideoURI(
                Uri.parse(
                    "android.resource://" + requireContext().packageName + "/" +
                            R.raw.video_find_ring
                )
            )
            setOnPreparedListener { mp -> mp.isLooping = true }
            start()
        }
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }


    }

    override fun subscribeObservers() {

    }


}