package com.oreo.ui.device.surgeCase

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.oreo.data.model.surgeCase.AboutSurgeCaseModel

class AboutChargerAdapter(fragment: Fragment,val items: List<AboutSurgeCaseModel>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val item = items[position]
        return ItemAboutChargerFragment.newInstance(item)
    }
}
