package com.oreo.ui.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLearnBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.LearnModel
import com.oreo.ui.home.summary.OSummaryHealthOverviewAdapter
import com.oreo.ui.home.summary.OSummaryHealthOverviewClickEnum
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LearnFragment : BaseFragment<FragmentLearnBinding>(FragmentLearnBinding::inflate) {

    val viewModel: LearnViewModel by viewModels()

    private val mAdapter by lazy {
        LearnAdapter(object : LearnAdapterActions {
            override fun onClicked(data: LearnModel) {
                if (data.type.equals("video", true)) {
                    navigate(R.id.ringInfoPlayerFragment, Bundle().apply {
                        this.putString("videoUrl", data.url)
                    })
                } else {
                    navigate(R.id.ringCareFragment)

                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.include29.tvTitle.text = "Learn more"

        setRecycler()
    }

    private fun setRecycler() {
        binding.rvLearn.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    override fun initListener() {
        binding.include29.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.learnDataList.observe(this) {
            mAdapter.setDataSet(it)
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
}