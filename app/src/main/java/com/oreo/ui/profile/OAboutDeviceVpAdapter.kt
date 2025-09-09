package com.oreo.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.oreo.ui.profile.ring.OAboutRingFragment
import com.oreo.ui.profile.ringCase.OAboutRingCaseFragment

class OAboutDeviceVpAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OAboutRingFragment() // Replace with your actual fragment
            1 -> OAboutRingCaseFragment() // Replace with your actual fragment
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}