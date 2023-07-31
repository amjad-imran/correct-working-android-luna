package com.noisefit.ui.reward.voucher

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit.ui.reward.voucher.active.ActiveVoucherFragment
import com.noisefit.ui.reward.voucher.expired.ExpiredVoucherFragment

class MyVoucherPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return ActiveVoucherFragment()
            1 -> return ExpiredVoucherFragment()
        }
        return ActiveVoucherFragment()
    }
}