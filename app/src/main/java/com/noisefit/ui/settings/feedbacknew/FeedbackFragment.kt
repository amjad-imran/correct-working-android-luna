package com.noisefit.ui.settings.feedbacknew

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFeedback2Binding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FeedbackFragment : BaseFragment<FragmentFeedback2Binding>(FragmentFeedback2Binding::inflate) {

    // val defaultEmojiVisibility =0.4f

    private val viewModel: FeedbackNewViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        binding.layoutToolbar.tvTitle.text = getString(com.noisefit_commans.R.string.text_rate_us)

        viewModel.getQuestionData()
    }


    override fun initListener() {


        hideViews(0)

        binding.lvAnimFirst.setOnClickListener {
            viewModel.rating = 1
            hideViews(1)

            handleEmoji(1)
            setUIData(0)
        }
        binding.lvAnimSecond.setOnClickListener {
            viewModel.rating = 2
            hideViews(1)

            handleEmoji(2)
            setUIData(1)
        }
        binding.lvAnimThird.setOnClickListener {
            viewModel.rating = 3
            hideViews(1)

            handleEmoji(3)
            setUIData(2)

        }
        binding.lvAnimFourth.setOnClickListener {
            viewModel.rating = 4
            hideViews(1)

            handleEmoji(4)
            setUIData(3)
        }
        binding.lvAnimFifth.setOnClickListener {
            viewModel.rating = 5
            hideViews(1)

            handleEmoji(5)
            setUIData(4)
        }
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.bAcceptContinue.setOnClickListener {
            val appSuggestion = binding.lytProblemDescription.appSuggestionEtv.text.toString()

            val ids: List<Int> = binding.lytChipView.chipsPrograms.checkedChipIds
            if (viewModel.problemTypeList.isNotEmpty())
                viewModel.problemTypeList.clear()
            for (id in ids) {
                val chip: Chip = binding.lytChipView.chipsPrograms.findViewById(id)
                viewModel.problemTypeList.add(chip.text.toString())
            }
            val problemType = viewModel.problemTypeList.joinToString { it }
            if (viewModel.problemTypeList.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_choose_one_problem_validation_msg),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                uiController.hideSoftKeyboard()
                viewModel.submitFeedbackNew(
                    viewModel.provideFeedbackNewData(
                        viewModel.rating,
                        problemType,
                        appSuggestion.replace("\\s+".toRegex(), " ")
                    )
                )
            }
        }
    }

    private fun handleEmoji(star: Int) {
        when (star) {
            1 -> {

                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimThird.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star_border)
            }
            2 -> {
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star)
                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimThird.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star_border)
            }
            3 -> {
                binding.lvAnimThird.setImageResource(R.drawable.ic_star)
                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star)
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star_border)
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star_border)
            }
            4 -> {
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star)
                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star)
                binding.lvAnimThird.setImageResource(R.drawable.ic_star)
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star_border)
            }
            5 -> {
                binding.lvAnimFifth.setImageResource(R.drawable.ic_star)
                binding.lvAnimFirst.setImageResource(R.drawable.ic_star)
                binding.lvAnimSecond.setImageResource(R.drawable.ic_star)
                binding.lvAnimThird.setImageResource(R.drawable.ic_star)
                binding.lvAnimFourth.setImageResource(R.drawable.ic_star)
            }
        }
    }

    private fun setUIData(position: Int) {
        binding.lytChipView.tvQuestionTitle.text = viewModel.questionList[position].question
        setCategoryChips(viewModel.questionList[position].problem_array)
    }


    private fun hideViews(visibility: Int) {
        if (visibility == 0) {
            binding.lytChipView.root.gone()
            binding.lytProblemDescription.root.gone()
            binding.divider1.root.gone()
            binding.divider2.root.gone()
            binding.bAcceptContinue.gone()
        } else {
            binding.lytChipView.root.visible()
            binding.lytProblemDescription.root.visible()
            binding.divider1.root.visible()
            binding.divider2.root.visible()
            binding.bAcceptContinue.visible()
        }
    }


    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
        viewModel.feedbackQuestion.observe(this) {
            viewModel.questionList = it
        }

        viewModel.submittedSuccessfully.observe(this) {
            if (it) {
                if (viewModel.rating == 4 || viewModel.rating == 5) {
                    setFragmentResultListener(RATE_NOW) { key, bundle ->
                        val isSelected = bundle.getBoolean("isSelected")
                        if (isSelected) {
                            ShareUtil.openPlayStore(requireContext(), "com.noisefit")
                            navigateUpSafe()
                        }
                    }
                    setFragmentResultListener(LATER) { key, bundle ->
                        val isSelected = bundle.getBoolean("isSelected")
                        if (isSelected) {
                            navigateUpSafe()
                        }
                    }
                    navigate(R.id.rateNowBottomSheet,Bundle().apply {
                        putString("cameFrom","feedback")
                    })
                } else {
                    uiController.onDisplayError(getString(R.string.text_feedback_successful))
                    navigateUpSafe()
                }

            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                navigateUpSafe()
            }
        }

    fun setCategoryChips(category: List<String>?) {
        binding.lytChipView.chipsPrograms.removeAllViews()
        if (category != null) {
            for (item in category) {
                val mChip: Chip =
                    layoutInflater.inflate(R.layout.item_chip_feedback, null, false) as Chip
                mChip.text = item
                val paddingDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10F,
                    resources.displayMetrics
                )
                mChip.setPadding(paddingDp.toInt(), 0, paddingDp.toInt(), 0)
                binding.lytChipView.chipsPrograms.addView(mChip)
            }
        }
    }

}