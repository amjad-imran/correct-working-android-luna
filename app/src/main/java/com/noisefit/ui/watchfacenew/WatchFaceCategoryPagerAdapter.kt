package com.noisefit.ui.watchfacenew

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.noisefit.ui.watchface.WatchFaceFragment
import com.noisefit.ui.watchface.FavouriteWatchFacesFragment


class WatchFaceCategoryPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return WatchFaceFragment()
            1 -> return FavouriteWatchFacesFragment()
        }
        return WatchFaceFragment()
    }
}