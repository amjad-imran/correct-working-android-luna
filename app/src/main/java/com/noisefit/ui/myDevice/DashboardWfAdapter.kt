package com.noisefit.ui.myDevice

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit.data.remote.response.Watchface2


class DashboardWfAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val listOfWatchFace: List<List<Watchface2>>,
    private val dashboardWfListener: DashboardWfListener

) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return listOfWatchFace.size
    }

    override fun createFragment(position: Int): Fragment {
        val frag = DashboardWfFragment.newInstance(ArrayList(listOfWatchFace[position]))
        frag.setDashboardWfListener(dashboardWfListener)
        return frag
    }
}