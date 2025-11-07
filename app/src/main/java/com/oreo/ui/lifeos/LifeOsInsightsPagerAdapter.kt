package com.oreo.ui.lifeos

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LifeOsInsightsPagerAdapter(
    fragment: Fragment,
    private val items: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val text = items[position]
        return LifeOsInsightCardFragment.newInstance(text)
    }
}

