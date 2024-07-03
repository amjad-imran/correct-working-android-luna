package com.oreo.ui.chatGpt.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentChatHistoryBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.ChatGptAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatHistoryFragment :
    BaseFragment<FragmentChatHistoryBinding>(FragmentChatHistoryBinding::inflate) {
    val viewModel: ChatHistoryViewModel by viewModels()

    private val mAdapter: ChatHistoryAdapter by lazy {
        ChatHistoryAdapter(object : ChatHistoryInteraction {
            override fun onThreadClicked(threadId: String) {
                navigate(R.id.chatGptFragment)
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

        viewModel.chatHistory.observe(this){
            mAdapter.setDataSet(it)
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
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    private fun setRecycler() {
        with(binding.rv) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }
}