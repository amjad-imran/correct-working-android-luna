package com.oreo.ui.femalehealth.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FMHFragmentAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val fragmentSize: Int
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return fragmentSize
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FMHOnboardGoalFragment()
            }

            1 -> FMHOnboardSetPeriodFragment()
            2 -> FMHOnboardSetPeriodFragment()
            3 -> FMHOnboardCalenderFragment()
            4 -> FMHOnboardSetDignosisFragment()
            5 -> FMHOnboardSetHormonFragment()
            else -> FMHOnboardGoalFragment()
        }
    }
}