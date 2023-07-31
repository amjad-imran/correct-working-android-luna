package com.noisefit.ui.challengeNew.list

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChallengeListPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return NewChallengeFragment()
            1 -> return JoinedChallengeFragment()
            2 -> return CompletedChallengeFragment()
        }
        return NewChallengeFragment()
    }
}