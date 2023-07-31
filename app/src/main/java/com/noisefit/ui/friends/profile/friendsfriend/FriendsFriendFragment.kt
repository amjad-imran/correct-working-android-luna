package com.noisefit.ui.friends.profile.friendsfriend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.databinding.FragmentFriendsFriendBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone


class FriendsFriendFragment :
    BaseFragment<FragmentFriendsFriendBinding>(FragmentFriendsFriendBinding::inflate) {
    private lateinit var mPagerAdapter: FriendsFriendPagerAdapter
    private val mSharedViewModel: FFriendSharedViewModel by activityViewModels()
    private val args: FriendsFriendFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSharedViewModel.friendId = args.friendId
        mSharedViewModel.name = args.name ?: ""
        binding.lytToolbar.tvTitle.text = mSharedViewModel.name + "'s friends"
        setViewPager()
    }

    private fun setViewPager() {
        mPagerAdapter = FriendsFriendPagerAdapter(childFragmentManager, lifecycle)
        binding.vpFriends.isUserInputEnabled = false
        binding.vpFriends.adapter = mPagerAdapter
        mSharedViewModel.setSelected()
    }

    override fun initListener() {
        binding.lytToolbar.view1.gone()
        binding.lytToolbar.ivAddFriend.gone()
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSharedViewModel.cleanViewModelData()
    }

    override fun subscribeObservers() {
        mSharedViewModel.tabListData.observe(this) {
            TabLayoutMediator(binding.tabLayout, binding.vpFriends) { tab, position ->
                tab.text = it[position]
            }.attach()
        }
    }


}