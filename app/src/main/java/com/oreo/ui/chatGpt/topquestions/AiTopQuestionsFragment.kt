package com.oreo.ui.chatGpt.topquestions

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAiTopQuestionsBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.ai.TopQuestions
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.compose.element.button.CircularBackButton
import com.oreo.ui.compose.element.button.CircularHistoryButton
import com.oreo.ui.compose.element.button.Loading
import com.oreo.ui.compose.styles.FontStyle
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AiTopQuestionsFragment :
    BaseFragment<FragmentAiTopQuestionsBinding>(FragmentAiTopQuestionsBinding::inflate) {

    val viewModel: AiTopQuestionsViewModel by viewModels()

    private val navArgs: AiTopQuestionsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAiTopQuestions(navArgs.aiTopic)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ScreenAiTopQuestion(
                    onBackClicked = {
                        navigateUpSafe()
                    }, onHistoryClicked = {
                        navigate(R.id.chatHistoryFragment)
                        //navigate(AiTopQuestionsFragmentDirections.actionAiTopQuestionsFragmentToChatHistoryFragment())
                    }, onQuestionSelected = { ques ->
                        navigate(
                            AiTopQuestionsFragmentDirections.actionAiTopQuestionsFragmentToChatGptFragment(
                                "",
                                "",
                                ques,
                                "",
                                navArgs.aiTopic,
                                "",
                                PlanType.NONE
                            )
                        )
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
    val viewModel: AiTopQuestionsViewModel = hiltViewModel()

    val questions by viewModel.questions.collectAsState()
    val showHistoryIcon by viewModel.showHistoryIcon.collectAsState()
    val loading by viewModel.getLoading().collectAsState()
    val userName by viewModel.userName.collectAsState()

    AiTopQuestionMain(
        questions,
        showHistoryIcon,
        loading,
        userName,
        onBackClicked,
        onHistoryClicked,
        onQuestionSelected
    )


}

@Composable
fun AiTopQuestionMain(
    questions: List<TopQuestions>,
    showHistoryIcon: Boolean,
    loading: Boolean,
    userName: String,
    onBackClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
    onQuestionSelected: (question: String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.Top
        ) {
            AiHistoryToolbar(
                showHistoryIcon,
                onBackClicked = onBackClicked,
                onHistoryClicked = onHistoryClicked
            )
            Spacer(
                modifier = Modifier.height(32.dp)
            )
            Text(
                modifier = Modifier.padding(
                    horizontal = 26.dp
                ),
                text = if (userName.isEmpty()) {
                    "Hi,\n" + "Ask me anything !"
                } else {
                    "Hi $userName,\n" + "Ask me anything !"
                },
                style = FontStyle.SIZE_24,
                lineHeight = 32.sp
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            QuestionList(
                modifier = Modifier.padding(horizontal = 16.dp),
                questions
            ) { selectedQues ->
                onQuestionSelected(selectedQues)
            }


        }

        AskQuestion(
            Modifier
                .padding(bottom = 32.dp)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            onSendClicked = {
                onQuestionSelected(it)
            }
        )

        if (loading) {
            Loading()
        }
    }

}

@Composable
fun AskQuestion(
    modifier: Modifier,
    onSendClicked: (String) -> Unit
) {

    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = {
            Text(
                text = stringResource(R.string.text_type_something),
                style = FontStyle.SIZE_16,
                color = Color.LightGray
            )
        },
        textStyle = FontStyle.SIZE_16,
        singleLine = true,
        shape = RoundedCornerShape(52.dp),
        modifier = modifier,
        trailingIcon = {
            if (text.isNotEmpty()) {
                Image(
                    painter = painterResource(R.drawable.ic_ai_send_message),
                    modifier = Modifier.clickable {
                        onSendClicked(text)
                    },
                    contentDescription = "Send"
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xc009284c),
            unfocusedContainerColor = Color(0xc009284c),
            cursorColor = Color(0xFFb0e3ff),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedPlaceholderColor = Color(0X2effffff),
            unfocusedPlaceholderColor = Color(0x2effffff),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}


@Composable
fun QuestionList(
    modifier: Modifier, questions: List<TopQuestions>, onQuesClicked: (ques: String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(questions) { ques ->
            QuestionItem(ques) { selectedQues ->
                onQuesClicked(selectedQues)
            }
        }
    }
}

@Composable
fun QuestionItem(quest: TopQuestions, onQuesClicked: (ques: String) -> Unit) {
    val backgroundColor = Color(0xFF162536)

    Surface(
        shape = RoundedCornerShape(16.dp),  // Adjust corner radius as needed
        color = backgroundColor, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onQuesClicked(quest.question ?: "")
            }
    ) {
        Text(
            text = quest.question ?: "",
            style = FontStyle.SIZE_14,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
fun AiHistoryToolbar(
    showHistoryIcon: Boolean,
    onBackClicked: () -> Unit, onHistoryClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularBackButton(onClick = onBackClicked)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.image_luna_ai_logo),
                modifier = Modifier.height(16.dp),
                contentDescription = "Luna Ai image"
            )
            Spacer(
                modifier = Modifier.width(6.dp)
            )
            Image(
                painter = painterResource(R.drawable.image_luna_ai_version),
                modifier = Modifier.height(12.dp),
                contentDescription = "Ai version"
            )
        }
        if (showHistoryIcon) {
            CircularHistoryButton(onClick = onHistoryClicked)
        } else {
            Spacer(modifier = Modifier.width(38.dp))//width to be same as icon
        }
    }

}


/*@Preview
@Composable
fun QuestionListPreview() {
    QuestionList(arrayListOf("Ques 1", "Ques 2", "Ques 3"))
}*/

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AiTopQuestionMainPreview() {
    AiTopQuestionMain(
        arrayListOf(
            TopQuestions("Ques 1"), TopQuestions("Ques 2"),
            TopQuestions("Ques 3"), TopQuestions("Ques 1"), TopQuestions("Ques 2"),
            TopQuestions("Ques 3"), TopQuestions("Ques 1"), TopQuestions("Ques 2"),
            TopQuestions("Ques 3"), TopQuestions("Ques 1"), TopQuestions("Ques 2"),
            TopQuestions("Ques 3"), TopQuestions("Ques 1"), TopQuestions("Ques 2"),
            TopQuestions("Ques 3"),
        ),
        true,
        false,
        "Deepak",
        onBackClicked = {},
        onHistoryClicked = {},
        onQuestionSelected = {})
}