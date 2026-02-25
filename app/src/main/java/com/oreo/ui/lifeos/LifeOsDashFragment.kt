package com.oreo.ui.lifeos

import android.os.Build
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
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsDashBinding
import com.noisefit.oreo.BottomNavOption
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.common.MarginLeftRightItemDecoration
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import com.oreo.ui.lifeos.onboarding.LifeOsOnboardBeginFragment
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import kotlin.getValue

@AndroidEntryPoint
class LifeOsDashFragment :
    BaseFragment<FragmentLifeOsDashBinding>(FragmentLifeOsDashBinding::inflate) {

    private val viewModel: LifeOsDashViewModel by viewModels()

    private val mainViewModel: OreoMainViewModel by activityViewModels()

    private var startTime: Long = 0
    private var spentTime: Long = 0

    private val questionsAdapter by lazy {
        LifeOsQuestionAdapter { data ->
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_suggested_q_clicked,
                hashMapOf(
                    "source" to "LifeOS tab [main]",
                    "question" to data
                )
            )

            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = data,
                title = null,
                aiTopic = AITopics.GENERAL,
                srcKey = "lifeos",
            )
            navigate(
                frag, bundle
            )
        }
    }

    private val insightAdapter by lazy {
        LifeOsInsightListAdapter(true){ insightItem ->
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_insight_card_clicked
            )

            navigate(
                R.id.lifeOsInsightDetailsFragment,
                Bundle().apply { putParcelable("insightData", insightItem) }
            )
        }
    }

    private val whatsNewAdapter by lazy {
        LifeOsWhatsNewAdapter { position ->
            navigate(R.id.whatsNewInLifeOsFragment, Bundle().apply {
                putInt("blogPosition", position)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBlur()
        setUi()
        setupQuestionsRecycler()
        setupWhatsNewRecycler()
//        setupInsightsPager(list)
    }

    private fun setBlur() {
        val activity = requireActivity()

        val radius = 20f;
        val decorView = activity.window.decorView;
        val rootView = binding.root
        val windowBackground = decorView.background

        val blurAlgo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffectBlur()
        } else {
            RenderScriptBlur(activity)
        }
        binding.blurView.setupWith(rootView, blurAlgo) // or RenderEffectBlur
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)

    }

    private fun setUi() {
        binding.lytHeader.tvTitleWithUserName.text = viewModel.getGreetText()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getOnboardingData()
    }

    private fun setLytOnboardQuesProgress(total: Int, ansMarked: Int) {
        if(ansMarked >= total){
            binding.lytHeader.lytOnboardQuesProgress.root.gone()

            binding.lytHeader.imageView117.visible()
            binding.lytHeader.textView205.visible()
            binding.lytHeader.ivPersonalize.visible()
            return
        }else{
            binding.lytHeader.ivPersonalize.invisible()
            binding.lytHeader.imageView117.invisible()
            binding.lytHeader.textView205.invisible()
        }

        binding.lytHeader.lytOnboardQuesProgress.root.visible()

        setProgress((ansMarked.toFloat() / total.toFloat() * 100).toInt())
    }

    fun setProgress(progress: Int) {
        binding.lytHeader.lytOnboardQuesProgress.progressBar.progress = progress
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
                            fontFamily = FontFamily(Font(com.noisefit_commans.R.font.google_sans_flex_medium)),
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
                        fontFamily = FontFamily(Font(com.noisefit_commans.R.font.google_sans_flex_medium)),
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
        binding.lytDashInsights.rvInsights.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = insightAdapter
            PagerSnapHelper().attachToRecyclerView(this)
            addItemDecoration(
                MarginLeftRightItemDecoration(
                    12
                )
            )
        }


        binding.lytDashWhatsNew.rvNewFeatureList.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = whatsNewAdapter
        }
    }

    override fun initListener() {

        binding.lytHeader.ivPersonalize.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_personalize_clicked
            )
            navigate(R.id.personalizeLifeOsFragment)
        }

        binding.lytToolbar.ivNewChat.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL,
                srcKey = "lifeos",
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
                aiTopic = AITopics.GENERAL,
                srcKey = "lifeos",
            )
            findNavController().navigate(
                frag, bundle
            )
        }

        binding.lytHeader.lytChatBox.ivAddAttachment.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL,
                srcKey = "lifeos",
                displayAddAttachmentBS = true,
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
                aiTopic = AITopics.GENERAL,
                srcKey = "lifeos",
            )
            findNavController().navigate(
                frag, bundle
            )
        }

        binding.lytHeader.lytChatBox.btnAction.setOnClickListener {
            navigate(if (mainViewModel.getUserSelectedPersona().isNotEmpty()) {
                R.id.lifeOsVoiceChatFragment
            } else {
                R.id.choosePersonaVoiceFragment
            }, bundleOf("source" to "lifeos_tab_chat"))
        }

        binding.ivVoiceAI.setOnClickListener {
            navigate(if (mainViewModel.getUserSelectedPersona().isNotEmpty()) {
                R.id.lifeOsVoiceChatFragment
            } else {
                R.id.choosePersonaVoiceFragment
            }, bundleOf("source" to "lifeos_tab_bottom"))
        }

        binding.lytToolbar.ivHistory.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_history_view,
                hashMapOf(
                    "source" to "LifeOS tab [main]",
                )
            )
            navigate(R.id.chatHistoryFragment)
        }

        binding.lytToolbar.view1.setOnClickListener {
            navigate(R.id.lifeOsInsightFrag)
        }

        binding.lytDashInsights.ivMore.setOnClickListener {
            navigate(R.id.lifeOsInsightFrag)
        }

        binding.lytHeader.lytOnboardQuesProgress.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_onboarding_complete
            )
            viewModel.totalQuesAnsResp = null
            navigate(R.id.lifeOsOnboardingQuesFragment)
        }

        binding.svMain.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY == 0) {
                binding.blurView.gone()
            } else {
                binding.blurView.visible()
            }
        }
    }

    override fun subscribeObservers() {

        viewModel.uiStateData.observe(this){
            if(it==null) return@observe
            setUiState(it)
        }

        viewModel.questions.observe(viewLifecycleOwner) { list ->
            questionsAdapter.submit(list)
        }

        viewModel.whatsNew.observe(viewLifecycleOwner) { data ->
            binding.lytDashWhatsNew.tvTitle.visible()
            binding.lytDashWhatsNew.tvTitle.text = getString(R.string.text_what_s_new_with_life_os)
            whatsNewAdapter.submit(data)
        }

        viewModel.insightsCardsData.observe(viewLifecycleOwner){ list ->
            binding.insightsProgressBar.root.gone()
            if(list.isEmpty()){
                binding.lytDashInsights.root.gone()
                binding.lytDashInsightsEmpty.root.visible()
            }else{
                insightAdapter.submitList(list)
                binding.lytDashInsightsEmpty.root.gone()
                binding.lytDashInsights.root.visible()
            }
        }

        //
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.mainProgressBar.root.visible()
            } else {
                binding.mainProgressBar.root.gone()
            }
        }
    }

    private fun setUiState(states: LifeOsDashViewModel.LifeOsDashUiStates) {
        when(states){
            is LifeOsDashViewModel.LifeOsDashUiStates.LifeOsMain -> {
                binding.svMain.visible()
                binding.lytToolbar.root.visible()
                setLytOnboardQuesProgress(states.total, states.ansMarked)
            }

            LifeOsDashViewModel.LifeOsDashUiStates.BeginFrag -> {
                setFragmentResultListener(LifeOsOnboardBeginFragment.LIFE_OS_ONBOARD_BEGIN_KEY){ _, bundle ->
                    val isBackClicked = bundle.getBoolean("isBackClicked")
                    if(isBackClicked){
                        navigateUpSafe()
                        mainViewModel.navigateTo(BottomNavOption.HOME)
                    }
                }
                navigate(R.id.lifeOsOnboardBeginFragment)
            }

            LifeOsDashViewModel.LifeOsDashUiStates.QuesFrag -> {
                navigate(R.id.lifeOsOnboardingQuesFragment)
            }

        }
        viewModel.uiStateData.value = null
    }

    override fun onStart() {
        super.onStart()

        startTime = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()

        spentTime = (System.currentTimeMillis() - startTime) / 1000
        viewModel.sessionManager.logMoEngageAppEvent(
            MoEngageLunaAppEvents.lifeos_home_viewed,
            hashMapOf(
                "source" to "footer, homepage, $spentTime seconds",
                "view_duration_ms" to "$spentTime seconds",
            )
        )
    }

}
