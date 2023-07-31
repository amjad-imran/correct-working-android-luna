package com.noisefit.ui.friends

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit.ui.feeds.feed.FeedFragment
import com.noisefit.ui.friends.compete.CompetitionsListFragment

class FriendsPagerAdapter(fragmentManager: FragmentManager,lifecycle:Lifecycle):
    FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return FeedFragment()
            1 -> return FriendsListFragment()
            2 -> return CompetitionsListFragment()
        }
        return FeedFragment()
    }
}