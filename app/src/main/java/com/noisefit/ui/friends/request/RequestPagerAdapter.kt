package com.noisefit.ui.friends.request

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit.ui.friends.request.received.RequestReceivedFragment
import com.noisefit.ui.friends.request.sent.RequestSentFragment

class RequestPagerAdapter(fragmentManager: FragmentManager, lifecycle:Lifecycle):
    FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return RequestReceivedFragment()
            1 -> return RequestSentFragment()
        }
        return RequestReceivedFragment()
    }
}