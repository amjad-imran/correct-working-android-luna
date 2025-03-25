package com.oreo.ui.customHomeScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCustomHomeScreenBinding
import com.noisefit_commans.ui.BaseFragment

class CustomHomeScreenFragment :
    BaseFragment<FragmentCustomHomeScreenBinding>(FragmentCustomHomeScreenBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    override fun initListener() {
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.switchMain.setOnCheckedChangeListener { _, isChecked ->
            binding.recyclerView.visibility = if (isChecked) View.GONE else View.VISIBLE
            binding.tvMessage.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    override fun subscribeObservers() {

    }

    private fun initUi(){

        binding.recyclerView.visibility = if (binding.switchMain.isChecked) View.GONE else View.VISIBLE
        binding.layoutToolbar.tvTitle.text = getString(R.string.text_customize_homescreen)
        binding.tvMessage.visibility = if (binding.switchMain.isChecked) View.VISIBLE else View.GONE

        val _items = MutableLiveData(mutableListOf(
            CustomHomeScreenItem(R.drawable.icon_google_fit, "Sleep", false),
            CustomHomeScreenItem(R.drawable.icon_google_fit, "Activity", false),
            CustomHomeScreenItem(R.drawable.icon_google_fit, "Readiness", false),
            CustomHomeScreenItem(R.drawable.icon_google_fit, "Sleep Planner", false),
        ))
        val items: LiveData<MutableList<CustomHomeScreenItem>> = _items

        val adapter = ItemAdapter(items.value ?: mutableListOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        // Attach Drag & Drop
        val touchHelper = ItemTouchHelper(ItemMoveCallback(adapter))
        touchHelper.attachToRecyclerView(binding.recyclerView)
        adapter.onDragStartListener = { viewHolder ->
            touchHelper.startDrag(viewHolder)
        }

        // Observe changes in the list
        _items.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }
    }

}