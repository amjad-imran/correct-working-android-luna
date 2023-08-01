package com.oreo.ui.workout.detect

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit_commans.data.model.OreoAutoSportData


class DetectWorkoutPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val hm: HashMap<String, ArrayList<OreoAutoSportData>>,
    val titleList: ArrayList<String>
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return hm.size
    }

    override fun createFragment(position: Int): Fragment {

        return DetectWorkoutFragment.newInstance(hm[titleList[position]]!!)
    }


}