package com.noisefit.ui.settings.helpAndSupport.sublist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit_commans.data.response.HelpAndSupportQuestion
import com.noisefit.databinding.FragmentHelpAndSupportListBinding
import com.noisefit.ui.settings.helpAndSupport.HelpAndSupportViewModel
import com.noisefit_commans.common.MarginItemDecoration
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpAndSupportListFragment : BaseFragment<FragmentHelpAndSupportListBinding>(
    FragmentHelpAndSupportListBinding::inflate
), HelpAndSupportInteractionListener {

    private val viewModel: HelpAndSupportViewModel by viewModels()

    private val helpAndSupportListAdapter by lazy {
        HelpAndSupportListAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = HelpAndSupportListFragmentArgs.fromBundle(it)
            args.helpSupportItem?.let { helpSupportItem ->
                viewModel.setHelpAndSupport(helpSupportItem)
            }
            args.type?.let {
                binding.btnAnyOtherQuery.gone()
                viewModel.setHelpAndSupportType(it)
                viewModel.fetchHelpAndSupportData()
            }

        }


    }

    override fun initListener() {
        binding.toolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }

        }

        binding.btnAnyOtherQuery.setOnClickListener {
            navigateUpSafe()
        }
        setAdapter()
    }


    private fun setTitle(title: String) {
        binding.toolbar.apply {
            tvTitle.text = title
        }
    }

    private fun setAdapter() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = helpAndSupportListAdapter
            addItemDecoration(MarginItemDecoration(24))

        }
    }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.helpAndSupportResponse.observe(this) {
            it?.let {
                it.questionsList?.let { it1 -> helpAndSupportListAdapter.setDataSet(it1) }
                it.title?.let { it1 -> setTitle(it1) }
            }
        }
    }

    override fun onHelpAndSupportClick(helpAndSupportQuestion: HelpAndSupportQuestion) {
        helpAndSupportQuestion.id?.let {
            navigate(
                HelpAndSupportListFragmentDirections.actionNavigationHelpAndSupportListToDetailsFragment(
                    it
                )
            )
        }

    }


}