package com.noisefit.ui.friends.reactions.paginate

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.FragmentReactionsBinding
import com.noisefit.ui.common.*
import com.noisefit_commans.data.model.UserFriendData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import dagger.hilt.android.AndroidEntryPoint

private const val POST_ID = "POST_ID"
private const val EMOJI_TYPE = "EMOJI_TYPE"
private const val NEXT_PAGE = "NEXT_PAGE"
private const val INITIAL_DATA = "INITIAL_DATA"


@AndroidEntryPoint
class ReactionsFragmentPaginate :
    BaseFragment<FragmentReactionsBinding>(FragmentReactionsBinding::inflate) {


    private val viewModel: ReactionViewModel by viewModels()


    private val mAdapter: ReactionsAdapterPaginate by lazy {
        ReactionsAdapterPaginate(object : ReactionActions {
            override fun onUserClicked(userId: Long) {
                tryCatch {
                    val stringUri = "https://noisefit.page.link/FriendsProfile?id=$userId"
                    val request = NavDeepLinkRequest.Builder.fromUri(stringUri.toUri()).build()
                    navigate(request)
                }
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(
            postId: Long,
            emojiType: Int,
            initialReactionData: List<UserFriendData>?,
            hasNext: Boolean?
        ) =
            ReactionsFragmentPaginate().apply {
                arguments = Bundle().apply {
                    putLong(POST_ID, postId)
                    putInt(EMOJI_TYPE, emojiType)
                    putBoolean(NEXT_PAGE, hasNext ?: false)
                    if (initialReactionData != null) {
                        putParcelableArray(INITIAL_DATA, initialReactionData.toTypedArray())
                    }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.postId = it.getLong(POST_ID)
            viewModel.reactionType = if (it.getInt(EMOJI_TYPE) == -1) {
                null
            } else {
                it.getInt(EMOJI_TYPE)
            }
            val initialData =
                it.getParcelableArray(INITIAL_DATA)?.toList() as? List<UserFriendData>?
            if (initialData != null) {
                viewModel.page = 1
                viewModel.has_next = it.getBoolean(NEXT_PAGE)
                viewModel.reactionsData.value = Event(initialData)
            }
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()

        if (viewModel.reactionsData.value?.peekContent() == null) {
            viewModel.getPostReactionsPaginate(viewModel.postId ?: -1, viewModel.reactionType)
        } else {
            mAdapter.setDataSet(viewModel.reactionsData.value?.getContent() ?: ArrayList())
        }
    }

    private fun setRecycler() {
        with(binding.rv) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }


    override fun initListener() {

        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.isReactionsApiLoading) {
                        return
                    }

                    nullableBinding?.let {
                        val visibleItemCount = it.rv.layoutManager?.childCount ?: 0
                        val totalItemCount = it.rv.layoutManager?.itemCount ?: 0
                        val firstVisibleItemPosition =
                            (it.rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            viewModel.getPostReactionsPaginate(
                                viewModel.postId ?: -1, viewModel.reactionType
                            )
                        }
                    }

                }
            }

        })

    }

    override fun subscribeObservers() {

        viewModel.reactionsData.observe(this) {
            it.getContent()?.let { content ->
                mAdapter.insertData(content)
            }
        }

        viewModel.getInitialLoading().observe(this) {
            if (it) {
                binding.progressBarInitial.visible()
            } else {
                binding.progressBarInitial.gone()
                binding.progressBarLoadMore.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoadMoreLoading().observe(this) {
            if (it) {
                binding.progressBarLoadMore.visible()
                binding.progressBarInitial.gone()
            } else {
                binding.progressBarLoadMore.gone()
                binding.progressBarInitial.gone()
            }
        }

    }
}