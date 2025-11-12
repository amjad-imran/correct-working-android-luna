package com.oreo.ui.helpsupport.questionaries

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.facebook.share.Share
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHSQuestionariesBinding
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.oreo.data.model.OHSQuestionariesResponseModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OHSQAFragment :
    BaseFragment<FragmentOHSQuestionariesBinding>(FragmentOHSQuestionariesBinding::inflate) {
    private val mOHSQAViewModel: OHSQAViewModel by viewModels()
    private val args: OHSQAFragmentArgs by navArgs()
    private val mOHSQAAdapter: OHSQAAdapter by lazy {
        OHSQAAdapter(object :OHSQAAdapter.OnItemClickListener{
            override fun onItemClick(data: OHSQuestionariesResponseModel) {
                mOHSQAViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_support_options_question_click,HashMap<String, Any>().apply {
                    this[MoEngageAppEventParams.question_id]=data.quesId
                    this[MoEngageAppEventParams.question_title]=data.question
                })
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        mOHSQAViewModel.getHSQAnswer(args.id)
    }

    private fun setRecycler() {
        with(binding.rvQa) {
            adapter = mOHSQAAdapter
        }

    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = args.title
        binding.lytHelpful.ivThumbsUp.setOnClickListener {
            binding.lytHelpful.ivThumbsUp.alpha = 1.0f
            binding.lytHelpful.ivThumbsDown.alpha = 0.5f
            requireActivity().displayToast(getString(R.string.text_thank_you_for_your_feedback))
        }
        binding.lytHelpful.ivThumbsDown.setOnClickListener {
            binding.lytHelpful.ivThumbsUp.alpha = 0.5f
            binding.lytHelpful.ivThumbsDown.alpha = 1.0f
            openBadRatingBottomSheet()
        }
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    private fun openBadRatingBottomSheet() {
        setFragmentResultListener(CALL_REQUEST_KEY) { _, bundle ->
            val isCall = bundle.getBoolean("call")
            if (isCall) {
                context?.let {
                    ShareUtil.composeEmail(it,"support@lunazone.com","")
                }
            } else {
                binding.lytHelpful.ivThumbsDown.alpha = 0.5f
            }
        }
        navigate(R.id.badRatingBottomSheet)
    }

    override fun subscribeObservers() {
        mOHSQAViewModel.hsqAnswerData.observe(this) {
            if (it.isNotEmpty()) {
                mOHSQAAdapter.setData(it as ArrayList<OHSQuestionariesResponseModel>)
                binding.container.visible()
            } else
                binding.container.gone()
        }

        mOHSQAViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        mOHSQAViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        mOHSQAViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }


}