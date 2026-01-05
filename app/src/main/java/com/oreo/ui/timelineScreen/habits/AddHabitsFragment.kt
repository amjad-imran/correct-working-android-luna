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
import android.util.DisplayMetrics
import android.view.View
import androidx.activity.addCallback
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.moengage.core.internal.utils.showToast
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddHabitsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.hideKeyboard
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.timeline.habits.CategoryUi
import com.oreo.data.model.timeline.habits.HabitListItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddHabitsFragment : BaseFragment<FragmentAddHabitsBinding>(FragmentAddHabitsBinding::inflate) {

    private val viewModel: AddHabitsViewModel by viewModels()

    private val args: AddHabitsFragmentArgs by navArgs()
    private lateinit var mLayoutManager: LinearLayoutManager

    private val adapter by lazy {
        HabitsAdapter { habit ->
            binding.etSearch.hideKeyboard()
            habit.id?.let { viewModel.toggleHabit(it) }
        }
    }

    private var scrollFromTab = false
    private var tabFromScroll = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedHabitsFromBundle = args.selectedOptions

        setUi(viewModel.selectedHabitsFromBundle?.options.isNullOrEmpty())
        setRecycler()
        setupTabClickScroll()
        setupRecyclerScrollTabHighlight()
    }

    private fun smoothScrollToHeader(pos: Int) {
        val scroller = object : LinearSmoothScroller(requireContext()) {
            override fun getVerticalSnapPreference() = SNAP_TO_START
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 80f / displayMetrics.densityDpi
            }
        }
        scroller.targetPosition = pos
        mLayoutManager.startSmoothScroll(scroller)
    }

    private fun setupTabClickScroll() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tabFromScroll) return

                val state = viewModel.uiState.value
                val catId = tab.tag as? String ?: return
                val pos = state.headerPositions[catId] ?: return

                scrollFromTab = true
                smoothScrollToHeader(pos)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab) = onTabSelected(tab)
        })
    }

    private fun setUi(isAddHabitsTitle: Boolean) {
        binding.toolbar.apply {
            tvTitle.text = if (isAddHabitsTitle) getString(R.string.text_add_habits)
                        else getString(R.string.text_edit_habits)
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
        mLayoutManager = LinearLayoutManager(requireContext())
        binding.rvHabits.layoutManager = mLayoutManager
        binding.rvHabits.adapter = adapter
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvSave.setOnClickListener {
            val selected = viewModel.uiState.value.selectedHabits.toList()
            if(selected.isEmpty()){
                showToast(requireContext(), "Please Select At least 1 Habit")
            }else {
                viewModel.saveHabitsToServer(selected) {
                    displaySuccessBottomSheet()
                }
            }
            // e.g. setResult(RESULT_OK, Intent().putStringArrayListExtra("selectedHabits", ArrayList(selected)))
            // finish()
        }

        binding.toolbar.backBtn.setOnClickListener {
            displayDiscardChangesBS()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            displayDiscardChangesBS()
        }

        // Text watcher for search field
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s?.toString().orEmpty())
            }
        })

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            if (binding.etSearch.text.isNullOrEmpty()) {
                viewModel.resetSearch()
            }
            false
        }

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

    private fun displayDiscardChangesBS() {
        setFragmentResultListener(AddHabitsDiscardChangesBS.DISCARD_CHANGES_ADD_HABITS_KEY) { _, bundle ->
            val isDiscardClicked = bundle.getBoolean("isDiscardClicked")
            if(isDiscardClicked==true){
                navigateUpSafe()
            }
        }
        navigate(R.id.addHabitsDiscardChangesBS)
    }

    private fun setupTabsIfNeeded(categories: List<CategoryUi>) {
        val tabLayout = binding.tabLayout

        // rebuild only if count differs or ids differ
        val shouldRebuild =
            tabLayout.tabCount != categories.size ||
                    (0 until tabLayout.tabCount).any { idx ->
                        tabLayout.getTabAt(idx)?.tag != categories.getOrNull(idx)?.id
                    }

        if (!shouldRebuild) return

        tabLayout.removeAllTabs()
        categories.forEach { cat ->
            tabLayout.addTab(tabLayout.newTab().setText(cat.title).setTag(cat.id))
        }
    }

    override fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                // submit list
                adapter.submitList(state.items)
                adapter.updateSelectedHabits(state.selectedHabits)

                // tabs
                if(state.isSearchActive){
                    binding.tabLayout.gone()
                }else {
                    binding.tabLayout.visible()
                    setupTabsIfNeeded(state.categories)
                }

                state.error?.let { /* show toast/snackbar */ }
            }
        }

        //
        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }

    private fun setupRecyclerScrollTabHighlight() {
        binding.rvHabits.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (scrollFromTab) return

                val firstVisible = mLayoutManager.findFirstVisibleItemPosition()
                if (firstVisible == RecyclerView.NO_POSITION) return

                val state = viewModel.uiState.value
                val currentCategoryId = findCurrentCategoryId(firstVisible, state.items) ?: return
                val tabIndex = state.categories.indexOfFirst { it.id == currentCategoryId }

                if (tabIndex >= 0 && tabIndex != binding.tabLayout.selectedTabPosition) {
                    tabFromScroll = true
                    binding.tabLayout.getTabAt(tabIndex)?.select()
                    tabFromScroll = false
                }
            }

            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) scrollFromTab = false
            }
        })
    }

    private fun findCurrentCategoryId(firstVisible: Int, list: List<HabitListItem>): String? {
        var i = firstVisible
        while (i >= 0) {
            val row = list.getOrNull(i) ?: return null
            if (row is HabitListItem.Header) return row.category.id
            i--
        }
        return null
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