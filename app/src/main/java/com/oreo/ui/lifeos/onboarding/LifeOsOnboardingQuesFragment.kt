package com.oreo.ui.lifeos.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsOnboardingQuesBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.lifeos.onboarding.quesChildFrags.LifeOsChildMcqSingleFragment
import com.oreo.ui.lifeos.onboarding.quesChildFrags.LifeOsOnboardTextFldOrNoneFragment
import com.oreo.ui.lifeos.onboarding.quesChildFrags.LifeosCheckBoxAndOtherFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsOnboardingQuesFragment : BaseFragment<FragmentLifeOsOnboardingQuesBinding>(FragmentLifeOsOnboardingQuesBinding::inflate) {

    val viewModel: LifeOsOnboardingQuesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {

            }

        viewModel.personalizeQuesId = arguments?.getInt(PersonalizeLifeOsFragment.PERSONALIZE_QUES_ID_KEY)
        viewModel.getOnboardQues(){
            navigateUpSafe()
        }
    }

    override fun initListener() {
        binding.viewSkip.setOnClickListener {
            viewModel.curQues.value?.let { ques ->
                viewModel.saveCurrentQues(ques.id, null)
            }
        }

        binding.ivBack.setOnClickListener {
            viewModel.switchToPrevQuestion()
        }

        binding.btnNext.setOnClickListener {
            viewModel.nextBtnClicked.postValue(true)
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.nextBtnClicked.postValue(true)
        }

        binding.btnClose.setOnClickListener {
            if(viewModel.personalizeQuesId!=null){
                navigateUpSafe()
                return@setOnClickListener
            }
            setFragmentResultListener(LIFEOS_ONBOARD_BS_KEY) { _, bundle ->
                val isSaveAndExit = bundle.getBoolean("saveAndExit")
                if(isSaveAndExit){
                    viewModel.saveAndExitBtnClickedBs.postValue(true)
//                    viewModel.submitQuesAnsToServer()
                }
            }
            navigate(R.id.lifeOSOnboardSkipOrConBottomSheet)
        }
    }

    private fun setLinearProgressIndicatorUi() {
        binding.btnSaveChanges.invisible()
        binding.tvTitle.invisible()

        binding.indicatorProgress.visible()
        binding.tvSkip.visible()
        binding.viewSkip.visible()
        binding.btnNext.visible()

        val totalQues = viewModel.onBoardResponseData?.questions?.size
        val curProgress = viewModel.curQuesIndex?.plus(1)
        if(totalQues==null || curProgress==null){
            /*binding.tvSkip.invisible()
            binding.viewSkip.invisible()*/
            return
        }

        binding.indicatorProgress.apply {
            if(this.max != totalQues) max = totalQues
            progress = curProgress
        }
        if(totalQues==curProgress){
            /*binding.tvSkip.invisible()
            binding.viewSkip.invisible()*/
            binding.btnNext.text = getString(R.string.text_done)
        }else{
            /*binding.tvSkip.visible()
            binding.viewSkip.visible()*/
            binding.btnNext.text = getString(R.string.text_next)
        }

        if(curProgress==1){
            binding.ivBack.invisible()
        }else{
            binding.ivBack.visible()
        }
    }

    override fun subscribeObservers() {
        viewModel.curQues.observe(this){
            viewModel.updateNextButtonState.postValue(it.answer.find { it.isSelected } != null)

            if(viewModel.personalizeQuesId==null) {
                setLinearProgressIndicatorUi()
            }else{
                setPersonalizeUi(it.personalizeTitle)
            }

            val childFrag = when(it.type){
                "mcq-single" -> {
                    LifeOsChildMcqSingleFragment().apply {
                        this.arguments = Bundle().apply {
                            putParcelable("question", it)
                        }
                    }
                }

                "mcq-multiple" -> {
                    LifeosCheckBoxAndOtherFragment().apply {
                        this.arguments = Bundle().apply {
                            putParcelable("question", it)
                        }
                    }
                }

                "text-none" -> {
                    LifeOsOnboardTextFldOrNoneFragment().apply {
                        this.arguments = Bundle().apply {
                            putParcelable("question", it)
                        }
                    }
                }

                else -> null
            }

            childFrag?.let {
                childFragmentManager.beginTransaction()
                    .replace(R.id.childFragmentContainer, childFrag)
                    .commit()
            }
        }

        viewModel.updateNextButtonState.observe(this){
            binding.btnNext.isEnabled = it
            binding.btnSaveChanges.isEnabled = it
        }

        viewModel.navigateToFinishScreen.observe(this){
            if(it){
                viewModel.navigateToFinishScreen.value = false
                navigateUpSafe()
                if(viewModel.personalizeQuesId==null)
                    navigate(R.id.lifeOsOnboardFinishFragment)
            }
        }

        //
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
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

    private fun setPersonalizeUi(personalizeTitle: String?) {
        binding.btnNext.invisible()
        binding.ivBack.invisible()
        binding.indicatorProgress.invisible()
        binding.viewSkip.invisible()
        binding.tvSkip.invisible()

        binding.btnClose.setImageResource(R.drawable.image_back_btn)
        binding.btnSaveChanges.visible()

        binding.tvTitle.apply {
            text = personalizeTitle ?: "Health Goals"
            visible()
        }
    }

}