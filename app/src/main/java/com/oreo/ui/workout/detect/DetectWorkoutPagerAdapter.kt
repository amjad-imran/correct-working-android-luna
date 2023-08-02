package com.oreo.ui.workout.detect

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit_commans.data.model.OreoAutoSportData


class DetectWorkoutPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val hm: LinkedHashMap<String, ArrayList<OreoAutoSportData>>,
    private val titleList: ArrayList<String>,
    private val detectWorkoutFragmentListener: DetectWorkoutFragmentListener

) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return hm.size
    }

    override fun createFragment(position: Int): Fragment {

        val frag =  DetectWorkoutFragment.newInstance(hm[titleList[position]]!!,titleList[position])
        frag.setDetectWorkoutListener(detectWorkoutFragmentListener)
        return frag
    }



}