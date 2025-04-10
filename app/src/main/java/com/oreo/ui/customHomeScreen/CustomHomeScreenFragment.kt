package com.oreo.ui.customHomeScreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

        binding.switchMain.setOnCheckedChangeListener { button, isChecked ->

            if (button.isPressed.not()) {
                return@setOnCheckedChangeListener
            }

            if(isChecked){
                viewModel.updateData(isChecked, adapter.getDataSet())
            }

            if(isChecked){
                binding.recyclerView.gone()
                binding.tvOtherMessage.gone()
                binding.bSaveChanges.gone()
                binding.tvMessage.visible()
            }else{
                binding.tvMessage.gone()
                binding.recyclerView.visible()
                binding.tvOtherMessage.visible()
                binding.bSaveChanges.visible()
            }

        }

        binding.bSaveChanges.setOnClickListener {
            val updatedList = adapter.getDataSet()
            viewModel.updateData(binding.switchMain.isChecked, updatedList)
        }

        viewModel.dataUpdated.observe(this){
            it.getContent()?.let {
                binding.bSaveChanges.isEnabled = false
                binding.blurView.visible()
                binding.txtSaved.visible()
                Handler(Looper.myLooper()!!).postDelayed({
                    try {
                        navigateUpSafe()
                    }catch (e: Exception){

                    }
                }, 2000)
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateData(items)
        }

        viewModel.lunaManagedState.observe(this){
            it?.let {
                binding.switchMain.isChecked = it
                if(it){
                    binding.recyclerView.gone()
                    binding.tvOtherMessage.gone()
                    binding.bSaveChanges.gone()
                    binding.tvMessage.visible()
                }else{
                    binding.tvMessage.gone()
                    binding.recyclerView.visible()
                    binding.tvOtherMessage.visible()
                    binding.bSaveChanges.visible()
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