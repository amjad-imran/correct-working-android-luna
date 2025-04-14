package com.oreo.ui.caffeineWindowScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCaffeineWindowScreenBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CaffeineWindowScreenFragment :
    BaseFragment<FragmentCaffeineWindowScreenBinding>(FragmentCaffeineWindowScreenBinding::inflate) {

    private val viewModel: CaffeineWindowScreenViewModel by viewModels()

    private val myItemsAdapter by lazy {
        ItemAdapter()
    }

    private val allItemsAdapter by lazy {
        ItemAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadItems()
        initUi()
    }

    private fun initUi() {
        binding.apply {
            layoutToolbar.tvTitle.text = getString(R.string.text_caffeine_window)
        }
        setAdapter()
    }

    private fun setAdapter(){
        binding.rvMyItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myItemsAdapter
        }

        binding.rvAllItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = allItemsAdapter
        }

    }

    override fun initListener() {
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivToggleRv.setOnClickListener{
            viewModel.rvDisplayAllItemsToggleState.postValue(!viewModel.rvDisplayAllItemsToggleState.value!!)
        }

        myItemsAdapter.itemClickListener = { type ->
            when(type){
                is CaffeineWindowScreenClickEnum.onFavIconClicked -> {
                    val myItemsList = ArrayList<CaffeineFoodItem>(myItemsAdapter.getItemsList())
                    val curIdx = type.position
                    val curItem = myItemsList.get(curIdx)
                    curItem.is_favourite = false
                    myItemsList.removeAt(curIdx)
                    myItemsAdapter.updateItems(myItemsList)

                    val allItemsList = ArrayList<CaffeineFoodItem>(allItemsAdapter.getItemsList())
                    allItemsList.add(curItem)
                    allItemsAdapter.updateItems(allItemsList)
                }
                is CaffeineWindowScreenClickEnum.onNotFavIconClicked -> {}
            }
        }

        allItemsAdapter.itemClickListener = { type ->
            when(type){
                is CaffeineWindowScreenClickEnum.onFavIconClicked -> {}
                is CaffeineWindowScreenClickEnum.onNotFavIconClicked -> {
                    val allItemsList = ArrayList<CaffeineFoodItem>(allItemsAdapter.getItemsList())
                    val curIdx = type.position
                    val curItem = allItemsList.get(curIdx)
                    curItem.is_favourite = true
                    allItemsList.removeAt(curIdx)
                    allItemsAdapter.updateItems(allItemsList)

                    val myItemsList = ArrayList<CaffeineFoodItem>(myItemsAdapter.getItemsList())
                    myItemsList.add(curItem)
                    myItemsAdapter.updateItems(myItemsList)
                }
            }
        }

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

        viewModel.dataUpdated.observe(this){
            it.getContent()?.let {

            }
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