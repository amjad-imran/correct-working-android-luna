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
import com.noisefit_commans.common.MarginItemDecoration
import com.noisefit_commans.common.MarginTopItemDecoration
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.ScreenUtils
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.custom.SwipeHelper
import com.oreo.ui.lifeos.LifeOsChatFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatHistoryFragment :
    BaseFragment<FragmentChatHistoryBinding>(FragmentChatHistoryBinding::inflate) {
    val viewModel: ChatHistoryViewModel by viewModels()

    @Inject
    lateinit var screenUtils: ScreenUtils

    private val mAdapter: ChatHistoryAdapter by lazy {
        ChatHistoryAdapter(object : ChatHistoryInteraction {
            override fun onThreadClicked(threadId: String, title: String) {
                if (viewModel.ringDataStore.getRingDevice() == null) {
                    context.showShortToast(getString(R.string.text_luna_ai_message))
                    return
                }

                val (frag, bundle) = LifeOsChatFragment.getStartData(
                    threadId = threadId,
                    userMessage = "",
                    title = title,
                    aiTopic = AITopics.GENERAL,
                    planType = PlanType.NONE,
                )
                navigate(frag, bundle)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.text_recents)
        setRecycler()
        viewModel.getChatHistory()
    }


    override fun initListener() {
        binding.ivBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivNewChat.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            navigate(
                frag, bundle
            )
        }

    }

    override fun subscribeObservers() {

        viewModel.chatHistory.observe(this) {
            mAdapter.setDataSet(it)
            if (it.isEmpty()) {
                binding.ivNoData.visible()
                binding.textNoData.visible()
            } else {
                binding.ivNoData.gone()
                binding.textNoData.gone()
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
            val marginItemDecoration = MarginTopItemDecoration(screenUtils.dpToPx2(8, this.context))
            addItemDecoration(marginItemDecoration)
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
                        UnderlayButton("",
                            R.drawable.ic_delete_ai_chat,
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