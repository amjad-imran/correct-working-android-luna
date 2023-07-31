package com.noisefit.ui.friends.addfriends

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddFriendsBinding
import com.noisefit.ui.friends.compete.FriendSharedViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.getDeeplinkPathArg
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddFriendsFragment :
    BaseFragment<FragmentAddFriendsBinding>(FragmentAddFriendsBinding::inflate) {


    private val sharedViewModel: AddTabFriendSharedViewModel by activityViewModels()

    private val mFriendSharedVModel: FriendSharedViewModel by activityViewModels()

    private var fragsArray: List<Pair<AddFriendsMode, Fragment>>? = null

    var defaultMode: AddFriendsMode? = null

    private val tabsAdapter: AddFriendsTabAdapter by lazy {
        AddFriendsTabAdapter(
            object : TabAction {
                override fun onTabClicked(position: Int, text: String) {
                    sharedViewModel.currentItem = position

                    fragsArray?.let {
                        if ((it.size) > position) {
                            loadFragment(it[position])
                        }
                    }

                    binding.rvTabs.scrollToPosition(position)
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            it.getDeeplinkPathArg()?.let { param ->
                defaultMode = if (param.equals("AddFriendNear", true)) {
                    AddFriendsMode.NEAR_ME
                } else if (param.equals("AddFriendWinner", true)) {
                    AddFriendsMode.PAST_WINNER
                } else if (param.equals("AddFriendInterest", true)) {
                    AddFriendsMode.COMMON_INTEREST
                } else if (param.equals("AddFriendContacts", true)) {
                    AddFriendsMode.CONTACTS
                } else {
                    null
                }
            }
            arguments = null
        }



        setTabs()
    }

    /*private fun setViewPager() {
        pagerAdapter = AddFriendsPagerAdapter(childFragmentManager, lifecycle)
        binding.vpAddFriends.isUserInputEnabled = false
        binding.vpAddFriends.adapter = pagerAdapter
        Handler(Looper.getMainLooper()).postDelayed({
            if (view != null) {
                binding.vpAddFriends.setCurrentItem(sharedViewModel.currentItem, true)
            }
        }, 500)

    }*/

    private fun setTabs() {
        val contactsCount = sharedViewModel.localDataStore.getNoiseFitContactCount()

        if (contactsCount > 0 || contactsCount == -1) {
            fragsArray = arrayListOf(
                Pair(AddFriendsMode.CONTACTS, ContactFragment()),
                Pair(AddFriendsMode.COMMON_INTEREST, CommonInterestFragment()),
                Pair(AddFriendsMode.PAST_WINNER, PastWinnerFragment()),
                Pair(AddFriendsMode.NEAR_ME, NearMeFragment()),
            )
        } else {
            fragsArray =
                arrayListOf(
                    Pair(AddFriendsMode.COMMON_INTEREST, CommonInterestFragment()),
                    Pair(AddFriendsMode.PAST_WINNER, PastWinnerFragment()),
                    Pair(AddFriendsMode.NEAR_ME, NearMeFragment()),
                    Pair(AddFriendsMode.CONTACTS, ContactFragment()),
                )
        }

        sharedViewModel.setSelected()
        binding.rvTabs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTabs.adapter = tabsAdapter

        if (defaultMode == null) {
            tabsAdapter.selectedPosition = sharedViewModel.currentItem
        } else {
            val position = fragsArray?.indexOfFirst {
                it.first == defaultMode
            }
            defaultMode = null
            sharedViewModel.currentItem = position ?: 0
            tabsAdapter.selectedPosition = position ?: 0
        }


        fragsArray?.let {
            if ((it.size) > tabsAdapter.selectedPosition) {
                loadFragment(it[tabsAdapter.selectedPosition])
            }
        }

        if (sharedViewModel.currentItem == 0) {
            tryCatch {
                Handler(Looper.getMainLooper()).postDelayed({
                    nullableBinding?.rvTabs?.smoothScrollToPosition((tabsAdapter.itemCount - 1))
                }, 500)

                Handler(Looper.getMainLooper()).postDelayed({
                    nullableBinding?.rvTabs?.smoothScrollToPosition(0)
                }, 1000)
            }
        } else {
            nullableBinding?.rvTabs?.scrollToPosition(sharedViewModel.currentItem)
        }
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.ivFriendRequest.setOnClickListener {
            navigate(
                R.id.requestFragment
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedViewModel.cleanViewModel()
    }

    override fun subscribeObservers() {

        sharedViewModel.tabListData.observe(this) {
            tabsAdapter.setDataSet(it)
        }

        mFriendSharedVModel.requestCount.observe(this){
            if (it == 0) {
                binding.ivDot.gone()
            } else {
                binding.ivDot.visible()
            }
        }

    }

    fun loadFragment(data: Pair<AddFriendsMode, Fragment>) {
        val fm: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.flFragment, data.second)
        fragmentTransaction.commit()

    }

}

enum class AddFriendsMode {
    CONTACTS, COMMON_INTEREST, PAST_WINNER, NEAR_ME
}