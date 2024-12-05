package com.oreo.ui.chatGpt.functions

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.allViews
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentWorkoutPlansBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ai.TopQuestions
import com.oreo.ui.chatGpt.topquestions.AiTopQuestionsViewModel
import com.oreo.ui.chatGpt.topquestions.QuestionItem
import com.oreo.ui.compose.element.button.ButtonSecondary
import com.oreo.ui.compose.element.button.CircularBackButton
import com.oreo.ui.compose.element.button.CircularHistoryButton
import com.oreo.ui.compose.element.button.CircularImageButton
import com.oreo.ui.compose.element.button.Loading
import com.oreo.ui.compose.styles.FontStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutPlansFragment :
    BaseFragment<FragmentWorkoutPlansBinding>(FragmentWorkoutPlansBinding::inflate) {

    val viewModel: WorkoutPlanViewModel by viewModels()

    val mAdapter: AiWorkoutAdapter by lazy { AiWorkoutAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_workout_plan)

        viewModel.getWorkoutPlans()
        setRecycler()
    }


    override fun initListener() {
        binding.lytWeek.tvMon.setOnClickListener(weekListener)
        binding.lytWeek.tvTue.setOnClickListener(weekListener)
        binding.lytWeek.tvWed.setOnClickListener(weekListener)
        binding.lytWeek.tvThu.setOnClickListener(weekListener)
        binding.lytWeek.tvFri.setOnClickListener(weekListener)
        binding.lytWeek.tvSat.setOnClickListener(weekListener)
        binding.lytWeek.tvSun.setOnClickListener(weekListener)

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.selectedPosition.observe(this){
            showSelected(it)
        }
        viewModel.workoutList.observe(this) {
            mAdapter.setDataSet(it)
            binding.lytRestDay.root.setVisibilityByCondition(it.isEmpty())
        }
        viewModel.dayTitle.observe(this){
            binding.tvDayName.text = it
            binding.tvDayName.setVisibilityByCondition(it.isNotEmpty())
        }
    }


    private fun setRecycler() {
        binding.rvWorkouts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWorkouts.adapter = mAdapter
    }


    private val weekListener = OnClickListener { v ->
        val tag = v?.tag?.toString()?.toIntOrNull()
        tag?.let {
            viewModel.setSelectedPosition(it)
        }
    }


    private fun showSelected(selectedTag: Int) {
        val main = binding.lytWeek
        val layouts = arrayListOf(
            main.tvMon,
            main.tvTue,
            main.tvWed,
            main.tvThu,
            main.tvFri,
            main.tvSat,
            main.tvSun
        )
        layouts.forEach {
            if (it.tag.toString().toInt() == selectedTag) {
                it.setBackgroundResource(R.drawable.bg_week_selected)
            } else {
                it.setBackgroundResource(0)
            }
        }
    }
}
/*

@Preview
@Composable
fun EditPlanPreview() {
    EditPlanView(onCancelClicked = {}, onProceedClicked = {})
}

@Composable
private fun EditPlanView(onCancelClicked: () -> Unit, onProceedClicked: () -> Unit) {
    LOGS.d("sdfksjdkfjhksdjf Created")

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.text_ai_edit_plan),
            style = FontStyle.SIZE_16,
            color = Color(0xB2FFFFFF),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .height(42.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
                    .clickable {
                        LOGS.d("sdfksjdkfjhksdjf cancelled 1")
                        onCancelClicked()
                    },
                text = stringResource(R.string.text_cancel),
                style = FontStyle.SIZE_16,
                color = Color(0xFFDB4343),
                textAlign = TextAlign.Center,
            )

            ButtonSecondary(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .zIndex(1f),
                42.dp, stringResource(R.string.text_proceed)
            ) {
                onProceedClicked()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WorkoutPlanScreenPreview() {
    WorkoutPlanScreen(onWorkoutClicked = {})
}

@Composable
fun WorkoutPlanScreen(onWorkoutClicked: (workout: String) -> Unit) {

    val viewModel: WorkoutPlanViewModel = hiltViewModel()

    val showEditScreen by viewModel.showEditScreen.collectAsState()
    val loading by viewModel.getLoading().collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (showEditScreen) {
            Box(modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    CircularImageButton(onClick = {
                        viewModel.showEditScreen(false)
                    }) {
                        Image(
                            painter = painterResource(R.drawable.ic_edit_ai),
                            contentDescription = "Edit"
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    EditPlanView(onCancelClicked = {
                        viewModel.showEditScreen(false)
                    }, onProceedClicked = {
                        viewModel.showEditScreen(false)
                    })
                }
            }
        }

        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .blur(if (showEditScreen) 60.dp else 0.dp)
        ) {
            WorkoutPlanToolbar(onBackClicked = {

            }, onEditClicked = {
                viewModel.showEditScreen(true)
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
        if (loading) {
            Loading()
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

*/
/*@Preview
@Composable
fun WorkoutListPreview() {
    WorkoutList(
        Modifier.fillMaxWidth(),
        arrayListOf("one", "one", "one", "one")
    ) {

    }
}*//*


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

}*/
