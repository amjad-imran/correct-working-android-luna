package com.oreo.ui.chatGpt.topquestions

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import com.noisefit.luna.databinding.FragmentAiTopQuestionsBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AiTopQuestionsFragment :
    BaseFragment<FragmentAiTopQuestionsBinding>(FragmentAiTopQuestionsBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ScreenAiTopQuestion(
                    onBackClicked = {
                        navigateUpSafe()
                    },
                    onHistoryClicked = {

                    },
                    onQuestionSelected = {

                    }
                )
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
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Hi Amit,\n" +
                    "Ask me anything !",

            )
    }
}

@Preview
@Composable
fun ScreenAiTopQuestionPreview() {
    ScreenAiTopQuestion(onBackClicked = {}, onHistoryClicked = {}, onQuestionSelected = {})
}