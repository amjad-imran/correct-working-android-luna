package com.noisefit.ui.friends.reactions.paginate

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.UserFriendData


class ReactionsViewPagerPaginate(
    fm: Fragment,
    val postId: Long,
    private val emojis: List<Emoji>,
    private val initialReactionData: List<UserFriendData>? = null,
    private val hasNext: Boolean?
) : FragmentStateAdapter(fm) {


    override fun getItemCount(): Int {
        return (emojis.size + 1)
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {//ALL
            ReactionsFragmentPaginate.newInstance(
                postId, -1, initialReactionData, hasNext
            )
        } else {
            try {
                ReactionsFragmentPaginate.newInstance(
                    postId, emojis[position - 1].emoji.toInt(), null, null
                )

            } catch (exp: IndexOutOfBoundsException) {
                exp.printStackTrace()
                ReactionsFragmentPaginate.newInstance(
                    postId, -1, null, null
                )
            }

        }
    }


}