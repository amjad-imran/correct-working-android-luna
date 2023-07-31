package com.noisefit.ui.friends.addfriends

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class AddFriendsPagerAdapter(fragmentManager: FragmentManager, lifecycle:Lifecycle):
    FragmentStateAdapter(fragmentManager, lifecycle){

    var fragsArray = ArrayList<Fragment>()
    override fun getItemCount(): Int {
        return 4
    }

    fun setFragments(frags:List<Fragment>){
        fragsArray.clear()
        fragsArray.addAll(frags)
        notifyDataSetChanged()
    }

    override fun createFragment(position: Int): Fragment {
            return fragsArray[position]
    }
}