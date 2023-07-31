package com.noisefit.ui.friends.profile.myfriend

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.R
import com.noisefit.data.model.FriendsFriendListData
import com.noisefit.databinding.FragmentMyFriendListBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyFriendListFragment :
    BaseFragment<FragmentMyFriendListBinding>(FragmentMyFriendListBinding::inflate) {
    private val myFriendViewModel: MyFriendViewModel by viewModels()
    private val myFriendListAdapter: MyFriendListAdapter by lazy {
        MyFriendListAdapter(object : OnFriendFilterListener {
            override fun onAddClicked(resultData: FriendsFriendListData, position: Int) {

            }

            override fun onRemoveClicked(resultData: FriendsFriendListData, position: Int) {

            }

            override fun onItemClick(resultData: FriendsFriendListData) {
                navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", resultData.userId?.toInt() ?: -1)
                })
            }
            override fun onDataFiltered(hasUser: Boolean) {
                if (hasUser) {
                    binding.rvFriends.visible()
                    binding.tvEmptyPlaceholder.gone()
                } else {
                    binding.rvFriends.gone()
                    binding.tvEmptyPlaceholder.visible()
                }
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        myFriendViewModel.getAllFriendList()
    }

    override fun initListener() {
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.tvTitle.text = getString(R.string.text_my_profile)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytToolbar.ivAddFriend.setOnClickListener {
            navigate(R.id.addFriendsFragment)
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myFriendListAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    override fun subscribeObservers() {
        myFriendViewModel.allFriendList.observe(this) {
            myFriendListAdapter.setDataSet(it)
            updateUi()

        }
        myFriendViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        myFriendViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    private fun setRecycler() {
        with(binding.rvFriends) {
            adapter = myFriendListAdapter
        }
    }

    private fun updateUi() {
        val itemCount = myFriendListAdapter.itemCount
        if (itemCount == 0) {
            binding.rvFriends.gone()
            binding.searchBox.gone()
            binding.etSearch.gone()
            binding.tvEmptyPlaceholder.visible()
        } else {
            binding.rvFriends.visible()
            binding.searchBox.visible()
            binding.etSearch.visible()
            binding.tvEmptyPlaceholder.gone()
        }
    }
}