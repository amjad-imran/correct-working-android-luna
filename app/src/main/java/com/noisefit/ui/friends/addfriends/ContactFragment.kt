package com.noisefit.ui.friends.addfriends

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.model.BuddiesUserNew
import com.noisefit.luna.databinding.FragmentContactBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_ADD_STRING
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_REMOVE_STRING
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

private val PERMISSIONS_REQUEST_READ_CONTACTS = 111

@AndroidEntryPoint
class ContactFragment : BaseFragment<FragmentContactBinding>(FragmentContactBinding::inflate) {

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
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTACTS_ADD_CLICK)
            }

            override fun onRemoveClicked(friendUser: BuddiesUserNew, position: Int) {
                clickedItemPos = position
                viewModel.removeFriendRequest(friendUser.id ?: -1, FRIEND_STATUS_REMOVE_STRING) {
                    mAdapter.updateStatusToAdd(position)
                    updateUiState()
                }
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTACTS_REMOVE_CLICK)
            }

            override fun onItemClicked(friendUser: BuddiesUserNew) {
                navigate(
                    AddFriendsFragmentDirections.actionAddFriendsFragmentToFriendProfileFragment()
                        .apply { friendId = friendUser.id ?: -1 })
            }

            override fun onDataFiltered(hasUser: Boolean) {
                if (hasUser) {
                    nullableBinding?.lytNoContact?.root?.gone()
                } else {
                    nullableBinding?.lytNoContact?.apply {
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

    private fun updateUiState() {
        val userList = mAdapter.itemCount
        binding.lytNoContact.textViewTitle.text = getString(R.string.text_no_contacts_found)
        binding.lytNoContact.textViewMsg.text =
            getString(R.string.text_fitenss_health_is_better_with_friends)
        binding.lytNoContact.imageViewNoChallenge.setImageResource(R.drawable.ic_no_contact_found)
        binding.lytNoContact.lytJoin.visible()
        binding.lytNoContact.bJoinChallenge.text = getString(R.string.text_invite_friends)
        binding.lytNoContact.bJoinChallenge.setBackgroundResource(R.drawable.tab_layout_bg)

        if (userList == 0) {
            binding.searchBox.gone()
            binding.etSearch.gone()
            binding.rvContacts.gone()
            binding.lytNoContact.root.visible()
        } else {
            binding.searchBox.visible()
            binding.etSearch.visible()
            binding.rvContacts.visible()
            binding.lytNoContact.root.gone()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LOGS.d("ON_VIEW_CREATED,", "ContactFragment")
        setRecycler()
        checkContactsPermission()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchPhoneContacts()
            } else {
                showPermissionDenialDialog(true)
            }
        }

    }

    private fun fetchPhoneContacts() {
        binding.lytNoPermission.root.gone()
        viewModel.fetchPhoneContacts()
    }

    private fun showPermissionDenialDialog(isPermissionDenial: Boolean) {


        binding.searchBox.gone()
        binding.lytNoContact.root.gone()
        binding.rvContacts.gone()


        binding.lytNoPermission.apply {
            root.visible()
            tvDesc.makeLinks(
                true,
                Pair("Privacy Policy", View.OnClickListener {
                    activity?.let {
                        startActivity(
                            WebViewActivity.getStartIntent(
                                it,
                                getString(R.string.text_privacy_policy),
                                AppConstants.URL_PRIVACY_POLICY
                            )
                        )
                    }

                })
            )
            btnContinue.setOnClickListener {
                if (isPermissionDenial) {
                    ShareUtil.openAppPermissionSettings(context)
                } else {
                    requestPermissions(
                        Array(1) { Manifest.permission.READ_CONTACTS },
                        PERMISSIONS_REQUEST_READ_CONTACTS
                    )
                }
            }
        }
    }

    private fun setRecycler() {
        with(binding.rvContacts) {
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
            ShareUtil.shareText(requireContext(), viewModel.text)
        }
    }

    override fun subscribeObservers() {
        viewModel.noiseFitAppUsers.observe(this) { userList ->

            if (userList.isNotEmpty())
                viewModel.localDataStore.setNoiseFitContactCount(userList.size)
            else
                viewModel.localDataStore.setNoiseFitContactCount(0)
            mAdapter.setDataSet(userList)
            sharedViewModel.contactCount.value = userList.size
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

    private fun checkContactsPermission() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            showPermissionDenialDialog(false)


        } else fetchPhoneContacts()
    }


}