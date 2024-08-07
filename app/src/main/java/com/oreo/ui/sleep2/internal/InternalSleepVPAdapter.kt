package com.oreo.ui.sleep2.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class InternalSleepVPAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    val fragments = mutableListOf<Fragment>()


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun setDataSet(frags: List<Fragment>) {
        fragments.clear()
        fragments.addAll(frags)
        notifyDataSetChanged()
    }

    fun addFragment(frag: Fragment, atStart: Boolean) {
        if (atStart) {
            fragments.add(0, frag)
            notifyItemInserted(0)
        } else {
            fragments.add(frag)
            notifyItemInserted(fragments.size - 1)
        }
    }
}