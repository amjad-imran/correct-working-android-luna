package com.oreo.ui.circadianAlignment.quiz

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentQuizCircadianBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.circadian.CircadianQuizResponseModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizCircadianFragment : BaseFragment<FragmentQuizCircadianBinding>(FragmentQuizCircadianBinding::inflate) {

    private val questionAdapter: QuizQuestionAdapter by lazy {
        QuizQuestionAdapter(){
            handleQuizOptionClick(it)
        }
    }

    private val viewModel : QuizCircadianViewModel by viewModels()

    private fun handleQuizOptionClick(pair: Pair<Int, Int>) {
        viewModel.handleQuizOptionClick(pair)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
        viewModel.getQuizData()
        setAdapter()
    }

    private fun setUi() {
        if(!viewModel.localDataStore.isCircadianOnboardShown()){
            val tvSkip = binding.tvSkip
            tvSkip.paintFlags = tvSkip.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            tvSkip.visible()
        }else {
            binding.tvSkip.gone()
        }
    }

    private fun setAdapter() {

       /*val layoutManager = object : LinearLayoutManager(context, RecyclerView.VERTICAL, false) {
            override fun smoothScrollToPosition(
                recyclerView: RecyclerView,
                state: RecyclerView.State,
                position: Int
            ) {

                val smoothScroller = object : LinearSmoothScroller(recyclerView.context){
                    override fun getVerticalSnapPreference(): Int {
                        return SNAP_TO_START
                    }
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return 100f / displayMetrics.densityDpi
                    }
                }

                smoothScroller.targetPosition = position
                startSmoothScroll(smoothScroller)

            }
        }*/
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvSkip.setOnClickListener {
            viewModel.submitQuizQuesAndAnswers()
        }

        binding.btnGetStarted.setOnClickListener {
            viewModel.submitQuizQuesAndAnswers()
        }
    }

    override fun subscribeObservers() {
        viewModel.quizData.observe(this){
            val list:List<CircadianQuizResponseModel> = it
//            questionAdapter.updateDataSet(list)
            val adapter = QuizFragmentAdapter(this, list){ pair ->
                handleQuizOptionClick(pair)
            }
            binding.viewPager.adapter = adapter

            // Enable vertical scrolling
            binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            //binding.viewPager.offscreenPageLimit = 2

            binding.viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT

            binding.viewPager.isUserInputEnabled = false


            // Apply the custom page transformer for positioning and scaling
            /*binding.viewPager.setPageTransformer { page, position ->
                val scaleFactor = Math.max(0.85f, 1 - Math.abs(position)) // Scale the page based on its position
                val maxTranslationY = 100f // Move items up/down based on their position

                // Adjust translation for vertical movement
                page.translationY = position * maxTranslationY

                // Apply scaling to pages as they move away from the center
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor

                // Adjust the opacity (fade out pages that are not centered)
                page.alpha = 1 - Math.abs(position)
            }*/
            binding.viewPager.setPageTransformer { page, position ->
                when {
                    position < -1 -> {
                        // Page is way off-screen to the top
                        page.alpha = 0f
                        page.scaleX = 0.8f
                        page.scaleY = 0.8f
                    }
                    position <= -0.1 -> {
                        // Page is slightly off-screen (peek from top)
                        page.alpha = 0.7f + (0.3f * (1 + position / 0.9f))
                        val scaleFactor = 0.8f + (0.2f * (1 + position / 0.9f))
                        page.scaleX = scaleFactor
                        page.scaleY = scaleFactor
                        page.translationY = -50 * (1 + position)
                    }
                    position <= 0.1 -> {
                        // Current page (center)
                        page.alpha = 1f
                        page.scaleX = 1f
                        page.scaleY = 1f
                        page.translationY = 0f
                    }
                    position <= 1 -> {
                        // Page is slightly off-screen (peek from bottom)
                        page.alpha = 0.7f + (0.3f * (1 - position / 0.9f))
                        val scaleFactor = 0.8f + (0.2f * (1 - position / 0.9f))
                        page.scaleX = scaleFactor
                        page.scaleY = scaleFactor
                        page.translationY = 50 * position
                    }
                    else -> {
                        // Page is way off-screen to the bottom
                        page.alpha = 0f
                        page.scaleX = 0.8f
                        page.scaleY = 0.8f
                    }
                }
            }
        }

        viewModel.optionSelectedLiveData.observe(this){
            binding.viewPager.adapter?.let { adapter ->
                val next =  binding.viewPager.currentItem + 1
                if (next < adapter.itemCount) {
                    binding.viewPager.setCurrentItem(next, true)
                }
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val lastPageIndex = (binding.viewPager.adapter?.itemCount ?: 1) - 1
                if(lastPageIndex >= 0 && position == lastPageIndex){
                    binding.btnGetStarted.apply {
                        text = if(viewModel.localDataStore.isCircadianOnboardShown()) getString(R.string.text_done)
                                else getString(R.string.text_get_started)
                        visible()
                    }
                }else{
                    binding.btnGetStarted.gone()
                }
            }
        })


        viewModel.quizDataSubmitted.observe(this){
            it.getContent()?.let {
                if(viewModel.localDataStore.isCircadianOnboardShown()){
                    navigateUpSafe()
                }else{
                    viewModel.localDataStore.setCircadianOnboardShown()
                    navigate(QuizCircadianFragmentDirections.actionQuizCircadianFragmentToCircadianAlignmentFragment())
                }
            }
        }

        // --
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

}