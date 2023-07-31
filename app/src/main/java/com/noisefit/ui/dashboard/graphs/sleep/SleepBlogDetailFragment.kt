package com.noisefit.ui.dashboard.graphs.sleep

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.noisefit_commans.data.response.SleepBlogCategories
import com.noisefit.luna.databinding.FragmentSleepBlogDetailBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SleepBlogDetailFragment : BaseFragment<FragmentSleepBlogDetailBinding>(
    FragmentSleepBlogDetailBinding::inflate
) {
    private lateinit var type: SleepBlogCategories

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    private lateinit var sleepBlogAdapter: SleepBlogDetailAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        arguments?.let {
            type = SleepBlogDetailFragmentArgs.fromBundle(it).blogData
            binding.tvTitle.text = type.title
            binding.tvSubTitle.text = type.sub_title
            binding.tvDescription.text = type.description
            Glide.with(binding.ivMain.context)
                .load(type.image)
                .into(binding.ivMain)
            sleepBlogAdapter.setDataSet(type.sleepSubCategories)
        }
    }

    private fun setRecycler() {
        sleepBlogAdapter = SleepBlogDetailAdapter()
        with(binding.rvList) {
            layoutManager = LinearLayoutManager(context)
            adapter = sleepBlogAdapter
        }
        binding.rvList.isNestedScrollingEnabled = false
    }

    override fun initListener() {
        binding.bBack.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }
}