package com.noisefit.ui.friends.reactions

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit.luna.databinding.LayoutReactionsBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReactionsBottomSheet :
    BaseBottomSheetWithTransparent<LayoutReactionsBottomSheetBinding>(
        LayoutReactionsBottomSheetBinding::inflate
    ) {


    private var tabTitleList = ArrayList<String>()
    private var tabIconList = ArrayList<Drawable?>()
    private var reactionsWrapperList: ArrayList<ReactionsWrapper>? = null
    private lateinit var reactionsViewPager: ReactionsViewPager
    private var friendId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            reactionsWrapperList =
                ArrayList(ReactionsBottomSheetArgs.fromBundle(it).userFriendReactions.toList())
            handleReactionData(reactionsWrapperList!!)

        }

        setViewPager()
    }

    //getChildFragmentManager
    private fun setViewPager() {
        reactionsViewPager = ReactionsViewPager(
            this,
            reactionsWrapperList!!
        )
//


        binding.viewPager1.apply {
            offscreenPageLimit = 3
            adapter = reactionsViewPager
            currentItem = 0
        }


        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager1
        ) { myTabLayout: TabLayout.Tab, position: Int ->
            myTabLayout.text = tabTitleList[position]
            myTabLayout.icon = tabIconList[position]
        }.attach()


    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


    private fun handleReactionData(reactionsWrapperList: ArrayList<ReactionsWrapper>) {
        tabTitleList.add("All")
        tabIconList.add(null)
        reactionsWrapperList.forEach { reactionsWrapper ->
            when (reactionsWrapper.title) {
                Emoji.EmojiHand.emoji -> {
                    tabTitleList.add("${reactionsWrapper.count}")
                    tabIconList.add(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            com.noisefit_commans.R.drawable.ic_strong_emoji
                        )
                    )
                }
                Emoji.EmojiHeart.emoji -> {
                    tabTitleList.add("${reactionsWrapper.count}")
                    tabIconList.add(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            com.noisefit_commans.R.drawable.ic_heart_emoji
                        )
                    )
                }
                Emoji.Emoji100.emoji -> {
                    tabTitleList.add("${reactionsWrapper.count}")
                    tabIconList.add(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            com.noisefit_commans.R.drawable.ic_100_emoji
                        )
                    )
                }
                Emoji.EmojiFire.emoji -> {
                    tabTitleList.add("${reactionsWrapper.count}")
                    tabIconList.add(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            com.noisefit_commans.R.drawable.ic_fire_emoji
                        )
                    )
                }
            }
        }


    }

}