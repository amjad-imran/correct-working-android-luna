package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
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
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.RecyclerView as RV

@AndroidEntryPoint
class LifeOsDashFragment :
    BaseFragment<FragmentLifeOsDashBinding>(FragmentLifeOsDashBinding::inflate) {

    private val viewModel: LifeOsDashViewModel by viewModels()

    private val questionsAdapter by lazy {
        LifeOsQuestionAdapter {

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
        setupInsightsPager()
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

        binding.lytDashInsights.ivMore.setOnClickListener {
            navigate(R.id.lifeOsInsightFrag)
        }
    }

    override fun subscribeObservers() {
        viewModel.questions.observe(viewLifecycleOwner) { list ->
            questionsAdapter.submit(list)
        }

        viewModel.whatsNew.observe(viewLifecycleOwner) { list ->
            binding.lytDashWhatsNew.tvTitle.text = "What’s new with Life OS?"
            binding.lytDashWhatsNew.tvVersion.text = "Version 1.2"
            whatsNewAdapter.submit(list)
        }
    }

    private fun setupInsightsPager() {
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
