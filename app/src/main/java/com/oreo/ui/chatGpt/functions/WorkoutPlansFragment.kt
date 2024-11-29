package com.oreo.ui.chatGpt.functions

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentWorkoutPlansBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.ai.TopQuestions
import com.oreo.ui.chatGpt.topquestions.QuestionItem
import com.oreo.ui.compose.element.button.ButtonSecondary
import com.oreo.ui.compose.element.button.CircularBackButton
import com.oreo.ui.compose.element.button.CircularHistoryButton
import com.oreo.ui.compose.element.button.CircularImageButton
import com.oreo.ui.compose.styles.FontStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutPlansFragment :
    BaseFragment<FragmentWorkoutPlansBinding>(FragmentWorkoutPlansBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WorkoutPlanScreen(onEditClicked = {

                }, onWorkoutClicked = {

                })
            }

        }

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}

@Preview
@Composable
fun EditPlanPreview() {
    EditPlanView()
}

@Composable
private fun EditPlanView() {
    Text(
        text = stringResource(R.string.text_ai_edit_plan),
        style = FontStyle.SIZE_16,
        color = Color(0xB2FFFFFF),
    )


    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.text_cancel),
            style = FontStyle.SIZE_16,
            color = Color(0xFFDB4343),
            textAlign = TextAlign.Center,
        )

        ButtonSecondary(42.dp, stringResource(R.string.text_proceed)) {

        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WorkoutPlanScreenPreview() {
    WorkoutPlanScreen(onEditClicked = {}, onWorkoutClicked = {})
}

@Composable
fun WorkoutPlanScreen(onEditClicked: () -> Unit, onWorkoutClicked: (workout: String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        WorkoutPlanToolbar(onBackClicked = {

        }, onEditClicked = {
            onEditClicked()
        })

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Lorem ipsum dolor sit amet consectetur. Sed nisi sit purus malesuada pulvinar.",
                        style = FontStyle.SIZE_14,
                        color = Color(0xCCFFFFFF)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Last updated: 21 March ‘24",
                        style = FontStyle.SIZE_14,
                        color = Color(0x7AFFFFFF),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            items(
                arrayListOf(
                    true,
                    false,
                    false,
                    false,
                    true,
                    false,
                    false,
                    false,
                    true,
                    false,
                    false,
                    false
                )
            ) { item ->

                if (item) {
                    WorkoutHeader("Header name here")
                } else {
                    WorkoutItem(item) { selectedQues ->
                        onWorkoutClicked(selectedQues)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutHeader(headerText: String) {
    Text(
        text = headerText,
        style = FontStyle.SIZE_18,
        color = Color(0xFFE8E8E8),
        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
    )

}

/*@Preview
@Composable
fun WorkoutListPreview() {
    WorkoutList(
        Modifier.fillMaxWidth(),
        arrayListOf("one", "one", "one", "one")
    ) {

    }
}*/

@Composable
fun WorkoutItem(workout: Boolean, onWorkoutClicked: (workout: String) -> Unit) {
    val backgroundColor = Color(0xFF3D435D)

    Surface(
        shape = RoundedCornerShape(12.dp),  // Adjust corner radius as needed
        color = backgroundColor, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onWorkoutClicked("")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.image_ai_thinking),
                contentDescription = "image description",
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Flat Bench Press",
                    style = FontStyle.SIZE_16,
                    color = Color(0xE5FFFFFF),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "3 sets 10 reps",
                    style = FontStyle.SIZE_12,
                    color = Color(0xFF4BBEFF)
                )
            }
        }
    }
}

@Composable
fun WorkoutPlanToolbar(
    onBackClicked: () -> Unit, onEditClicked: () -> Unit
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

        Text(
            text = stringResource(R.string.text_workout_plan),
            style = FontStyle.SIZE_20,
            color = Color(255f, 255f, 255f, 0.8f)
        )

        CircularImageButton(onClick = {
            onEditClicked()
        }) {
            Image(
                painter = painterResource(R.drawable.ic_edit_ai),
                contentDescription = "Edit"
            )
        }

    }

}