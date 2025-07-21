package com.oreo.ui.circadianAlignment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.FragmentQuizCircadianBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.QuizQuestionCircadianDataModel

class QuizCircadianFragment : BaseFragment<FragmentQuizCircadianBinding>(FragmentQuizCircadianBinding::inflate) {

    private val questionAdapter: QuizQuestionAdapter by lazy {
        QuizQuestionAdapter(){
            handleQuizClick(it)
        }
    }

    private fun handleQuizClick(ques: QuizQuestionCircadianDataModel) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvQuestions.layoutManager = layoutManager
        binding.rvQuestions.adapter = questionAdapter

        questionAdapter.updateDataSet(getQuesData())

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvQuestions)

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

    }

    override fun subscribeObservers() {

    }

    private fun getQuesData(): List<QuizQuestionCircadianDataModel>{
        return listOf(
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            )
        )
    }

}