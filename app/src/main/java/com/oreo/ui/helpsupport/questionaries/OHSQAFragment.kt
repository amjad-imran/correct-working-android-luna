package com.oreo.ui.helpsupport.questionaries

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHSQuestionariesBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.displayToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OHSQAFragment :
    BaseFragment<FragmentOHSQuestionariesBinding>(FragmentOHSQuestionariesBinding::inflate) {
    private val mOHSQAViewModel: OHSQAViewModel by viewModels()
    private val args: OHSQAFragmentArgs by navArgs()
    private val mOHSQAAdapter: OHSQAAdapter by lazy {
        OHSQAAdapter(object : OHSQAAdapter.OnItemClickListener {
            override fun onItemClick(isExpanded: Boolean, position: Int) {
                mOHSQAAdapter.updateData(isExpanded, position)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvQa) {
            adapter = mOHSQAAdapter
        }
        mOHSQAAdapter.setData(mOHSQAViewModel.getQAData())
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = args.title
        binding.lytHelpful.ivThumbsUp.setOnClickListener {
            binding.lytHelpful.ivThumbsUp.alpha = 1.0f
            binding.lytHelpful.ivThumbsDown.alpha = 0.5f
            requireActivity().displayToast("Thank you for your feedback!")
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
                //todo redirect for call
            } else {
                binding.lytHelpful.ivThumbsDown.alpha = 0.5f
            }
        }
        navigate(R.id.badRatingBottomSheet)
    }

    override fun subscribeObservers() {

    }


}