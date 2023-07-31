package com.noisefit.ui.friends.addfriends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.model.BuddiesUserNew
import com.noisefit.databinding.FragmentCommonInterestBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_ADD_STRING
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_REMOVE_STRING
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommonInterestFragment :
    BaseFragment<FragmentCommonInterestBinding>(FragmentCommonInterestBinding::inflate) {

    private val sharedViewModel: AddTabFriendSharedViewModel by activityViewModels()
    private val viewModel: AddFriendViewModel by viewModels()
    private val mAdapter: ContactListAdapter by lazy {
        ContactListAdapter(object : OnAddItemClickListener {

            override fun onAddClicked(friendUser: BuddiesUserNew, position: Int) {
                viewModel.addFriendRequest(friendUser.id ?: -1, FRIEND_STATUS_ADD_STRING) {
                    mAdapter.updateStatus(position)
                    updateUiState()
                }
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMONINTEREST_ADD_CLICK)
            }

            override fun onRemoveClicked(friendUser: BuddiesUserNew, position: Int) {
                viewModel.removeFriendRequest(friendUser.id ?: -1, FRIEND_STATUS_REMOVE_STRING) {
                    mAdapter.updateStatusToAdd(position)
                    updateUiState()
                }
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMONINTEREST_REMOVE_CLICK)
            }

            override fun onItemClicked(friendUser: BuddiesUserNew) {
                navigate(
                    AddFriendsFragmentDirections.actionAddFriendsFragmentToFriendProfileFragment()
                        .apply { friendId = friendUser.id ?: -1 })
            }

            override fun onDataFiltered(hasUser: Boolean) {
                if (hasUser) {
                    binding.lytNoContact.root.gone()
                } else {
                    binding.lytNoContact.apply {
                        imageViewNoChallenge.setImageResource(R.drawable.ic_friends_navigation)
                        textViewTitle.text = getString(R.string.text_no_user_found)
                        textViewMsg.text = ""
                        lytJoin.gone()
                        root.visible()
                    }
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getInterestListData()
    }

    private fun setRecycler() {
        with(binding.rvInterest) {
            layoutManager = LinearLayoutManager(context)
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

        binding.lytNoContact.bJoinChallenge.setOnClickListener {
            navigate(R.id.bottomSheetInterestSelector)
        }

    }

    private fun updateUiState() {
        val userList = mAdapter.itemCount
        binding.lytNoContact.textViewTitle.text = getString(R.string.text_no_interest_added)
        binding.lytNoContact.textViewMsg.text = getString(R.string.text_no_interest_added_msg)
        binding.lytNoContact.bJoinChallenge.text = getString(R.string.text_add_interest)
        if (userList == 0) {
            binding.searchBox.gone()
            binding.etSearch.gone()
            binding.rvInterest.gone()
            binding.lytNoContact.root.visible()
        } else {
            binding.searchBox.visible()
            binding.etSearch.visible()
            binding.rvInterest.visible()
            binding.lytNoContact.root.gone()
        }
    }

    override fun subscribeObservers() {
        viewModel.commonInterestListData.observe(this) {
            mAdapter.setDataSet(it)

            sharedViewModel.commonInterestCount.value = it.size
            sharedViewModel.setSelected()
            updateUiState()
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }


}