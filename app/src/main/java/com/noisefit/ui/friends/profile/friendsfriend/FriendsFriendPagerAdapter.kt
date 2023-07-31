package com.noisefit.ui.friends.profile.friendsfriend

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit.ui.friends.profile.friendsfriend.all.FAllFriendFragment
import com.noisefit.ui.friends.profile.friendsfriend.mutual.FMutualFriendFragment

class FriendsFriendPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return FAllFriendFragment()
            1 -> return FMutualFriendFragment()
        }
        return FAllFriendFragment()
    }
}