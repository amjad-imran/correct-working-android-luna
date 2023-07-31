package com.noisefit.ui.friends.profile.friendsfriend.all

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.data.model.FriendsFriendListData
import com.noisefit.luna.databinding.FragmentFAllFriendBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.profile.friendsfriend.FFriendSharedViewModel
import com.noisefit.ui.friends.profile.myfriend.MyFriendListAdapter
import com.noisefit.ui.friends.profile.myfriend.OnFriendFilterListener
import com.noisefit.ui.friends.request.received.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FAllFriendFragment :
    BaseFragment<FragmentFAllFriendBinding>(FragmentFAllFriendBinding::inflate) {
    private val mViewModel: FAllFriendViewModel by viewModels()
    private val mSharedViewModel: FFriendSharedViewModel by activityViewModels()
    private val mAdapter: MyFriendListAdapter by lazy {
        MyFriendListAdapter(object : OnFriendFilterListener {
            override fun onAddClicked(resultData: FriendsFriendListData, position: Int) {
                mViewModel.addFriendRequest(
                    resultData.userId ?: -1,
                    FRIEND_STATUS_ADD_STRING
                ) {
                    mAdapter.updateStatus(position, FRIEND_STATUS_ADD.toInt())
                    updateUiState()
                }
            }

            override fun onRemoveClicked(resultData: FriendsFriendListData, position: Int) {
                mViewModel.removeFriendRequest(
                    resultData.userId ?: -1,
                    FRIEND_STATUS_REMOVE_STRING
                ) {
                    mAdapter.updateStatus(position, FRIEND_STATUS_NONE.toInt())
                    updateUiState()
                }
            }

            override fun onItemClick(resultData: FriendsFriendListData) {
                navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", resultData.userId ?: -1)
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
        mViewModel.friendId = mSharedViewModel.friendId.toString()
        setRecycler()
        mViewModel.getAllFriendList()

    }

    private fun setRecycler() {
        with(binding.rvFriends) {
            adapter = mAdapter
        }

    }

    override fun initListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    override fun subscribeObservers() {
        mViewModel.allFriendList.observe(this) {
            mAdapter.setDataSet(it)
            mSharedViewModel.allCount.value = it.size
            updateUiState()
            mSharedViewModel.setSelected()
        }
        mViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }

    private fun updateUiState() {
        val itemCount = mAdapter.itemCount
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