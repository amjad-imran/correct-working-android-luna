package com.oreo.ui.chatGpt.topquestions

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAiTopQuestionsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.oreo.data.model.ai.TopQuestions
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
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
                    },
                    onAiAudioClicked = {
                        navigate(
                            AiTopQuestionsFragmentDirections.actionAiTopQuestionsFragmentToAudioAiFragment(
                                null
                            )
                        )
                    },
                    onHistoryClicked = {
                        navigate(R.id.chatHistoryFragment)
                        //navigate(AiTopQuestionsFragmentDirections.actionAiTopQuestionsFragmentToChatHistoryFragment())
                    }, onQuestionSelected = { ques ->
                        if (viewModel.ringDataStore.getRingDevice() == null) {
                            context.showShortToast(getString(R.string.text_luna_ai_message))
                            return@ScreenAiTopQuestion
                        }

                        navigate(
                            AiTopQuestionsFragmentDirections.actionAiTopQuestionsFragmentToChatGptFragment(
                                "",
                                "",
                                ques,
                                "",
                                navArgs.aiTopic,
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
    onAiAudioClicked: () -> Unit,
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
        onAiAudioClicked,
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
    onAiAudioClicked: () -> Unit,
    onQuestionSelected: (question: String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AiHistoryToolbar(
                showHistoryIcon,
                onBackClicked = onBackClicked,
                onHistoryClicked = onHistoryClicked
            )

            QuestionList(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
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
            },
            onAiAudioClicked = {
                onAiAudioClicked()
            }
        )

        if (loading) {
            Loading()
        }
    }

}

@Preview
@Composable
fun AskQuestionPreview() {
    AskQuestion(modifier = Modifier,
        onSendClicked = {

        }, onAiAudioClicked = {

        })
}

@Composable
fun AskQuestion(
    modifier: Modifier,
    onSendClicked: (String) -> Unit,
    onAiAudioClicked: () -> Unit
) {

    var text by remember { mutableStateOf("") }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp, color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(size = 52.dp)
                ),
            placeholder = {
                Text(
                    text = stringResource(R.string.text_type_something),
                    style = FontStyle.SIZE_16,
                    color = Color.LightGray
                )
            },
            textStyle = FontStyle.SIZE_16.copy(lineHeight = 20.sp),
            maxLines = 2,
            minLines = 1,
            shape = RoundedCornerShape(52.dp),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSendClicked(text)
                },
            ),
        trailingIcon = {
            if (text.isNotEmpty()) {
                Image(
                    painter = painterResource(R.drawable.ic_ai_send_message_2),
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

        if(text.isEmpty()){
            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .background(
                    color = Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(size = 48.dp)
                )
                .clickable {
                    onAiAudioClicked()
                }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ai_mic_black),
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .padding(10.dp),
                    contentDescription = "Audio AI",
                )
            }
        }
    }
}

/*@Preview
@Composable
fun QuestionListPreview() {
    QuestionList(modifier = Modifier, questions = arrayListOf(
        TopQuestions("Ques 1"),
        TopQuestions("Ques 2"),
        TopQuestions("Ques 3")
    ), onQuesClicked = {})
}*/

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

/*@Preview
@Composable
fun QuestionItemPreview(){
    QuestionItem(TopQuestions("Question here"), onQuesClicked = {})
}*/

@Composable
fun QuestionItem(quest: TopQuestions, onQuesClicked: (ques: String) -> Unit) {
    val backgroundColor = Color(0x00000000)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(width = 2.dp, color = Color(0x66FFFFFF)),
        modifier = Modifier
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
            verticalAlignment = Alignment.Bottom
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

/*@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AiTopQuestionMainPreview() {
    AiTopQuestionMain(
        arrayListOf(
            TopQuestions("Ques 1"), TopQuestions("Ques 2"),
            TopQuestions("Ques 3")
        ),
        true,
        false,
        "Deepak",
        onBackClicked = {},
        onHistoryClicked = {},
        onAiAudioClicked = {},
        onQuestionSelected = {})
}*/
