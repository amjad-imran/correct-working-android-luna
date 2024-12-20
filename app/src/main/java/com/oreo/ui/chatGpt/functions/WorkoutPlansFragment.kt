package com.oreo.ui.chatGpt.functions

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentWorkoutPlansBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutPlansFragment :
    BaseFragment<FragmentWorkoutPlansBinding>(FragmentWorkoutPlansBinding::inflate) {

    private val viewModel: WorkoutPlanViewModel by viewModels()
    private val mAdapter: AiWorkoutAdapter by lazy {
        AiWorkoutAdapter(onWorkoutSelected = {
            navigate(R.id.aiWorkoutDetailFragment, bundleOf("data" to it.toTypedArray()))
        })
    }

    private val mainViewModel: OreoMainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_workout_plan)

        viewModel.getWorkoutPlans()
        setRecycler()

        mainViewModel.addWorkoutCtaVisibility.postValue(true)
    }


    override fun initListener() {
        binding.lytWeek.tvMon.setOnClickListener(weekListener)
        binding.lytWeek.tvTue.setOnClickListener(weekListener)
        binding.lytWeek.tvWed.setOnClickListener(weekListener)
        binding.lytWeek.tvThu.setOnClickListener(weekListener)
        binding.lytWeek.tvFri.setOnClickListener(weekListener)
        binding.lytWeek.tvSat.setOnClickListener(weekListener)
        binding.lytWeek.tvSun.setOnClickListener(weekListener)

        binding.ivEdit.setOnClickListener {
            navigate(WorkoutPlansFragmentDirections.actionWorkoutPlansFragmentToChatGptFragment(
                "",
                "",
                getString(R.string.text_build_me_a_workout_plan),
                "",
                AITopics.GENERAL,
                PlanType.WORKOUT
            ))
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.selectedPosition.observe(this) {
            showSelected(it)
        }

        viewModel.workoutList.observe(this) {
            mAdapter.setDataSet(it ?: ArrayList())
            binding.lytRestDay.root.setVisibilityByCondition(it.isNullOrEmpty())
        }

        viewModel.dayTitle.observe(this) {
            binding.tvDayName.text = it
            binding.tvDayName.setVisibilityByCondition(it.isNullOrEmpty().not())
        }

        viewModel.currentSelectedWeekDayPosition.observe(this) { selectedPos ->
            val main = binding.lytWeek

            val views = arrayListOf(
                main.selection1,
                main.selection2,
                main.selection3,
                main.selection4,
                main.selection5,
                main.selection6,
                main.selection7
            )
            views.forEach {
                if (it.tag.toString().toInt() == selectedPos) {
                    it.visible()
                } else {
                    it.invisible()
                }
            }
        }

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
