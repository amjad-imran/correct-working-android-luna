package com.oreo.ui.customHomeScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCustomHomeScreenBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.setVisibilityByCondition
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomHomeScreenFragment :
    BaseFragment<FragmentCustomHomeScreenBinding>(FragmentCustomHomeScreenBinding::inflate) {

        private val viewModel: CustomHomescreenViewModel by viewModels()
    private val adapter by lazy {
        ItemAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    override fun initListener() {
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.switchMain.setOnCheckedChangeListener { _, isChecked ->
            binding.recyclerView.setVisibilityByCondition(!isChecked) // = if (isChecked) View.GONE else View.VISIBLE
            binding.tvMessage.setVisibilityByCondition(isChecked) // visibility = if (isChecked) View.VISIBLE else View.GONE

            binding.tvOtherMessage.setVisibilityByCondition(!isChecked) // = if (isChecked) View.GONE else View.VISIBLE
            binding.bSaveChanges.setVisibilityByCondition(!isChecked) // = if (isChecked) View.GONE else View.VISIBLEbinding.recyclerView.setVisibilityByCondition(!isChecked) // = if (isChecked) View.GONE else View.VISIBLE
        }

        binding.bSaveChanges.setOnClickListener {
            val updatedList = adapter.getDataSet()
            viewModel.updateData(updatedList)
        }
    }

    override fun subscribeObservers() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateData(items)
        }
    }

    private fun initUi(){

        binding.recyclerView.setVisibilityByCondition(!(binding.switchMain.isChecked)) //= if (binding.switchMain.isChecked) View.GONE else View.VISIBLE
        binding.layoutToolbar.tvTitle.text = getString(R.string.text_customize_homescreen)
        binding.tvMessage.setVisibilityByCondition(binding.switchMain.isChecked) // if (binding.switchMain.isChecked) View.VISIBLE else View.GONE

        binding.tvOtherMessage.setVisibilityByCondition(!(binding.switchMain.isChecked)) //= if (binding.switchMain.isChecked) View.GONE else View.VISIBLEbinding.recyclerView.setVisibilityByCondition(!(binding.switchMain.isChecked)) //= if (binding.switchMain.isChecked) View.GONE else View.VISIBLE
        binding.bSaveChanges.setVisibilityByCondition(!(binding.switchMain.isChecked)) //= if (binding.switchMain.isChecked) View.GONE else View.VISIBLEbinding.recyclerView.setVisibilityByCondition(!(binding.switchMain.isChecked)) //= if (binding.switchMain.isChecked) View.GONE else View.VISIBLE

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        // Attach Drag & Drop
        val touchHelper = ItemTouchHelper(ItemMoveCallback(adapter))
        touchHelper.attachToRecyclerView(binding.recyclerView)
        adapter.onDragStartListener = { viewHolder ->
            touchHelper.startDrag(viewHolder)
        }

    }

}