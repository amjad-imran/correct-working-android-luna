package com.oreo.ui.chatGpt.history

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentChatHistoryBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.custom.SwipeHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatHistoryFragment :
    BaseFragment<FragmentChatHistoryBinding>(FragmentChatHistoryBinding::inflate) {
    val viewModel: ChatHistoryViewModel by viewModels()

    private val mAdapter: ChatHistoryAdapter by lazy {
        ChatHistoryAdapter(object : ChatHistoryInteraction {
            override fun onThreadClicked(threadId: String) {

                navigate(
                    ChatHistoryFragmentDirections.actionChatHistoryFragmentToChatGptFragment(
                        threadId
                    )
                )
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()
        viewModel.getChatHistory()
    }


    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

        viewModel.chatHistory.observe(this) {
            mAdapter.setDataSet(it)
            if (it.isEmpty()) {
                navigateUpSafe()
            }
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

    private fun setRecycler() {
        with(binding.rv) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter
        }
        setSwipeHelper()
    }

    private fun setSwipeHelper() {
        object : SwipeHelper(requireContext(), binding.rv) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder?, underlayButtons: MutableList<UnderlayButton>?
            ) {
                if (viewHolder is ChatHistoryAdapter.ViewHolderThread) {
                    underlayButtons?.add(
                        UnderlayButton("Delete",
                            0,
                            Color.parseColor("#ff3358"),
                            ResourcesCompat.getFont(
                                requireContext(),
                                com.noisefit_commans.R.font.gilroy_medium
                            ),
                            object : UnderlayButtonClickListener {
                                override fun onClick(pos: Int) {
                                    val threadId = mAdapter.getThreadId(pos)
                                    threadId?.let {
                                        viewModel.deleteChatHistoryServer(it)
                                    }
                                }
                            })
                    )
                }
            }
        }
    }
}