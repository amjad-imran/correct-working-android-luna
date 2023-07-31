package com.noisefit.ui.friends.reactions.paginate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.LayoutReactionsBottomSheetBinding
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.UserFriendData
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReactionsBottomSheetPaginate :
    BaseBottomSheetWithTransparent<LayoutReactionsBottomSheetBinding>(
        LayoutReactionsBottomSheetBinding::inflate
    ) {


    private lateinit var reactionsViewPager: ReactionsViewPagerPaginate


    private val viewModel: ReactionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.postId = ReactionsBottomSheetPaginateArgs.fromBundle(it).postId
        }

        if (viewModel.postId == null) {
            navigateUpSafe()
            return
        }

        viewModel.getInitialPostData(viewModel.postId ?: -1)

    }


    private fun setViewPager(
        emojis: List<Emoji>,
        initialReactionData: List<UserFriendData>?,
        hasNext: Boolean
    ) {
        reactionsViewPager = ReactionsViewPagerPaginate(
            this, viewModel.postId ?: -1, emojis, initialReactionData, hasNext
        )


        binding.viewPager1.apply {
            offscreenPageLimit = 1
            adapter = reactionsViewPager
            currentItem = 0
        }


        TabLayoutMediator(
            binding.tabLayout, binding.viewPager1
        ) { myTabLayout: TabLayout.Tab, position: Int ->
            myTabLayout.text = viewModel.tabTitleList[position]
            myTabLayout.icon = viewModel.tabIconList[position]
        }.attach()


    }


    override fun initListener() {

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBarDefault.visible()
            } else {
                binding.progressBarDefault.gone()
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                context.showShortToast(getString(R.string.text_something_went_wrong))
                navigateUpSafe()
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
                navigateUpSafe()
            }
        }

    }

    override fun subscribeObservers() {

        viewModel.reactionList.observe(this) {
            binding.divider.root.visible()
            setViewPager(
                it,
                viewModel.reactionsData.value?.getContent(),
                viewModel.has_next ?: false
            )
        }
    }

}