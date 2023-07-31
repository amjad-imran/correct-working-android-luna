package com.noisefit.ui.reward.voucher

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMyVoucherBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyVoucherFragment :
    BaseFragment<FragmentMyVoucherBinding>(FragmentMyVoucherBinding::inflate) {
    private lateinit var mPagerAdapter: MyVoucherPagerAdapter
    private val mSharedViewModel: VoucherSharedViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewPager()

    }

    private fun setViewPager() {
        mPagerAdapter = MyVoucherPagerAdapter(childFragmentManager, lifecycle)
        binding.vpVoucher.isUserInputEnabled = false
        binding.vpVoucher.adapter = mPagerAdapter
        mSharedViewModel.setSelected()

    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_my_voucher_page_title)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mSharedViewModel.cleanViewModelData()
    }

    override fun subscribeObservers() {
        mSharedViewModel.tabListData.observe(this) {
                TabLayoutMediator(binding.tabLayout, binding.vpVoucher) { tab, position ->
                    tab.text = it[position]
                }.attach()
        }
    }


}