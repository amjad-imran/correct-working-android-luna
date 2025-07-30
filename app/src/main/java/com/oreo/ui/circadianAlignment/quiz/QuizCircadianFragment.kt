package com.oreo.ui.circadianAlignment.quiz

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentQuizCircadianBinding
import com.noisefit_commans.ui.BaseFragment
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
        viewModel.getQuizData()
        setAdapter()
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

        val layoutManager = SingleScrollLinearLayoutManager(requireContext())
        binding.rvQuestions.layoutManager = layoutManager

        // Calculate padding: (RecyclerView height / 2) - (approx item height / 2)
        val screenHeight = resources.displayMetrics.heightPixels
        val estimatedItemHeight = 300 // Adjust this if your item layout is taller/shorter
        val padding = (screenHeight / 2) - (estimatedItemHeight / 2)

        binding.rvQuestions.addItemDecoration(CenterPaddingItemDecoration(padding))

        binding.rvQuestions.adapter = questionAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvQuestions)

        binding.rvQuestions.post {
            val snapView = snapHelper.findSnapView(layoutManager)
            snapView?.let {
                val position = layoutManager.getPosition(it)
                questionAdapter.setFocusedIndex(position)
            }
        }

        binding.rvQuestions.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    val snapView = snapHelper.findSnapView(layoutManager)
                    val centerPos = layoutManager.getPosition(snapView!!)
                    questionAdapter.setFocusedIndex(centerPos)
                }
            }
        })
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvSkip.setOnClickListener {
            viewModel.submitQuizQuesAndAnswers(false)
        }
    }

    override fun subscribeObservers() {
        viewModel.quizData.observe(this){
            val list:List<CircadianQuizResponseModel> = it
            questionAdapter.updateDataSet(list)
        }

        viewModel.quizDataSubmitted.observe(this){
            it.getContent()?.let {
                navigate(R.id.circadianAlignmentFragment)
            }
        }
    }

}