package com.noisefit.ui.friends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.model.BuddiesUserNew
import com.noisefit.databinding.FragmentCommonFriendBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommonFriendFragment :
    BaseFragment<FragmentCommonFriendBinding>(FragmentCommonFriendBinding::inflate) {

    private val viewModel: CommonFriendsViewModel by viewModels()
    private val args: CommonFriendFragmentArgs by navArgs()


    private val mAdapter: CommonFriendsListAdapter by lazy {
        CommonFriendsListAdapter(object : OnItemClickListener {
            override fun onItemClicked(friendUser: BuddiesUserNew) {
                navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", friendUser.id ?: -1)
                })
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
        viewModel.friendId = args.friendId
        binding.toolbar.tvTitle.text = getString(R.string.text_common_friends)
        setRecycler()
        viewModel.getCommonFriends()
    }

    private fun setRecycler() {
        with(binding.rvInterest) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}

        })

    }


    override fun subscribeObservers() {
        viewModel.friendsList.observe(this) {
            mAdapter.setDataSet(it)
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

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

    }

    private fun updateUiState() {
        binding.lytNoContact.root.gone()
        binding.searchBox.visible()
        binding.etSearch.visible()
    }


}