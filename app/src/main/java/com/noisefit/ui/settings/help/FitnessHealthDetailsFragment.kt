package com.noisefit.ui.settings.help

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit_commans.data.model.FitnessHealthModel
import com.noisefit.luna.databinding.FragmentFitnessHealthDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.web.WebViewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FitnessHealthDetailsFragment :
    BaseFragment<FragmentFitnessHealthDetailsBinding>(FragmentFitnessHealthDetailsBinding::inflate) {

    private val viewModel: FitnessHealthViewModel by viewModels()

    private val adapter: HealthItemDetailsAdapter by lazy {
        HealthItemDetailsAdapter(requireActivity(), object : HyperlinkAction {
            override fun onLinkClicked(item: FitnessHealthModel) {
                startActivity(
                    WebViewActivity.getStartIntent(
                        requireContext(),
                        "",
                        item.hyperLink
                    )
                )
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val listData =
                ArrayList(FitnessHealthDetailsFragmentArgs.fromBundle(it).itemData!!.toList())
            val title = FitnessHealthDetailsFragmentArgs.fromBundle(it).title
            viewModel.title = title
            viewModel.setDataInfoList(listData)
        }
        initView()
        setRecyclerView()
    }

    private fun initView() {
        binding.layoutToolbar.tvTitle.text = viewModel.title
    }

    private fun setRecyclerView() {

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        adapter.setDataSet(viewModel.getDataInfoList())
    }

    override fun initListener() {
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }


}