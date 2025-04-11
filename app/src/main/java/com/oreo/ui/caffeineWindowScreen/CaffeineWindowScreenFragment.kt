package com.oreo.ui.caffeineWindowScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCaffeineWindowScreenBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.setVisibilityByCondition
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CaffeineWindowScreenFragment :
    BaseFragment<FragmentCaffeineWindowScreenBinding>(FragmentCaffeineWindowScreenBinding::inflate) {

    private val viewModel: CaffeineWindowScreenViewModel by viewModels()

    private val myItemsAdapter by lazy {
        ItemAdapter(object : ItemClickListener{
            override fun onItemStateChanged(position: Int) {

            }

        })
    }

    private val allItemsAdapter by lazy {
        ItemAdapter(object : ItemClickListener{
            override fun onItemStateChanged(position: Int) {

            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadItems()
        initUi()
    }

    private fun initUi() {
        binding.apply {
            layoutToolbar.tvTitle.text = getString(R.string.text_caffeine_window)

            rvMyItems.layoutManager = LinearLayoutManager(context)
            rvMyItems.adapter = myItemsAdapter

            rvAllItems.layoutManager = LinearLayoutManager(context)
            rvAllItems.adapter = allItemsAdapter

        }
    }

    override fun initListener() {
        binding.ivToggleRv.setOnClickListener{
            viewModel.rvDisplayAllItemsToggleState.postValue(!viewModel.rvDisplayAllItemsToggleState.value!!)
        }
    }

    override fun subscribeObservers() {
        viewModel.rvDisplayAllItemsToggleState.observe(this){
            binding.ivToggleRv.setImageResource(
                if (it) R.drawable.ic_baseline_keyboard_arrow_down_24
                else R.drawable.ic_arrow_up_stress
            )

            binding.rvAllItems.setVisibilityByCondition(it)
        }

        viewModel.myItemsList.observe(viewLifecycleOwner) { items ->
            myItemsAdapter.updateItems(items)
        }

        viewModel.allItemsList.observe(viewLifecycleOwner) { items ->
            allItemsAdapter.updateItems(items)
        }
    }

}