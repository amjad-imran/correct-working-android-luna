package com.oreo.ui.chatGpt.topquestions

import android.os.Bundle
import android.view.View
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noisefit.luna.databinding.FragmentAiTopQuestionsBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.compose.styles.FontStyle
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AiTopQuestionsFragment :
    BaseFragment<FragmentAiTopQuestionsBinding>(FragmentAiTopQuestionsBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ScreenAiTopQuestion(onBackClicked = {
                    navigateUpSafe()
                }, onHistoryClicked = {

                }, onQuestionSelected = {

                })
            }
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}

@Composable
fun ScreenAiTopQuestion(
    onBackClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
    onQuestionSelected: (question: String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        Text(
            text = "Hi Amit,\n" + "Ask me anything !",
            style = FontStyle.SIZE_24,
            lineHeight = 32.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        QuestionList(arrayListOf("Question 1", "Question 2", "Question 3"))
    }
}


@Composable
fun QuestionList(questions: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(questions) { ques ->
            QuestionItem(ques)
        }
    }
}

@Composable
fun QuestionItem(quest: String) {
    val backgroundColor = Color(0xFF162536)

    Surface(
        shape = RoundedCornerShape(16.dp),  // Adjust corner radius as needed
        color = backgroundColor, modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = quest, style = FontStyle.SIZE_14, modifier = Modifier.padding(14.dp)
        )
    }
}


/*@Preview
@Composable
fun QuestionListPreview() {
    QuestionList(arrayListOf("Ques 1", "Ques 2", "Ques 3"))
}*/

@Preview
@Composable
fun ScreenAiTopQuestionPreview() {
    ScreenAiTopQuestion(onBackClicked = {}, onHistoryClicked = {}, onQuestionSelected = {})
}