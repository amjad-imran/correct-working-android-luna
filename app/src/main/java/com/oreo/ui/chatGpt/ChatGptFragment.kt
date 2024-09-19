package com.oreo.ui.chatGpt

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentChatGptBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChatGptOverview
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatGptFragment : BaseFragment<FragmentChatGptBinding>(FragmentChatGptBinding::inflate) {
    companion object {
        fun getStartData(
            threadId: String?,
            defaultMessage: String?,
            userMessage: String?,
            title: String?
        ): Pair<Int, Bundle?> {
            return Pair(R.id.chatGptFragment, Bundle().apply {
                putString("threadId", threadId ?: "")
                putString("defaultMessage", defaultMessage ?: "")
                putString("userMessage", userMessage ?: "")
                putString("title", title ?: "")
            })
        }
    }


    private val viewModel: ChatGptViewModel by viewModels()
    private val args: ChatGptFragmentArgs by navArgs()

    private val mAdapter: ChatGptAdapter by lazy {
        ChatGptAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.threadId = args.threadId
        viewModel.defaultMessage = args.defaultMessage
        viewModel.userMessage = args.userMessage

        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_ai_page_visit)
        setAdapter()

        if (viewModel.threadId.isNullOrEmpty()) {
            viewModel.generateThreadId()
        } else {
            viewModel.loadMessagesByThreadId(viewModel.threadId!!)
            viewModel.threadTitle.postValue(args.title)
        }
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

        binding.ivHistory.setOnClickListener {
            navigate(ChatGptFragmentDirections.actionChatGptFragmentToChatHistoryFragment())
        }

        /* binding.lytChatBox.btnNewChat.setOnClickListener {
             navigate(ChatGptFragmentDirections.actionChatGptFragmentSelf("",""))
         }*/

        binding.lytChatBox.btnSendMessage.setOnClickListener {
            if (viewModel.fetchInProgress.value == true) {
                viewModel.stopResponseGeneration()
            } else {
                if (binding.lytChatBox.chatEtx.text.isNullOrEmpty().not()) {
                    sendMessage()
                }
            }
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
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

    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onDestroyView() {
        super.onDestroyView()
        requireView().viewTreeObserver.removeOnGlobalLayoutListener(keyboardListener)
    }

    override fun subscribeObservers() {

        keyboardListener = ViewTreeObserver.OnGlobalLayoutListener {
            view?.let {
                val insets = ViewCompat.getRootWindowInsets(it)
                val isKeyboardVisible = insets?.isVisible(WindowInsetsCompat.Type.ime())
                if (viewModel.threadTitle.value.isNullOrEmpty().not()) {
                    if (isKeyboardVisible == true) {
                        binding.tvChatTitle.gone()
                    } else {
                        binding.tvChatTitle.visible()
                    }
                }
            }
        }
        requireView().viewTreeObserver.addOnGlobalLayoutListener(keyboardListener)

        viewModel.threadTitle.observe(this) {
            binding.tvChatTitle.apply {
                visible()
                text = it
            }
        }

        viewModel.fetchInProgress.observe(this) {
            if (it) {
                binding.lytChatBox.chatEtx.isEnabled = false
                binding.lytChatBox.chatEtx.setText(getString(R.string.text_generating_data))
                binding.lytChatBox.chatEtx.setTextColor(android.graphics.Color.parseColor("#9ecfff"))
                binding.lytChatBox.btnSendMessage.setImageResource(R.drawable.ic_round_stop_circle)
            } else {
                binding.lytChatBox.chatEtx.isEnabled = true
                binding.lytChatBox.chatEtx.setText("")
                binding.lytChatBox.chatEtx.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                binding.lytChatBox.btnSendMessage.setImageResource(R.drawable.ic_ai_send_message)
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
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
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