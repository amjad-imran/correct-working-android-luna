package com.oreo.ui.heartrate

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HeartRatePagerAdapter (fragmentActivity: Fragment) : FragmentStateAdapter(fragmentActivity) {
    private var dates = ArrayList<String>()

    fun setDataSet(dates: List<String>) {
        this.dates.clear()
        this.dates.addAll(dates)
        notifyDataSetChanged()
    }

    fun getDate(position: Int): String? {

        if (position == -1) {
            return null
        }

        return try {
            if (position < dates.size) {
                dates[position]
            } else {
                null
            }
        } catch (exp: Exception) {
            null
        }
    }

    override fun getItemCount(): Int {
        return dates.size
    }

    fun getPositionForDate(selectedDate: String?): Int {
        if (selectedDate == null) return dates.size - 1

        val index = dates.indexOfFirst {
            it == selectedDate
        }
        if (index == -1) {
            return dates.size - 1
        }
        return index
    }

    override fun createFragment(position: Int): Fragment {
        val date = dates[position]
        return OHeartRateDataFragment.newInstance(date)

    }

}