package com.oreo.ui.chatGpt

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.FragmentChatGptBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChatGptOverview
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatGptFragment : BaseFragment<FragmentChatGptBinding>(FragmentChatGptBinding::inflate) {


    private val viewModel: ChatGptViewModel by viewModels()

    private val mAdapter: ChatGptAdapter by lazy {
        ChatGptAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_ai_page_visit)
        setAdapter()
        viewModel.sendInitMessage()
    }

    private fun setAdapter() {
        with(binding.rv) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        mAdapter.itemClickListener = { item, position ->
            when (item) {
                is ChatGptOverview.SentMessage -> {

                }

                is ChatGptOverview.ReceivedMessage -> {

                }

                is ChatGptOverview.RetryMessage -> {
                    viewModel.retryApi()

                }

                is ChatGptOverview.ThinkingMessage -> {

                }
            }
        }
    }

    override fun initListener() {

        binding.lytChatBox.btnNewChat.setOnClickListener {
            navigate(ChatGptFragmentDirections.actionChatGptFragmentSelf())
        }

        binding.lytChatBox.btnSendMessage.setOnClickListener {
            if (viewModel.fetchInProgress.value == true) return@setOnClickListener
            sendMessage()
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytChatWelcome.btnContinue.setOnClickListener {
            binding.lytChatWelcome.root.gone()
        }


        binding.lytChatBox.chatEtx.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        })


    }

    fun sendMessage() {
        val message = binding.lytChatBox.chatEtx.text.toString()
        if (message.isNotEmpty()) {
            viewModel.addSentMessage(message)
            viewModel.addThinkingMessage()

            //viewModel.addReceivedMessage("", true)
            binding.lytChatBox.chatEtx.setText("")

            viewModel.askQuestionStream(message)

            //viewModel.askQuestion(message)
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_ai_message_submit)
        }

    }

    override fun subscribeObservers() {

        viewModel.fetchInProgress.observe(this) {
            if (it) {
                binding.lytChatBox.chatEtx.isEnabled = false
            } else {
                binding.lytChatBox.chatEtx.isEnabled = true
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
        viewModel.scrollToBottom.observe(this) {
            it.getContent()?.let {
                binding.rv.smoothScrollToPosition(mAdapter.getItemCount() - 1)
            }
        }

        viewModel.chatGptOverview.observe(this) {
            it?.let {
                mAdapter.items = it
            }
        }
    }

}