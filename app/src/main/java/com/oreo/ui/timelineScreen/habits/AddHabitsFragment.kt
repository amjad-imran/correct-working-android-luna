package com.oreo.ui.timelineScreen.habits

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddHabitsBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.timeline.habits.toListItems
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddHabitsFragment : BaseFragment<FragmentAddHabitsBinding>(FragmentAddHabitsBinding::inflate) {

    private val viewModel: AddHabitsViewModel by viewModels()

    private val adapter by lazy {
        HabitsAdapter { habit ->
            viewModel.toggleHabitSelection(habit.id)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUi()
        setRecycler()
    }

    private fun setUi() {
        binding.toolbar.apply {
            tvTitle.text = getString(R.string.text_add_habits)
            backBtn.setImageResource(R.drawable.ic_close_add_habits)
        }
    }

    private fun setRecycler() {
        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHabits.adapter = adapter
    }

    override fun initListener() {
        binding.tvSave.setOnClickListener {
            // You can return selected habit IDs to caller
            val selected = viewModel.uiState.value.selectedHabits.toList()
            // e.g. setResult(RESULT_OK, Intent().putStringArrayListExtra("selectedHabits", ArrayList(selected)))
            // finish()
        }

        // Text watcher for search field
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s?.toString().orEmpty())
            }
        })

    }

    override fun subscribeObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                val tabLayout = binding.tabLayout
                // Tabs
                if (tabLayout.tabCount == 0 && state.categories.isNotEmpty()) {
                    state.categories.forEach { category ->
                        tabLayout.addTab(tabLayout.newTab().setText(category.name).setTag(category.id))
                    }
                }

                // Select correct tab
                val selectedId = state.selectedCategoryId
                if (selectedId != null && tabLayout.tabCount > 0) {
                    val index = state.categories.indexOfFirst { it.id == selectedId }
                    if (index >= 0 && index != tabLayout.selectedTabPosition) {
                        tabLayout.getTabAt(index)?.select()
                    }
                }

                // List items
                adapter.submitList(state.toListItems())
                adapter.selectedHabits = state.selectedHabits
            }
        }

        // Tab listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val categoryId = tab?.tag as? String ?: return
                viewModel.onCategorySelected(categoryId)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

    }

}