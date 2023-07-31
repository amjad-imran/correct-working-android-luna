package com.oreo.ui.sleep

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.oreo.ui.sleep.scoredetails.OSleepScoreDetailsFragment

class OSCVPAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle) :
FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return OSleepScoreDetailsFragment()
            1 -> return OSleepScoreDetailsFragment()
            2 -> return OSleepScoreDetailsFragment()
        }
        return OSleepScoreDetailsFragment()
    }
}