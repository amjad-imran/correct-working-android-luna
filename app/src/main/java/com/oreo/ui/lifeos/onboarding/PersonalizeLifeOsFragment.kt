package com.oreo.ui.lifeos.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentPersonalizeLifeOsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonalizeLifeOsFragment : BaseFragment<FragmentPersonalizeLifeOsBinding>(FragmentPersonalizeLifeOsBinding::inflate) {

    companion object {
        const val PERSONALIZE_QUES_ID_KEY = "personalizeQuesId"
    }

    private val viewModel: PersonalizeLifeOsViewModel by viewModels()
    private val mAdapter by lazy {
        PersonalizeQnaAdapter{ item ->
            navigate(
                R.id.lifeOsOnboardingQuesFragment,
                Bundle().apply {
                    putInt(PERSONALIZE_QUES_ID_KEY, item.id)
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getOnboardData()
        setUi()
        setRecycler()
    }

    private fun setUi() {
        binding.toolbar.apply {
            tvTitle.text = getString(R.string.text_personalise_life_os)
        }
    }

    private fun setRecycler() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.personalizeListData.observe(this){
            mAdapter.updateDataSet(it)
        }

        //
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
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

}