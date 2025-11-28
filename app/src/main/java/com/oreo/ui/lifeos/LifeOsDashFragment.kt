package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsDashBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.RecyclerView as RV

@AndroidEntryPoint
class LifeOsDashFragment :
    BaseFragment<FragmentLifeOsDashBinding>(FragmentLifeOsDashBinding::inflate) {

    private val viewModel: LifeOsDashViewModel by viewModels()

    private val questionsAdapter by lazy {
        LifeOsQuestionAdapter { data ->
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = data,
                title = null,
                aiTopic = AITopics.GENERAL,
            )
            navigate(
                frag, bundle
            )
        }
    }

    private val whatsNewAdapter by lazy { LifeOsWhatsNewAdapter() }

    private val insights by lazy {
        listOf(
            "Your circadian rhythm shifted by 45 minutes later this week.",
            "You slept 20m longer on average compared to last week.",
            "Your activity score is trending up for 3 days."
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuestionsRecycler()
        setupWhatsNewRecycler()
//        setupInsightsPager(list)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getLifeOsData()
        setLytOnboardQuesProgress()
    }

    private fun setLytOnboardQuesProgress() {
        val onboardQuesData = viewModel.localDataStore.getLifeOsOnboardData()
        if(onboardQuesData==null || onboardQuesData.questions.isNullOrEmpty() || onboardQuesData.answers.isNullOrEmpty()){
            binding.lytHeader.lytOnboardQuesProgress.root.gone()
            return
        }
        val quesSize = onboardQuesData.questions!!.size

        var ansSize = 0
        onboardQuesData.answers?.forEach {
            if (it.ans_id.isNotEmpty() || it.addOntext.isNotEmpty()) {
                ansSize++
            }
        }

        if(ansSize >= quesSize){
            binding.lytHeader.lytOnboardQuesProgress.root.gone()
            return
        }

        binding.lytHeader.lytOnboardQuesProgress.root.visible()

        binding.lytHeader.lytOnboardQuesProgress.composeView.setContent {
            GradientBorderCard(ansSize/quesSize.toFloat())
        }
    }

    @Composable
    private fun GradientBorderCard(curProgress: Float) {
        val borderWidth = 2.dp
        val cornerRadius = 16.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = borderWidth,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x1AFFFFFF),
                            Color(0x00FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clip(RoundedCornerShape(cornerRadius - borderWidth))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.3f)
                        )
                    ),
                    alpha = 0.01f
                )
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Image(
                        painter = painterResource(R.drawable.ic_lifeos_star_small),
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        text = getString(R.string.text_get_more_from_life_os),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(com.noisefit_commans.R.font.gilroy_medium)),
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color(0x19FFFFFF),
                                shape = CircleShape
                            )
                            .background(
                                color = Color(0x1EFFFFFF),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_arrow_planner_right),
                            contentDescription = null,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = getString(R.string.text_tell_us_how_you_live_work_and_recover_luna_will_personalise_your_experience_to_help_you_live_better),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(com.noisefit_commans.R.font.gilroy_regular)),
                        fontWeight = FontWeight(400),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(16.dp))

                GradientProgressBar(curProgress)
            }
        }
    }

    @Composable
    fun GradientProgressBar(curProgress: Float) {
        val progress = curProgress.coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.8f)
            ) {
                val barWidth = size.width * progress

                if (barWidth <= 0f) return@Canvas

                val brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color(red = 1f, green = 1f, blue = 0.72f),
                        0.29f to Color(red = 0.65f, green = 0.92f, blue = 0.97f),
                        0.52f to Color(red = 0.68f, green = 0.64f, blue = 0.97f),
                        0.75f to Color(red = 0.95f, green = 0.78f, blue = 0.94f),
                        1.00f to Color(red = 1f, green = 1f, blue = 0.72f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f)
                )

                drawRect(
                    brush = brush,
                    topLeft = Offset.Zero,
                    size = Size(
                        width = barWidth,
                        height = size.height
                    )
                )
            }
        }
    }

    private fun setupQuestionsRecycler() {
        binding.lytHeader.rvQuestions.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = questionsAdapter
            clipToPadding = false
            setPadding(16f.dpToPixel().toInt(), 0, 16f.dpToPixel().toInt(), 0)

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val spacing = 8f.dpToPixel().toInt()
                    outRect.right = spacing
                    if (position == 0) outRect.left = spacing
                }
            })
        }
    }

    private fun setupWhatsNewRecycler() {
        binding.lytDashWhatsNew.rvNewFeatureList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = whatsNewAdapter
        }
    }

    override fun initListener() {

        binding.lytHeader.ivPersonalize.setOnClickListener {
            navigate(R.id.personalizeLifeOsFragment)
        }

        binding.lytToolbar.ivNewChat.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            findNavController().navigate(
                frag, bundle
            )
        }

        binding.lytHeader.lytChatBox.chatEtx.apply {
            setCursorVisible(false)
            setFocusable(false)
            setFocusableInTouchMode(false)
            setClickable(true)
        }
        binding.lytHeader.lytChatBox.root.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            findNavController().navigate(
                frag, bundle
            )
        }
        binding.lytHeader.lytChatBox.chatEtx.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            findNavController().navigate(
                frag, bundle
            )
        }

        binding.lytHeader.lytChatBox.btnAction.setOnClickListener {
            val (frag, bundle) = AudioAiFragment.getStartData(
                PlanType.NONE
            )
            navigate(frag, bundle)
        }

        binding.lytToolbar.ivHistory.setOnClickListener {
            navigate(R.id.chatHistoryFragment)
        }

        binding.lytToolbar.view1.setOnClickListener {
            navigate(R.id.lifeOsInsightFrag)
        }

        binding.lytDashInsights.ivMore.setOnClickListener {
            navigate(R.id.lifeOsInsightFrag)
        }

        binding.lytHeader.lytOnboardQuesProgress.root.setOnClickListener {
            navigate(R.id.lifeOsOnboardingQuesFragment)
        }
    }

    override fun subscribeObservers() {
        viewModel.destinationData.observe(this){
            if(it==null) return@observe
            setDestination(it)
        }

        viewModel.questions.observe(viewLifecycleOwner) { list ->
            questionsAdapter.submit(list)
        }

        viewModel.whatsNew.observe(viewLifecycleOwner) { list ->
            binding.lytDashWhatsNew.tvTitle.text = "What’s new with Life OS?"
            binding.lytDashWhatsNew.tvVersion.text = "Version 1.2"
            whatsNewAdapter.submit(list)
        }

        viewModel.insightsCardsData.observe(viewLifecycleOwner){ list ->
            if(list.isEmpty()){
                binding.lytDashInsights.root.gone()
                binding.lytDashInsightsEmpty.root.visible()
            }else{
                binding.lytDashInsightsEmpty.root.gone()
                binding.lytDashInsights.root.visible()
                setupInsightsPager(list)
            }
        }
    }

    private fun setDestination(dest: LifeOsDashViewModel.LifeOsDestinations) {
        when(dest){
            LifeOsDashViewModel.LifeOsDestinations.BEGIN_FRAG -> {
                navigate(R.id.lifeOsOnboardBeginFragment)
            }
            LifeOsDashViewModel.LifeOsDestinations.QUES_FRAG -> {
                navigate(R.id.lifeOsOnboardingQuesFragment)
            }
            LifeOsDashViewModel.LifeOsDestinations.LIFE_OS_MAIN -> {
            }
        }
        viewModel.destinationData.value = null
    }

    private fun setupInsightsPager(insights: List<InsightCardUiModel>) {
        val pager = binding.lytDashInsights.vpInsights
        pager.adapter = LifeOsInsightsPagerAdapter(this, insights)
        pager.offscreenPageLimit = 3
        pager.clipToPadding = false
        pager.clipChildren = false
        (pager.getChildAt(0) as? RV)?.overScrollMode = RV.OVER_SCROLL_NEVER

        val transformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(10f.dpToPixel().toInt()))

        }
        pager.setPageTransformer(transformer)
    }
}
