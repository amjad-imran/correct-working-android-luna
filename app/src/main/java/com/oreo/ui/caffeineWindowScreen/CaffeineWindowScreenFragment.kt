package com.oreo.ui.caffeineWindowScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCaffeineWindowScreenBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.setVisibilityByCondition
import com.oreo.ui.customHomeScreen.CustomHomescreenViewModel

class CaffeineWindowScreenFragment :
    BaseFragment<FragmentCaffeineWindowScreenBinding>(FragmentCaffeineWindowScreenBinding::inflate) {

    private val viewModel: CaffeineWindowScreenViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding.apply {
            layoutToolbar.tvTitle.text = getString(R.string.text_caffeine_window)

        }
    }

    override fun initListener() {
        viewModel.rvDisplayAllItemsToggle.observe(this){
            binding.ivToggleRv.setImageResource(
                if (it) R.drawable.ic_baseline_keyboard_arrow_down_24
                else R.drawable.ic_arrow_up_stress
            )

            binding.rvAllItems.setVisibilityByCondition(it)
        }
    }

    override fun subscribeObservers() {

    }

}