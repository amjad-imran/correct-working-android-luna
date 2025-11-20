package com.oreo.ui.timelineScreen.habits

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
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

        binding.searchBox.background = createSearchBarBg(
            cornerRadius = 100f,
            borderWidth = 3f,
            backgroundColor = "#0D1113".toColorInt(),
            borderStartColor = "#26FFFFFF".toColorInt(),
            borderEndColor = "#00FFFFFF".toColorInt(),
        )
    }

    private fun setRecycler() {
        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHabits.adapter = adapter
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvSave.setOnClickListener {
            // You can return selected habit IDs to caller
            val selected = viewModel.uiState.value.selectedHabits.toList()
            viewModel.saveHabitsToServer(selected){
                displaySuccessBottomSheet()
            }
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

    private fun displaySuccessBottomSheet() {
        setFragmentResultListener(ADD_HABITS_SUCCESS_BS_KEY) { _, bundle ->
            val isOkayClicked = bundle.getBoolean("okayClicked")
            if(isOkayClicked==true){
                navigateUpSafe()
            }
        }
        navigate(
            R.id.addHabitsSuccessBottomSheet,
            bundleOf(
                "successTitle" to getString(R.string.text_habits_saved),
                "buttonText" to getString(R.string.text_done),
            )
        )
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

    private fun createSearchBarBg(
        borderWidth: Float = 8f,
        cornerRadius: Float = 24f,
        backgroundColor: Int = 0x0FFFFFFF,
        borderStartColor: Int = 0xFFFF00FF.toInt(),
        borderEndColor: Int = 0xFF00FFFF.toInt()
    ): Drawable{

        val gradientColors = intArrayOf(borderStartColor, borderEndColor)

        // Background fill paint
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }

        // Gradient border paint
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, 400f,
                gradientColors, null, Shader.TileMode.CLAMP
            )
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }

        return object : Drawable() {
            override fun draw(canvas: Canvas) {
                val halfBorder = borderWidth / 2

                // Background rect
                val fillRect = RectF(
                    0f,
                    0f,
                    bounds.width().toFloat(),
                    bounds.height().toFloat()
                )
                canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, fillPaint)

                // Border rect
                val strokeRect = RectF(
                    halfBorder,
                    halfBorder,
                    bounds.width() - halfBorder,
                    bounds.height() - halfBorder
                )
                canvas.drawRoundRect(strokeRect, cornerRadius, cornerRadius, strokePaint)
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(cf: ColorFilter?) {}
            override fun getOpacity() = PixelFormat.TRANSLUCENT
        }
    }

}