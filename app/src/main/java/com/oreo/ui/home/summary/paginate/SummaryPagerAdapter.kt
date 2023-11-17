package com.oreo.ui.home.summary.paginate

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class SummaryPagerAdapter(fragmentActivity: FragmentActivity, fragmentList: List<Fragment>) :
    FragmentStateAdapter(fragmentActivity) {
    private val fragmentList: List<Fragment>

    init {
        this.fragmentList = fragmentList
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }
}