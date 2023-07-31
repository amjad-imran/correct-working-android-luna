package com.noisefit.ui.friends.reactions

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit_commans.data.model.ReactionsWrapper


class ReactionsViewPager(
    fm: Fragment,
    val emojiList: ArrayList<ReactionsWrapper>
) : FragmentStateAdapter(fm) {



    override fun getItemCount(): Int {
        return emojiList.size
    }

    override fun createFragment(position: Int): Fragment {
        return ReactionsFragment.newInstance(
            emojiList[position]
        )
    }


}