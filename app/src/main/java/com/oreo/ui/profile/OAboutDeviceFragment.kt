package com.oreo.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOAboutDeviceBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setVisibilityByCondition
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class OAboutDeviceFragment :
    BaseFragment<FragmentOAboutDeviceBinding>(FragmentOAboutDeviceBinding::inflate) {

    private val viewModel: OAboutDeviceViewModel by viewModels()

    private var tabMediator: TabLayoutMediator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
        setTabLayoutAndVp()
    }

    private fun setTabLayoutAndVp() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val generation = getGeneration(viewModel.ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw)
        val showCase = generation != 1 && viewModel.sessionManager.caseInfoData.value?.serialNumber != null

        // Set up the adapter with the flag
        val adapter = OAboutDeviceVpAdapter(this, showCase)
        viewPager.adapter = adapter

        tabLayout.setVisibilityByCondition(showCase)
        viewPager.isUserInputEnabled = showCase

        tabMediator?.detach()
        tabMediator = if (showCase) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = "Ring"
                        val tv = tab.view.findViewById<TextView>(com.google.android.material.R.id.text)
                        tv?.isAllCaps = false
                    }
                    1 -> {
                        tab.text = "Case"
                        val tv = tab.view.findViewById<TextView>(com.google.android.material.R.id.text)
                        tv?.isAllCaps = false
                    }
                }
            }.also { it.attach() }
        } else null
    }

    private fun setUi() {
        binding.toolbar.tvTitle.text = getString(R.string.text_about_device)
    }

    private fun getGeneration(serialNoRaw: String?): Int {
        if (serialNoRaw == null) return 1

        return try {
            serialNoRaw.substring(1, 2).toInt()
        } catch (exp: Exception) {
            exp.printStackTrace()
            1
        }
    }

    override fun onDestroyView() {
        tabMediator?.detach()
        tabMediator = null
        super.onDestroyView()
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.sessionManager.caseInfoData.observe(this){
            setTabLayoutAndVp()
        }
    }

}