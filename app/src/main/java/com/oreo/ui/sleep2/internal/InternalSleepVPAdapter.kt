package com.oreo.ui.sleep2.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.oreo.ui.sleep2.internal.SleepMultiBarChartFragment

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

    fun addFragment(frag: Fragment) {
        fragments.add(frag)
        val lastPos = fragments.size - 1
        notifyItemInserted(lastPos)
    }
}