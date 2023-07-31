package com.noisefit.ui.friends.profile.badges

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFriendBadgeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.profile.FriendProfileSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendBadgeFragment :
    BaseFragment<FragmentFriendBadgeBinding>(FragmentFriendBadgeBinding::inflate) {

    val sharedViewModel: FriendProfileSharedViewModel by activityViewModels()

    val badgeAdapter by lazy {
        FriendBadgeAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()
        sharedViewModel.getBadgesList()
    }

    fun setRecycler() {
        with(binding.rvBadges) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = badgeAdapter
        }

    }

    override fun initListener() {
        binding.lytToolbar.view1.gone()
        val title: String = if (sharedViewModel.isMyProfile())
            getString(R.string.text_my_badges)
        else
            sharedViewModel.name + "'s badges"
        binding.lytToolbar.tvTitle.text = title

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }


    override fun subscribeObservers() {
        sharedViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        sharedViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        sharedViewModel.badgeListingResponse.observe(this) { data ->
            data?.let {
                badgeAdapter.setDataSet(it)
                if (data.isEmpty()) {
                    binding.lytNoData.root.visible()
                    binding.rvBadges.gone()
                } else {
                    binding.lytNoData.root.gone()
                    binding.rvBadges.visible()
                }
            }


        }

    }


}