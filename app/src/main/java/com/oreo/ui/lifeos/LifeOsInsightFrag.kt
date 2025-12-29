package com.oreo.ui.lifeos

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur

@AndroidEntryPoint
class LifeOsInsightFrag :
    BaseFragment<FragmentLifeOsInsightBinding>(FragmentLifeOsInsightBinding::inflate) {

    private val viewModel: LifeOsInsightsViewModel by viewModels()

    private val insightAdapter by lazy {
        LifeOsInsightListAdapter { insightItem ->
            navigate(
                R.id.lifeOsInsightDetailsFragment,
                Bundle().apply { putParcelable("insightData", insightItem) }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBlur()
        setupRecycler()
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

    override fun initListener() {
        binding.lytInsightMore.btnTalkToLifeOs.setOnClickListener {
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
        binding.lytToolbar.ivBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytToolbar.ivNewChat.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            navigate(frag,bundle)
        }

        binding.llMainScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY == 0) {
                binding.blurView.gone()
            } else {
                binding.blurView.visible()
            }
        }
    }

    private fun setupRecycler() {
        binding.rvInsights.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = insightAdapter
        }
    }

    override fun subscribeObservers() {
        viewModel.cards.observe(viewLifecycleOwner) { list ->
            if(list.isEmpty()){
                binding.llMainScrollView.gone()
                binding.lytDashInsightsEmpty.visible()
            }else{
                binding.lytDashInsightsEmpty.gone()
                binding.llMainScrollView.visible()
                insightAdapter.submitList(list)
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
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }
}
