package com.oreo.ui.chatGpt.history

import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt

import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentChatHistoryBinding
import com.noisefit_commans.common.MarginTopItemDecoration
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.ScreenUtils
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.lifeos.LifeOsChatFragment
import com.oreo.util.uiUtils.GenerateCustomDrawables
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

            override fun onDeleteThreadClicked(threadId: String) {
                viewModel.deleteChatHistoryServer(threadId){
                    onDeleteSuccess(threadId)
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUi()
        setRecycler()
        viewModel.getChatHistory()
    }

    private fun setUi() {
        binding.tvTitle.text = getString(R.string.text_recents)

        binding.searchBox.background = GenerateCustomDrawables.chatHistorySearchBar(
            backgroundColor = "#11141C".toColorInt()
        )
    }


    override fun initListener() {

        binding.etSearch.doAfterTextChanged { editable ->
            viewModel.masterList.let { list ->
                val query = editable?.toString()?.trim().orEmpty()

                if (query.isEmpty()) {
                    mAdapter.submitList(viewModel.masterList)
                    return@doAfterTextChanged
                }

                val filteredThreads = viewModel.masterList
                    .filter { !it.isHeader }
                    .filter {
                    it.title.orEmpty().lowercase().contains(query, true) ||
                    it.message.orEmpty().lowercase().contains(query, true)
                    }

                mAdapter.submitList(filteredThreads)
            }
        }

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
            viewModel.masterList = it
            mAdapter.submitList(it)
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

            // avoid adding decoration multiple times if setRecycler() called again
            if (itemDecorationCount == 0) {
                val marginItemDecoration =
                    MarginTopItemDecoration(screenUtils.dpToPx2(8, this.context))
                addItemDecoration(marginItemDecoration)
            }
        }
    }

    fun onDeleteSuccess(deletedThreadId: String) {
        viewModel.masterList = viewModel.masterList.filterNot { it.threadId == deletedThreadId }

        mAdapter.submitList(viewModel.masterList)
    }

}