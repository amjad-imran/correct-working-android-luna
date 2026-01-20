package com.oreo.ui.lifeos.insightsLvl1

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentHelpUsImproveBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

class HelpUsImproveBottomSheet :
    BaseBottomSheetWithTransparent<FragmentHelpUsImproveBottomSheetBinding>(
        FragmentHelpUsImproveBottomSheetBinding::inflate
    ) {

    companion object{
        const val HELP_US_IMPROVE_BS_INSIGHTS = "HELP_US_IMPROVE_BS_INSIGHTS"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reasons = arguments?.getStringArrayList("reasons")

        setChips(
            items = reasons?: ArrayList(),
            selectedBg = createGradientBorderDrawable(
                cornerRadius = 100f,
                borderWidth = 2f,
                backgroundColor = "#3DFFFFFF".toColorInt(),
                gradientColors = intArrayOf(
                    "#1AFFFFFF".toColorInt(),
                    "#00FFFFFF".toColorInt(),
                ),
            ),
            unSelectedBg = createGradientBorderDrawable(
                cornerRadius = 100f,
                borderWidth = 2f,
                backgroundColor = "#0AFFFFFF".toColorInt(),
                gradientColors = intArrayOf(
                    "#1AFFFFFF".toColorInt(),
                    "#00FFFFFF".toColorInt(),
                ),
            )
        )
    }

    private val selectedPositions = mutableSetOf<Int>()

    private fun setChips(items: List<String>, selectedBg: Drawable, unSelectedBg: Drawable) {
        val chipGrp = binding.chipGroupReasons
        chipGrp.removeAllViews()

        items.forEachIndexed { index, item ->
            val mChip =
                layoutInflater.inflate(R.layout.layout_add_event_readiness_chip, chipGrp, false)

            mChip.background =
                if(mChip.isSelected) selectedBg
                else unSelectedBg

            val tvTxt = mChip.findViewById<TextView>(R.id.tvTitle)
            tvTxt.text = item
            mChip.tag = index

            mChip.setOnClickListener {
                val nowSelected = !mChip.isSelected
                mChip.isSelected = nowSelected
                mChip.background = if (nowSelected) selectedBg else unSelectedBg

                if (nowSelected) selectedPositions.add(index)
                else selectedPositions.remove(index)
            }
            chipGrp.addView(mChip)
        }
    }

    override fun initListener() {
        binding.etFeedback.doOnTextChanged { text, _, _, _ ->
            updateSubmitState(text)
        }

        binding.btnSubmit.setOnClickListener {
            dismiss()
            val reasons = arguments?.getStringArrayList("reasons")
            if(reasons==null) return@setOnClickListener

            val selected = selectedPositions.map { reasons[it] }
            setFragmentResult(
                HELP_US_IMPROVE_BS_INSIGHTS,
                Bundle().apply {
                    putString("feedbackText", binding.etFeedback.text.toString())
                    if(selected.isNotEmpty()){
                        putString("reasons", selected.toString())
                    }
                }
            )
        }
    }

    override fun subscribeObservers() {

    }

    override fun getTheme(): Int {
        return R.style.MyCustomDialogStyleWithBlurEffect
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                isCancelable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

    private fun updateSubmitState(text: CharSequence?) {
        val enabled = !text.isNullOrBlank()
        binding.btnSubmit.apply {
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.5f
        }
    }

    fun createGradientBorderDrawable(
        cornerRadius: Float = 18f,
        borderWidth: Float = 1.5f,
        backgroundColor: Int = "#33FFFFFF".toColorInt(),
        gradientColors: IntArray = intArrayOf(
            "#6A5CFF".toColorInt(), // start color (change)
            "#00D4FF".toColorInt()  // end color (change)
        )
    ): Drawable {

        // Paint for the background fill
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }

        // Paint for the gradient border stroke
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, 400f,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }

        return object : Drawable() {
            override fun draw(canvas: Canvas) {
                val halfBorder = borderWidth / 2

                // Full rect for background fill
                val fillRect = RectF(
                    0f,
                    0f,
                    bounds.width().toFloat(),
                    bounds.height().toFloat()
                )
                canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, fillPaint)

                // Inset rect for border
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