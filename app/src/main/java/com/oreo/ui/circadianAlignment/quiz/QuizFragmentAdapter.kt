package com.oreo.ui.circadianAlignment.quiz

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.oreo.data.model.circadian.CircadianQuizResponseModel

class QuizFragmentAdapter(fragment: Fragment, private val questions: List<CircadianQuizResponseModel>, private val onOptionSelected: (Pair<Int?, Int>)->Unit) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun createFragment(position: Int): Fragment {
        return QuesVpFragment.newInstance(questions[position], onOptionSelected)
    }
}
