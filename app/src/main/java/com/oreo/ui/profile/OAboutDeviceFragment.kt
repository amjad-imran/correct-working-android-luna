package com.oreo.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOAboutDeviceBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OAboutDeviceFragment :
    BaseFragment<FragmentOAboutDeviceBinding>(FragmentOAboutDeviceBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTabLayoutAndVp()
        setUi()

    }

    private fun setTabLayoutAndVp() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        // Set up the adapter
        val adapter = OAboutDeviceVpAdapter(this)
        viewPager.adapter = adapter

        // Link TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Ring"  // Tab 1
                    val tabTextView = tab.view.findViewById<TextView>(com.google.android.material.R.id.text)
                    tabTextView?.apply {
                        isAllCaps = false // Disable all caps for this tab
                    }
                }
                1 -> {
                    tab.text = "Case"  // Tab 2
                    val tabTextView = tab.view.findViewById<TextView>(com.google.android.material.R.id.text)
                    tabTextView?.apply {
                        isAllCaps = false // Disable all caps for this tab
                    }
                }
            }
        }.attach()
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
        super.onDestroyView()

    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}