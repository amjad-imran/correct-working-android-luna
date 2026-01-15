package com.oreo.ui.lifeos

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.oreo.ui.lifeos.charts.InsightCardUiModel

class LifeOsInsightsPagerAdapter(
    fragment: Fragment,
    private val items: List<InsightCardUiModel>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val curItem = items[position]
        return LifeOsInsightCardFragment.newInstance(curItem)
    }
}
