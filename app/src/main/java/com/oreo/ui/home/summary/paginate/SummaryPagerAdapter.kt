package com.oreo.ui.home.summary.paginate

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS


class SummaryPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
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

        return if (position < dates.size) {
            dates[position]
        } else {
            null
        }
    }

    override fun createFragment(position: Int): Fragment {
        val date = dates[position]
        val todayDate = DateFormats.getTodaysDateString(10)
        return if (todayDate.equals(date, true)) {
            (SummaryDataFragmentToday.newInstance(date))
        } else {
            (SummaryDataFragment.newInstance(date))
        }
    }

    override fun getItemCount(): Int {
        return dates.size
    }

    fun getPositionForDate(selectedDate: String?): Int {
        if (selectedDate == null) return dates.size - 1

        val index = dates.indexOfFirst {
            it.equals(selectedDate)
        }
        if (index == -1) {
            return dates.size - 1
        }
        return index
    }

    fun reloadPos(currentPos: Int) {
        notifyDataSetChanged()
    }
}