package com.noisefit.ui.roundup

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.noisefit_commans.data.model.RoundUpResponse

class PagerAdapter(fragmentManager: FragmentManager, val data: RoundUpResponse) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var dataCount = 0

    override fun getItem(position: Int) = RoundUpEntryFragment.newInstance(position, data)

    override fun getCount() = dataCount

    override fun getItemPosition(obj: Any): Int {
        if (obj is RefreshData) {
            obj.refresh()
        }
        return super.getItemPosition(obj)
    }


}

interface RefreshData {
    fun refresh()
}