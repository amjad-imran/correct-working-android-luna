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

        setAdapter()
    }

    private fun setAdapter() {
        with(binding.rv) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        mAdapter.itemClickListener = { view, item, position ->
            when (item) {
                is ChatGptOverview.SentMessage -> {

                }

                is ChatGptOverview.ReceivedMessage -> {

                }

            }
        }
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytChatWelcome.btnContinue.setOnClickListener {
            binding.lytChatWelcome.root.gone()
        }

        binding.lytChatBox.btnSend.setOnClickListener {
            val message = binding.lytChatBox.chatEtx.text.toString()
            if (message.isEmpty()) {
                return@setOnClickListener
            }

            viewModel.addSentMessage(message)
            viewModel.addReceivedMessage("", true)
            binding.lytChatBox.chatEtx.setText("")
            viewModel.askQuestion(message)
        }

        binding.lytChatBox.chatEtx.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val message = binding.lytChatBox.chatEtx.text.toString()
                if (message.isNotEmpty()) {


                    viewModel.addSentMessage(message)
                    viewModel.addReceivedMessage("", true)
                    binding.lytChatBox.chatEtx.setText("")

                    viewModel.askQuestion(message)
                }
                true
            } else false
        })


    }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.lytChatBox.btnSend.disable()
                binding.lytChatBox.chatEtx.disable()
            } else {
                binding.lytChatBox.btnSend.enable()
                binding.lytChatBox.chatEtx.enable()
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

        viewModel.chatGptOverview.observe(this) {
            it?.let {
                mAdapter.items = it
            }
        }
    }

}