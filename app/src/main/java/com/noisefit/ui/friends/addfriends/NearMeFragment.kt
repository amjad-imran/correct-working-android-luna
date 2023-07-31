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
import com.noisefit.databinding.FragmentNearMeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.location.SAVE_LOCATION_REQUEST_KEY
import com.noisefit.ui.friends.location.search.CLOSED_SEARCH_STATE_KEY
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_ADD_STRING
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_REMOVE_STRING
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NearMeFragment : BaseFragment<FragmentNearMeBinding>(FragmentNearMeBinding::inflate) {

    private val sharedViewModel: AddTabFriendSharedViewModel by activityViewModels()
    private val viewModel: AddFriendViewModel by viewModels()
    private var clickedItemPos = -1
    private val mAdapter: ContactListAdapter by lazy {
        ContactListAdapter(object : OnAddItemClickListener {

            override fun onAddClicked(friendUser: BuddiesUserNew, position: Int) {
                clickedItemPos = position
                viewModel.addFriendRequest(friendUser.id ?: -1, FRIEND_STATUS_ADD_STRING) {
                    mAdapter.updateStatus(position)
                    updateUiState()
                }
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NEARME_ADD_CLICK)
            }

            override fun onRemoveClicked(friendUser: BuddiesUserNew, position: Int) {
                clickedItemPos = position
                viewModel.removeFriendRequest(friendUser.id ?: -1, FRIEND_STATUS_REMOVE_STRING) {
                    mAdapter.updateStatusToAdd(position)
                    updateUiState()
                }
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NEARME_REMOVE_CLICK)
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
        fetchNearBy()
    }

    private fun fetchNearBy() {
        viewModel.getNearByListData()
    }

    private fun setRecycler() {
        with(binding.rvNearMe) {
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

        requireActivity().supportFragmentManager.setFragmentResultListener(
            CLOSED_SEARCH_STATE_KEY,
            this
        ) { _, bundle ->
            val isClosed = bundle.getBoolean("closed")
            if (isClosed) {
                openLocationBottom()
            }

        }

        binding.lytNoContact.bJoinChallenge.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NEAR_ME_EDIT_YOUR_LOCATION_CLICK)
            requireActivity().supportFragmentManager.setFragmentResultListener(
                SAVE_LOCATION_REQUEST_KEY,
                this
            ) { _, bundle ->
                val isSaved = bundle.getBoolean("save")
                if (isSaved) {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NEAR_ME_EDIT_YOUR_LOCATION_SAVE_CLICK)
                    fetchNearBy()
                }

            }

            openLocationBottom()

        }

    }

    private fun openLocationBottom() {
        navigate(AddFriendsFragmentDirections.actionAddFriendsFragmentToLocationBottomSheet())
    }

    private fun updateUiState() {
        val userList = mAdapter.itemCount
        binding.lytNoContact.textViewTitle.text = getString(R.string.text_no_noisemakers_around_you)
        binding.lytNoContact.textViewMsg.text = getString(R.string.text_connect_noisemakers_msg)
        binding.lytNoContact.bJoinChallenge.text = getString(R.string.text_edit_your_location)
        binding.lytNoContact.bJoinChallenge.setBackgroundResource(R.drawable.tab_layout_bg)
        if (userList == 0) {
            binding.searchBox.gone()
            binding.etSearch.gone()
            binding.rvNearMe.gone()
            binding.lytNoContact.root.visible()
        } else {
            binding.searchBox.visible()
            binding.etSearch.visible()
            binding.rvNearMe.visible()
            binding.lytNoContact.root.gone()
        }
    }

    override fun subscribeObservers() {

        viewModel.nearByList.observe(this) {
            mAdapter.setDataSet(it)
            sharedViewModel.nearMeCount.value = it.size
            sharedViewModel.setSelected()
            updateUiState()
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
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