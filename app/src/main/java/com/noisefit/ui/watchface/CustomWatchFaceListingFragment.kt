package com.noisefit.ui.watchface

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.databinding.FragmentCustomWatchFaceListingBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.watchface.adapter.CustomWatchFaceActions
import com.noisefit.ui.watchface.adapter.CustomWatchFaceAdapter
import com.noisefit_commans.models.CustomWatchFace
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomWatchFaceListingFragment :
    BaseFragment<FragmentCustomWatchFaceListingBinding>(FragmentCustomWatchFaceListingBinding::inflate),
    CustomWatchFaceActions {

    @Inject
    lateinit var sessionManager: SessionManager

    private val adapter: CustomWatchFaceAdapter by lazy {
        CustomWatchFaceAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_custom_watchface).uppercase()

        setRecyclerView()

    }

    private fun setRecyclerView() {
        binding.rvWatchFace.layoutManager = GridLayoutManager(context, 2)
        binding.rvWatchFace.adapter = adapter
        adapter.setDataSet(AppStaticData.getCustomWatchFaceNav(sessionManager.connectedDevice.value))
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

    override fun onWatchFaceClicked(face: CustomWatchFace) {
       /* navigate(
            CustomWatchFaceListingFragmentDirections.actionCustomWatchFaceListingFragmentToCustomiseWatchfaceFragment(
                face
            )
        )*/
    }
}