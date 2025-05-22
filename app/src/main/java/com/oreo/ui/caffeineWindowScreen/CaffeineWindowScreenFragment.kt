package com.oreo.ui.caffeineWindowScreen

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
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

    private val args: CaffeineWindowScreenFragmentArgs by navArgs()

    private val myItemsAdapter by lazy {
        ItemAdapter()
    }

    private val allItemsAdapter by lazy {
        ItemAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            args.caffeineGraphData?.let {
                binding.caffeineGraphView.updateData(it)

                myItemsAdapter.setMaxQuantity(it.maxQuantity?:0)
                allItemsAdapter.setMaxQuantity(it.maxQuantity?:0)
                viewModel.maxQuantity = it.maxQuantity?:0

                viewModel.loadItems()
                initUi(it.message?:"")
            }
        }
    }

    private fun initUi(message: String) {
        binding.apply {
            layoutToolbar.tvTitle.text = getString(R.string.text_caffeine_window)

            val fullText = message
            val splitIndex = fullText.indexOf(':')

            if (splitIndex != -1) {
                val startingWords = "${fullText.substring(0, splitIndex)}:"

                val spannableString = SpannableString(fullText)

                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    startingWords.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                description.text = spannableString
            }
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
                    val myItemsList = viewModel._myItemsList.value
                    if (!myItemsList.isNullOrEmpty()){
                        val curIdx = type.position
                        val curItem = myItemsList.get(curIdx)
                        curItem.is_favorite = false

                        //
                        viewModel.hitPostApiToUpdateItems(
                            CaffeinePostApiModel(
                                remove_item_id = curItem.id
                            )
                        )
                        //

                        viewModel.performAddOrDeleteAndSorting(curItem, false, false)
//                        myItemsList.removeAt(curIdx)
//                        myItemsAdapter.updateSingleItem(null, curIdx)

                        viewModel.performAddOrDeleteAndSorting(curItem, true, true)
//                        viewModel._allItemsList.value?.add(curItem)
//                        allItemsAdapter.updateSingleItem(curItem, -1)

                    }
                }
                is CaffeineWindowScreenClickEnum.onNotFavIconClicked -> {}
            }
        }

        allItemsAdapter.itemClickListener = { type ->
            when(type){
                is CaffeineWindowScreenClickEnum.onFavIconClicked -> {}
                is CaffeineWindowScreenClickEnum.onNotFavIconClicked -> {
                    val allItemsList = viewModel._allItemsList.value
                    if (!allItemsList.isNullOrEmpty()){
                        val curIdx = type.position
                        val curItem = allItemsList.get(curIdx)
                        curItem.is_favorite = true

                        //
                        viewModel.hitPostApiToUpdateItems(
                            CaffeinePostApiModel(
                                favorite_item_id = curItem.id
                            )
                        )
                        //

                        viewModel.performAddOrDeleteAndSorting(curItem, true, false)
//                        allItemsList.removeAt(curIdx)
//                        allItemsAdapter.updateSingleItem(null, curIdx)

                        viewModel.performAddOrDeleteAndSorting(curItem, false, true)
//                        viewModel._myItemsList.value?.add(curItem)
//                        myItemsAdapter.updateSingleItem(curItem, -1)
                    }
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

        viewModel._myItemsList.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                myItemsAdapter.updateItems(items)
//                binding.rvMyItems.setVisibilityByCondition(true)
            }
        }

        viewModel._allItemsList.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                allItemsAdapter.updateItems(items)
//                binding.rvAllItems.setVisibilityByCondition(true)
            }
        }

    }

//    private fun setRvVisibility(){
//        binding.rvMyItems.setVisibilityByCondition(myItemsAdapter.getItemsListSize() > 0)
//        binding.rvAllItems.setVisibilityByCondition(allItemsAdapter.getItemsListSize() > 0)
//    }

}