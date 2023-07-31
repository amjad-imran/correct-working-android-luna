package com.noisefit.ui.friends

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFriendsBinding
import com.noisefit.ui.feeds.FEEDS_TERMS_KEY
import com.noisefit.ui.friends.compete.FriendSharedViewModel
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsFragment : BaseFragment<FragmentFriendsBinding>(FragmentFriendsBinding::inflate) {

    private var openFromNotification = false
    private lateinit var pagerAdapter: FriendsPagerAdapter
    private val sharedViewModel: FriendSharedViewModel by activityViewModels()
    private var currentItem: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            val friendsActivity = it.getInt("friendsactivity", -1)
            if (friendsActivity != -1) {
                openFromNotification = true
                currentItem = friendsActivity
            } else {
                it.getDeeplinkPathArg()?.let { param ->
                    currentItem = if (param.equals("friendsfeed", true)) {
                        0
                    } else if (param.equals("friendsactivity", true)) {
                        1
                    } else if (param.equals("friendscompetition", true)) {
                        2
                    } else if (param.equals("reaction", true)) {
                        sharedViewModel.showReactionSheet = true
                        1
                    } else {
                        0
                    }
                }
            }
            arguments = null

        }
        setViewPager()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentItem = tab?.position ?: 0
                if (tab?.position == 0)
                    sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMUNITY_FEEDS_CLICK)
                else if (tab?.position == 1)
                    sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMUNITY_FRIENDS_CLICK)
                else
                    sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMUNITY_COMPETE_CLICK)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })




    }

    private fun showWalkAround() {
        if (!sharedViewModel.localDataStore.isFriendsWalkAround()) {
            navigate(FriendsFragmentDirections.actionNavigationFriendsFragToFriendsWalkAroundBottomDialogFragment())
        }
    }

    private fun showPrivacyBottomSheet() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            FEEDS_TERMS_KEY,
            this
        ) { _, bundle ->
            val agree = bundle.getBoolean("agree")
            if (agree) {
                sharedViewModel.localDataStore.setFriendsPrivacyAcceptStatus(true)
                showWalkAround()
            }
        }
        navigate(FriendsFragmentDirections.actionNavigationFriendsToFeedsPrivacyBottomDialogFragment())
    }

    private fun setViewPager() {
        pagerAdapter = FriendsPagerAdapter(childFragmentManager, lifecycle)
        binding.vpFriends.isUserInputEnabled = false
        binding.vpFriends.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.vpFriends) { tab, position ->
            tab.text = arrayListOf("Feed", "Track", "Compete")[position]
        }.attach()
        if (openFromNotification) {
            openFromNotification = false
            delay(100) {
                binding.vpFriends.setCurrentItem(currentItem, true)
            }
        } else {
            binding.vpFriends.setCurrentItem(currentItem, true)
        }


    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.getPendingRequestCount()
        if (sharedViewModel.localDataStore.getFriendsPrivacyAcceptStatus()) {
            showWalkAround()
        } else {
            showPrivacyBottomSheet()
        }
    }

    override fun initListener() {
        binding.ivUserImage.loadImage(
            binding.ivUserImage.context,
            sharedViewModel.localDataStore.getUser()?.imageUrl,
            R.drawable.ic_default_profile_image
        )

        binding.ivUserImage.setOnClickListener {

            sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMUNITY_MYPROFILE_CLICK)
            navigate(
                FriendsFragmentDirections.actionNavigationFriendsToFriendProfileFragment().apply {
                    friendId = -1
                })


        }

        binding.ivAddedFriendCount.setOnClickListener {
            sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMUNITY_COMPETITON_ADD_FRIENDS_CLICK)
            navigate(
                FriendsFragmentDirections.actionNavigationFriendsFragmentToRequestFragment()
            )
        }
        binding.view1.setOnClickListener {
            sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMUNITY_ADD_FRIENDS_CLICK)
            navigate(FriendsFragmentDirections.actionNavigationFriendsFragmentToAddFriendsFragment())
        }
    }

    override fun subscribeObservers() {

        sharedViewModel.requestCount.observe(this) {
            if (it == 0) {
                binding.ivDot.gone()
            } else {
                binding.ivDot.visible()
            }
        }

    }


}