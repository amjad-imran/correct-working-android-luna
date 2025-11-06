package com.oreo.ui.lifeos

import com.noisefit.luna.databinding.FragmentLifeOsDashBinding
import com.noisefit_commans.ui.BaseFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.noisefit.luna.R
import dagger.hilt.android.AndroidEntryPoint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.ui.dpToPixel

@AndroidEntryPoint
class LifeOsDashFragment :
    BaseFragment<FragmentLifeOsDashBinding>(FragmentLifeOsDashBinding::inflate) {

    private val viewModel: LifeOsDashViewModel by viewModels()

    private val questionsAdapter by lazy {
        LifeOsQuestionAdapter {

        }
    }

    private val whatsNewAdapter by lazy { LifeOsWhatsNewAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuestionsRecycler()
        setupWhatsNewRecycler()
    }

    private fun setupQuestionsRecycler() {
        binding.lytHeader.rvQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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
        binding.lytHeader.lytChatBox.root.setOnClickListener {
            val sharedView = binding.lytHeader.lytChatBox.root
            val transitionName = sharedView.transitionName
            val extras = FragmentNavigatorExtras(sharedView to transitionName)
            findNavController().navigate(
                R.id.action_navigation_lifeOsFragment_to_lifeOsChatFragment,
                null,
                null,
                extras
            )
        }

        binding.lytToolbar.ivHistory.setOnClickListener {
            navigate(R.id.chatHistoryFragment)
        }
    }

    override fun subscribeObservers() {
        viewModel.questions.observe(viewLifecycleOwner) { list ->
            questionsAdapter.submit(list)
        }

        viewModel.whatsNew.observe(viewLifecycleOwner) { list ->
            whatsNewAdapter.submit(list)
        }
    }
}
